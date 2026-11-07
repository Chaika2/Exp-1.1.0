#version 330 core

in vec2 uv;
out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D image;
uniform int useImage;
uniform float time;
uniform float thickness;
uniform vec2 resolution;
uniform vec4 outlineColor;
uniform float quality;

#define PI_2 (3.14159 * 2.0)

const float BlurWeight0 = 0.2;
const float BlurWeight1 = 0.18;
const float BlurWeight2 = 0.12;
const float BlurWeight3 = 0.05;

float detectObjectScale(vec2 texCoord) {
    vec2 texelSize = 1.0 / resolution;
    float maxDist = 0.0;
    const int samples = 8;

    for (int i = 0; i < samples; i++) {
        float angle = float(i) * (PI_2 / float(samples));
        vec2 dir = vec2(cos(angle), sin(angle));

        for (float dist = 1.0; dist < 50.0; dist += 1.0) {
            vec2 samplePos = texCoord + dir * texelSize * dist;
            if (texture(texture0, samplePos).a < 0.01) {
                maxDist = max(maxDist, dist);
                break;
            }
        }
    }

    return clamp(maxDist / 20.0, 0.3, 1.0);
}

vec4 boxBlur(vec2 texCoord, vec2 direction, float scale) {
    vec2 texelSize = 1.0 / resolution;
    vec2 sampleStep = direction * texelSize * thickness * 0.15 * scale;

    vec4 result = texture(texture0, texCoord) * BlurWeight0;

    vec2 s1 = sampleStep * 2.0;
    vec2 s2 = sampleStep * 4.0;
    vec2 s3 = sampleStep * 6.0;

    result += (texture(texture0, texCoord + s1) + texture(texture0, texCoord - s1)) * BlurWeight1;
    result += (texture(texture0, texCoord + s2) + texture(texture0, texCoord - s2)) * BlurWeight2;
    result += (texture(texture0, texCoord + s3) + texture(texture0, texCoord - s3)) * BlurWeight3;
    return result;
}

void main() {
    vec2 texCoord = uv;
    if (useImage == 1) {
        vec4 blurred = texture(texture0, texCoord);
        vec4 original = texture(image, texCoord);

        float fillMask = original.a;
        float blurMask = blurred.a;
        float combinedAlpha = max(blurMask * 1.34, fillMask * 0.72);
        combinedAlpha = pow(clamp(combinedAlpha, 0.0, 1.0), 0.72);

        if (combinedAlpha < 0.01) {
            discard;
        }

        float outerHalo = max(blurMask - fillMask, 0.0);
        float peakChannel = max(max(outlineColor.r, outlineColor.g), outlineColor.b);
        vec3 normalizedColor = peakChannel > 0.0 ? outlineColor.rgb / peakChannel : outlineColor.rgb;
        vec3 richColor = mix(outlineColor.rgb, normalizedColor, 0.42);
        richColor *= 1.16 + fillMask * 0.24 + outerHalo * 0.42;
        richColor = clamp(richColor, 0.0, 1.0);

        float pulse = 0.985 + 0.015 * sin(time * 2.0 + texCoord.x * 7.0 + texCoord.y * 5.0);
        float finalAlpha = clamp(combinedAlpha * outlineColor.a * pulse * 1.38, 0.0, 1.0);
        fragColor = vec4(richColor, finalAlpha);
        return;
    }

    vec4 original = texture(texture0, texCoord);
    if (original.a > 0.5) {
        discard;
    }

    vec2 texelSize = 1.0 / resolution;
    float checkRadius = thickness * 0.15 * texelSize.x * 6.0;

    float quickCheck = texture(texture0, texCoord + vec2(checkRadius, 0)).a
                     + texture(texture0, texCoord - vec2(checkRadius, 0)).a
                     + texture(texture0, texCoord + vec2(0, checkRadius)).a
                     + texture(texture0, texCoord - vec2(0, checkRadius)).a;

    if (quickCheck < 0.01) {
        discard;
    }

    float objectScale = 1.0;
    if (quickCheck > 0.1) {
        objectScale = detectObjectScale(texCoord);
    }

    vec4 blurH = boxBlur(texCoord, vec2(1.0, 0.0), objectScale);
    vec4 blurV = boxBlur(texCoord, vec2(0.0, 1.0), objectScale);
    vec4 glow;

    if (quality > 0.5) {
        vec4 blurD1 = boxBlur(texCoord, normalize(vec2(1.0, 1.0)), objectScale);
        vec4 blurD2 = boxBlur(texCoord, normalize(vec2(1.0, -1.0)), objectScale);
        glow = (blurH + blurV + blurD1 + blurD2) * 0.25;
    } else {
        glow = (blurH + blurV) * 0.5;
    }

    vec4 result = glow * 2.2 + original * 1.1;

    if (outlineColor.a > 0.01) {
        float alpha = result.a;
        result.rgb = mix(result.rgb, outlineColor.rgb, 0.8);
        result.a = alpha * outlineColor.a;
    }

    result = min(result, vec4(1.0));
    if (result.a < 0.01) {
        discard;
    }

    fragColor = result;
}
