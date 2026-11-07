#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D DepthTexture;

void main() {
    float depth = texture(DepthTexture, clamp(uv, vec2(0.0), vec2(1.0))).r;
    float mask = smoothstep(0.995, 0.975, depth);
    outColor = vec4(mask, mask, mask, mask);
}
