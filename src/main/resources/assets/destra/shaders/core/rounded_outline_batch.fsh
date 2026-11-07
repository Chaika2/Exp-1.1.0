#version 150

#define MAX_BATCH 30
#define STRIDE 30

uniform float shapeData[MAX_BATCH * STRIDE];

flat in int ShapeIndex;

out vec4 fragColor;

float lnorm(vec2 value, float power) {
    value = abs(value);
    return pow(pow(value.x, power) + pow(value.y, power), 1.0 / power);
}

float roundedBoxSDF(vec2 p, vec2 halfSize, vec4 radius, float cornerPower) {
    float cornerRadius = p.x > 0.0
            ? (p.y > 0.0 ? radius.y : radius.z)
            : (p.y > 0.0 ? radius.x : radius.w);
    vec2 q = abs(p) - halfSize + cornerRadius;
    float n = max(cornerPower, 1.0);
    return min(max(q.x, q.y), 0.0) + lnorm(max(q, vec2(0.0)), n) - cornerRadius;
}

vec4 gradient(vec2 uv, vec4 color1, vec4 color2, vec4 color3, vec4 color4) {
    vec2 t = clamp(uv, 0.0, 1.0);
    t = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    vec4 top = mix(color1, color2, t.x);
    vec4 bottom = mix(color4, color3, t.x);
    return mix(bottom, top, t.y);
}

void main() {
    int base = ShapeIndex * STRIDE;

    vec2 location = vec2(shapeData[base], shapeData[base + 1]);
    vec2 size = max(vec2(shapeData[base + 2], shapeData[base + 3]), vec2(0.0001));
    vec4 radius = vec4(shapeData[base + 4], shapeData[base + 5], shapeData[base + 6], shapeData[base + 7]);
    float thickness = shapeData[base + 8];
    vec4 color1 = vec4(shapeData[base + 9], shapeData[base + 10], shapeData[base + 11], shapeData[base + 12]);
    vec4 color2 = vec4(shapeData[base + 13], shapeData[base + 14], shapeData[base + 15], shapeData[base + 16]);
    vec4 color3 = vec4(shapeData[base + 17], shapeData[base + 18], shapeData[base + 19], shapeData[base + 20]);
    vec4 color4 = vec4(shapeData[base + 21], shapeData[base + 22], shapeData[base + 23], shapeData[base + 24]);

    if (thickness <= 0.0) {
        discard;
    }

    vec2 halfSize = size * 0.5;
    vec4 clampedRadius = min(radius, vec4(min(halfSize.x, halfSize.y)));
    vec2 frag = gl_FragCoord.xy - location;
    float distance = roundedBoxSDF(frag - halfSize, halfSize, clampedRadius, 2.0);
    float edge = clamp(fwidth(distance), 0.65, 1.25);

    float outerAlpha = 1.0 - smoothstep(-edge, edge, distance);
    float innerAlpha = 1.0 - smoothstep(-thickness - edge, -thickness + edge, distance);
    float borderAlpha = clamp(outerAlpha - innerAlpha, 0.0, 1.0);

    if (borderAlpha <= 0.001) {
        discard;
    }

    vec4 color = gradient(frag / size, color1, color2, color3, color4);
    fragColor = vec4(color.rgb, color.a * borderAlpha);
}
