#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 size;
uniform vec4 radius;
uniform float softness;

out vec4 OutColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, vec4 radius) {
    float cornerRadius = p.x > 0.0
            ? (p.y > 0.0 ? radius.y : radius.z)
            : (p.y > 0.0 ? radius.x : radius.w);
    vec2 q = abs(p) - halfSize + cornerRadius;
    return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - cornerRadius;
}

float rectAlpha(vec2 size, vec2 coord, vec4 radius, float softness) {
    vec2 local = vec2(coord.x * size.x, (1.0 - coord.y) * size.y);
    vec2 halfSize = size * 0.5;
    vec4 clampedRadius = min(radius, vec4(min(halfSize.x, halfSize.y)));
    float distance = roundedBoxSDF(local - halfSize, halfSize, clampedRadius);
    float edge = clamp(fwidth(distance), 0.65, 1.25) * max(softness, 0.001);
    return 1.0 - smoothstep(-edge * 0.5, edge * 0.5, distance);
}

void main() {
    float alpha = rectAlpha(size, FragCoord, radius, softness);
    vec4 color = vec4(1.0, 1.0, 1.0, alpha) * texture(Sampler0, TexCoord) * FragColor;

    if (color.a == 0.0) {
        discard;
    }

    OutColor = color;
}
