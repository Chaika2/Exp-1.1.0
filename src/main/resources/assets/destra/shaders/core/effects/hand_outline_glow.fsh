#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D DepthTexture;
uniform sampler2D BlurredTexture;
uniform vec2 resolution;
uniform vec3 effectColor;
uniform int outlineEnabled;
uniform float outlineThickness;
uniform int glowEnabled;
uniform float glowStrength;
uniform float effectAlpha;

float handMask(vec2 coord) {
    float depth = texture(DepthTexture, clamp(coord, vec2(0.0), vec2(1.0))).r;
    return smoothstep(0.995, 0.975, depth);
}

void main() {
    vec2 pixel = 1.0 / max(resolution, vec2(1.0));
    float center = handMask(uv);
    vec2 edgeOffset = pixel * max(0.5, outlineThickness);
    float outlineMask = 0.0;
    outlineMask = max(outlineMask, handMask(uv + vec2(edgeOffset.x, 0.0)));
    outlineMask = max(outlineMask, handMask(uv - vec2(edgeOffset.x, 0.0)));
    outlineMask = max(outlineMask, handMask(uv + vec2(0.0, edgeOffset.y)));
    outlineMask = max(outlineMask, handMask(uv - vec2(0.0, edgeOffset.y)));
    outlineMask = max(outlineMask, handMask(uv + edgeOffset));
    outlineMask = max(outlineMask, handMask(uv - edgeOffset));
    outlineMask = max(outlineMask, handMask(uv + vec2(edgeOffset.x, -edgeOffset.y)));
    outlineMask = max(outlineMask, handMask(uv + vec2(-edgeOffset.x, edgeOffset.y)));

    float blurred = texture(BlurredTexture, clamp(uv, vec2(0.0), vec2(1.0))).r;
    float outside = 1.0 - center;
    float outline = outlineEnabled == 1 ? max(0.0, outlineMask - center) : 0.0;
    float softHalo = pow(clamp(blurred, 0.0, 1.0), 1.12) * outside;
    float nearHalo = smoothstep(0.035, 0.42, blurred) * outside;
    float glow = glowEnabled == 1
            ? (softHalo * 0.72 + nearHalo * 0.10) * glowStrength
            : 0.0;

    float alpha = clamp(max(outline * 0.92, glow) * effectAlpha, 0.0, 0.94);
    if (alpha <= 0.002) {
        discard;
    }

    float edgeLight = outline * 0.15 + nearHalo * 0.08;
    vec3 color = effectColor * (1.0 + edgeLight);
    outColor = vec4(color, alpha);
}
