#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D DepthBeforeTexture;
uniform sampler2D DepthAfterTexture;
uniform vec2 resolution;

float depthDeltaMask(vec2 coord) {
    float beforeDepth = texture(DepthBeforeTexture, coord).r;
    float afterDepth = texture(DepthAfterTexture, coord).r;
    return smoothstep(0.000015, 0.00016, beforeDepth - afterDepth);
}

void main() {
    vec2 pixel = 1.0 / max(resolution, vec2(1.0));
    float mask = depthDeltaMask(uv);
    mask = max(mask, depthDeltaMask(clamp(uv + vec2(pixel.x, 0.0), vec2(0.0), vec2(1.0))) * 0.88);
    mask = max(mask, depthDeltaMask(clamp(uv - vec2(pixel.x, 0.0), vec2(0.0), vec2(1.0))) * 0.88);
    mask = max(mask, depthDeltaMask(clamp(uv + vec2(0.0, pixel.y), vec2(0.0), vec2(1.0))) * 0.88);
    mask = max(mask, depthDeltaMask(clamp(uv - vec2(0.0, pixel.y), vec2(0.0), vec2(1.0))) * 0.88);
    float fakeDepth = mix(1.0, 0.0, clamp(mask, 0.0, 1.0));
    outColor = vec4(fakeDepth, fakeDepth, fakeDepth, 1.0);
}
