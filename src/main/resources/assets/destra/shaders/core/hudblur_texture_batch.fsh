#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec2 GlobalPos;
in float CornerRadius;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 PixelScale;
uniform float softness;

out vec4 OutColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, vec4 radius) {
    float cornerRadius = p.x > 0.0
            ? (p.y > 0.0 ? radius.y : radius.z)
            : (p.y > 0.0 ? radius.x : radius.w);
    vec2 q = abs(p) - halfSize + cornerRadius;
    return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - cornerRadius;
}

float rectAlpha(vec2 size, vec2 coord, float radius, float softness) {
    vec2 local = vec2(coord.x * size.x, (1.0 - coord.y) * size.y);
    vec2 halfSize = size * 0.5;
    vec4 clampedRadius = min(vec4(radius), vec4(min(halfSize.x, halfSize.y)));
    float distance = roundedBoxSDF(local - halfSize, halfSize, clampedRadius);
    float edge = clamp(fwidth(distance), 0.65, 1.25) * max(softness, 0.001);
    return 1.0 - smoothstep(-edge * 0.5, edge * 0.5, distance);
}

void main() {
    vec2 dxGlobal = dFdx(GlobalPos);
    vec2 dyGlobal = dFdy(GlobalPos);
    vec2 dxLocal = dFdx(FragCoord);
    vec2 dyLocal = dFdy(FragCoord);
    float width = abs(dxGlobal.x) / max(abs(dxLocal.x), 0.0001);
    float height = abs(dyGlobal.y) / max(abs(dyLocal.y), 0.0001);
    vec2 size = max(vec2(width, height) * PixelScale, vec2(1.0));
    float radius = clamp(CornerRadius, 0.0, min(size.x, size.y) * 0.5);
    float alpha = rectAlpha(size, FragCoord, radius, softness);
    vec4 color = vec4(1.0, 1.0, 1.0, alpha) * texture(Sampler0, TexCoord) * FragColor;

    if (color.a == 0.0) {
        discard;
    }

    OutColor = color;
}
