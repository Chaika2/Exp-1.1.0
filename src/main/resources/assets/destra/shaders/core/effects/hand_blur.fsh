#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D ColorTexture;
uniform sampler2D DepthTexture;
uniform sampler2D BlurTexture;
uniform vec4 baseColor;
uniform float effectAlpha;
uniform float blurStrength;
uniform float blurSaturation;
uniform float tintIntensity;
uniform vec2 resolution;

float handMask(vec2 p) {
    vec2 px = 1.0 / max(resolution, vec2(1.0));
    float d = texture(DepthTexture, p).r;
    d = min(d, texture(DepthTexture, p + vec2(px.x, 0.0)).r);
    d = min(d, texture(DepthTexture, p - vec2(px.x, 0.0)).r);
    d = min(d, texture(DepthTexture, p + vec2(0.0, px.y)).r);
    d = min(d, texture(DepthTexture, p - vec2(0.0, px.y)).r);
    return smoothstep(0.995, 0.978, d);
}

void main() {
    float mask = handMask(uv);
    if (mask <= 0.002) discard;

    vec3 original = texture(ColorTexture, uv).rgb;
    vec3 blurred = texture(BlurTexture, uv).rgb;
    float luma = dot(blurred, vec3(0.2126, 0.7152, 0.0722));
    blurred = mix(vec3(luma), blurred, max(0.0, blurSaturation));
    vec3 glass = mix(original, blurred, clamp(blurStrength, 0.0, 1.0));
    glass = mix(glass, baseColor.rgb * (0.72 + luma * 0.42), clamp(tintIntensity, 0.0, 1.0));

    vec2 px = 1.0 / max(resolution, vec2(1.0));
    float inner = handMask(uv + vec2(px.x * 2.0, px.y * 2.0));
    float edge = clamp(mask - inner * 0.65, 0.0, 1.0);
    glass += baseColor.rgb * edge * 0.18;
    outColor = vec4(clamp(glass, 0.0, 1.0), mask * effectAlpha);
}
