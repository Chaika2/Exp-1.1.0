#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D SourceTexture;
uniform vec2 direction;

void main() {
    float value = texture(SourceTexture, uv).r * 0.22702703;
    value += texture(SourceTexture, clamp(uv + direction, vec2(0.0), vec2(1.0))).r * 0.19459459;
    value += texture(SourceTexture, clamp(uv - direction, vec2(0.0), vec2(1.0))).r * 0.19459459;
    value += texture(SourceTexture, clamp(uv + direction * 2.0, vec2(0.0), vec2(1.0))).r * 0.12162162;
    value += texture(SourceTexture, clamp(uv - direction * 2.0, vec2(0.0), vec2(1.0))).r * 0.12162162;
    value += texture(SourceTexture, clamp(uv + direction * 3.0, vec2(0.0), vec2(1.0))).r * 0.05405405;
    value += texture(SourceTexture, clamp(uv - direction * 3.0, vec2(0.0), vec2(1.0))).r * 0.05405405;
    value += texture(SourceTexture, clamp(uv + direction * 4.0, vec2(0.0), vec2(1.0))).r * 0.01621622;
    value += texture(SourceTexture, clamp(uv - direction * 4.0, vec2(0.0), vec2(1.0))).r * 0.01621622;
    outColor = vec4(value, value, value, value);
}
