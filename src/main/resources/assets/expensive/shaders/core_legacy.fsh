#version 330 core

#define COLOR_MODE 0
#define TEXTURE_MODE 1
#define ROUNDED_RECTANGLE_MODE 2
#define ROUNDED_TEXTURE_MODE 3
#define BLUR 4
#define CHECKER_MODE 5
#define CIRCLE_MODE 6
#define OUTER_MASK 7
#define ALPHA_MASK 8
#define MSDF_FONT 9
#define RADIAL_ROUNDED_RECTANGLE_MODE 10

in vec2 meshPosition;
in vec2 meshSize;
in vec2 texCoord;
in vec4 radius;
in vec4 color;
in vec4 outlineColor;
in float thickness;
in float softness;

flat in int texIndex;
flat in int drawMode;
flat in int maskIndex;

out vec4 fragColor;

uniform sampler2D textureSampler[8];
uniform vec2 resolution;


float sdRoundedBox(in vec2 point, in vec2 size, in vec4 r) {
    r.xy = (point.x > 0.0) ? r.xy : r.zw;
    r.x = (point.y > 0.0) ? r.x : r.y;
    vec2 q = abs(point) - size + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

float median(vec3 color) {
    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));
}
vec4 sampleTex(int id, vec2 uv);
ivec2 sampleTexSize(int id);

vec4 sampleTex(int id, vec2 uv) {
    if (id == 0)  return texture(textureSampler[0],  uv);
    if (id == 1)  return texture(textureSampler[1],  uv);
    if (id == 2)  return texture(textureSampler[2],  uv);
    if (id == 3)  return texture(textureSampler[3],  uv);
    if (id == 4)  return texture(textureSampler[4],  uv);
    if (id == 5)  return texture(textureSampler[5],  uv);
    if (id == 6)  return texture(textureSampler[6],  uv);
    if (id == 7)  return texture(textureSampler[7],  uv);
    return vec4(1.0, 0.0, 1.0, 1.0);
}

ivec2 sampleTexSize(int id) {
    if (id == 0)  return textureSize(textureSampler[0],  0);
    if (id == 1)  return textureSize(textureSampler[1],  0);
    if (id == 2)  return textureSize(textureSampler[2],  0);
    if (id == 3)  return textureSize(textureSampler[3],  0);
    if (id == 4)  return textureSize(textureSampler[4],  0);
    if (id == 5)  return textureSize(textureSampler[5],  0);
    if (id == 6)  return textureSize(textureSampler[6],  0);
    if (id == 7)  return textureSize(textureSampler[7],  0);
    return ivec2(1, 1);
}

void main() {
    switch (drawMode) {
        case COLOR_MODE:
        {
            fragColor = color;
            break;
        }
        case TEXTURE_MODE:
        {
            vec4 textureColor = sampleTex(texIndex, texCoord);

            if (textureColor.a == 0.0) {
                discard;
            }

            fragColor = textureColor * color;
            break;
        }
        case ROUNDED_RECTANGLE_MODE:
        {
            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);
            float dist = sdRoundedBox(center, meshSize / 2.0, radius);
            float smoothedAlpha = 1.0 - smoothstep(-1.0, thickness > 0. ? 1. : softness + 1., dist);
            float smoothedborderAlpha = (1.0 - smoothstep(-softness, softness, dist));
            float borderAlpha = 1.0 - smoothstep(thickness - 2.0, thickness, abs(dist));

            if (smoothedAlpha < 0.49 && thickness > 0.) {
                fragColor = vec4(outlineColor.rgb, outlineColor.a * smoothedborderAlpha);
            } else {
                vec4 basicColor = vec4(color.rgb, color.a * smoothedAlpha);

                fragColor = mix(vec4(color.rgb, 0.), mix(basicColor, thickness > 0. ? outlineColor : basicColor,
                outlineColor.a *  borderAlpha), smoothedAlpha);
            }
            break;
        }
        case ROUNDED_TEXTURE_MODE:
        {
            vec4 textureColor = sampleTex(texIndex, texCoord);
            vec4 textureColorMultipliedByInputColor = textureColor * color;

            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);
            float dist = sdRoundedBox(center, meshSize / 2.0, radius);
            float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);

            fragColor = vec4(textureColorMultipliedByInputColor.rgb, textureColorMultipliedByInputColor.a * alpha);
            break;
        }
        case BLUR:
        {
            vec2 pos = gl_FragCoord.xy;
            vec2 blurredPos = pos / resolution;
            vec4 textureColor = sampleTex(texIndex, blurredPos);
            vec3 blurredColor = textureColor.rgb;

            vec2 center = pos - meshPosition - (meshSize / 2.0);
            float dist = sdRoundedBox(center, meshSize / 2.0, radius);
            float alpha = 1.0 - smoothstep(-softness, softness, dist);

            fragColor = vec4(blurredColor.rgb * color.rgb, color.a * alpha);
            break;
        }
        case CHECKER_MODE: {
            vec2 local = gl_FragCoord.xy - meshPosition;

            float rows = 2.0;

            float cellSize = min(meshSize.x, meshSize.y) / rows;

            ivec2 cell = ivec2(floor(local / cellSize));

            bool isWhite = (cell.x + cell.y) % 2 == 0;

            vec3 checkerColor = isWhite ? color.rgb : outlineColor.rgb;
            float alpha = isWhite ? color.a : outlineColor.a;

            vec2 center = local - (meshSize * 0.5);
            float dist = sdRoundedBox(center, meshSize * 0.5, radius);
            float mask = 1.0 - smoothstep(-softness, softness, dist);

            fragColor = vec4(checkerColor, alpha * mask);
            break;
        }
        case CIRCLE_MODE:{
            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);
            float len = length(center);
            float dist = len - radius.x;
            if (radius.y > 0.0) {
                dist = max(dist, radius.y - len);
            }
            float smoothedAlpha = 1.0 - smoothstep(-1.0, thickness > 0. ? 1. : softness + 1., dist);
            float smoothedborderAlpha = (1.0 - smoothstep(-softness, softness, dist));
            float borderAlpha = 1.0 - smoothstep(thickness - 2.0, thickness, abs(dist));

            float angle = atan(center.y, center.x);
            if (angle < 0.0) angle += 6.28318530718;
            float start = texCoord.x;
            float end = texCoord.y;
            bool insideArc = end >= start ? (angle >= start && angle <= end) : (angle >= start || angle <= end);
            if (!insideArc) discard;

            if (smoothedAlpha < 0.49 && thickness > 0.) {
                fragColor = vec4(outlineColor.rgb, outlineColor.a * smoothedborderAlpha);
            } else {
                vec4 basicColor = vec4(color.rgb, color.a * smoothedAlpha);

                fragColor = mix(vec4(color.rgb, 0.), mix(basicColor, thickness > 0. ? outlineColor : basicColor,
                outlineColor.a *  borderAlpha), smoothedAlpha);
            }
            break;
        }
        case OUTER_MASK: {
            vec4 original = sampleTex(texIndex, texCoord);
            float mask = sampleTex(maskIndex, texCoord).a;

            vec3 rgb = original.rgb * color.rgb * (1.0 - mask);
            float a  = original.a * color.a * (1.0 - mask);

            fragColor = vec4(rgb, a);
            break;
        }
        case ALPHA_MASK: {
            vec4 textureColor = sampleTex(texIndex, texCoord);
            textureColor.a *= color.a;
            fragColor = textureColor;
            break;
        }
        case MSDF_FONT: {
            float dist = median(sampleTex(texIndex, texCoord).rgb) - 0.5 + thickness;
            vec2 h = vec2(dFdx(texCoord.x), dFdy(texCoord.y)) * vec2(sampleTexSize(texIndex));
            float pixels = 10 * inversesqrt(h.x * h.x + h.y * h.y);
            float alpha = smoothstep(-softness, softness, dist * pixels);
            vec4 outColor = vec4(color.rgb, color.a * alpha);
            fragColor = outColor;
            break;
        }
        case RADIAL_ROUNDED_RECTANGLE_MODE: {
            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);
            float dist = sdRoundedBox(center, meshSize / 2.0, radius);
            float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);
            vec2 normalized = center / (meshSize / 2.0);
            float radial = clamp(length(normalized), 0.0, 1.0);
            vec4 gradientColor = mix(color, outlineColor, radial);

            fragColor = vec4(gradientColor.rgb, gradientColor.a * alpha);
            break;
        }
    }
}

