#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;

uniform vec2 size;
uniform float round;
uniform float thickness;
uniform vec4 colorTopLeft;
uniform vec4 colorTopRight;
uniform vec4 colorBottomRight;
uniform vec4 colorBottomLeft;

out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - radius;
}

vec4 getGradientColor(vec2 fragPos) {
    vec2 normalized = fragPos / size;
    vec4 topColor = mix(colorTopLeft, colorTopRight, normalized.x);
    vec4 bottomColor = mix(colorBottomLeft, colorBottomRight, normalized.x);
    return mix(topColor, bottomColor, normalized.y);
}

void main() {
    vec2 tex = FragCoord;
    vec2 local = vec2(tex.x * size.x, (1.0 - tex.y) * size.y);
    vec2 halfSize = size * 0.5;
    float radius = clamp(round, 0.0, min(halfSize.x, halfSize.y));
    float distance = roundedBoxSDF(local - halfSize, halfSize, radius);
    float edge = clamp(fwidth(distance), 0.65, 1.25);

    float outerAlpha = 1.0 - smoothstep(-edge, edge, distance);
    float innerAlpha = 1.0 - smoothstep(-thickness - edge, -thickness + edge, distance);
    float alphaMultiplier = clamp(outerAlpha - innerAlpha, 0.0, 1.0);

    if (alphaMultiplier <= 0.01) {
        discard;
        return;
    }

    vec4 gradientColor = getGradientColor(tex * size);

    fragColor = vec4(gradientColor.rgb, gradientColor.a * alphaMultiplier);
}
