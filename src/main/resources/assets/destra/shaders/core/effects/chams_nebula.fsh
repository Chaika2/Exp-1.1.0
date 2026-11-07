#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D ColorTexture;
uniform sampler2D MaskTexture;
uniform sampler2D SceneDepthTexture;
uniform sampler2D BodyDepthTexture;
uniform sampler2D ArmorMaskTexture;
uniform float time;
uniform vec4 baseColor;
uniform float effectAlpha;

#define NUM_OCTAVES 5

float random(vec2 pos) {
    return fract(sin(dot(pos.xy, vec2(13.9898, 78.233))) * 43758.5453123);
}

float destraNoise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i + vec2(0.0, 0.0));
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos, float t) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));

    for (int i = 0; i < NUM_OCTAVES; i++) {
        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;
        v += a * destraNoise(pos + dir * t * 0.3);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }

    return v;
}

float entityMaskValue(vec2 coord) {
    float mask = texture(MaskTexture, coord).a;
    vec2 texelSize = 1.0 / textureSize(SceneDepthTexture, 0);
    float sceneDepth = texture(SceneDepthTexture, coord).r;
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(-texelSize.x, 0.0)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(texelSize.x, 0.0)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(0.0, -texelSize.y)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(0.0, texelSize.y)).r);
    float bodyDepth = texture(BodyDepthTexture, coord).r;
    float depthDelta = max(bodyDepth - sceneDepth, 0.0);
    float visibleBody = 1.0 - smoothstep(0.000003, 0.000035, depthDelta);
    float armorMask = smoothstep(0.01, 0.08, texture(ArmorMaskTexture, coord).a);
    return smoothstep(0.01, 0.08, mask) * visibleBody * (1.0 - armorMask);
}

void main() {
    vec4 originalColor = texture(ColorTexture, uv);
    float mask = entityMaskValue(uv);
    if (mask < 0.01) {
        discard;
    }

    vec2 p = (uv * 2.0 - 1.0) * 3.0;
    float t = time;

    vec2 q = vec2(0.0);
    q.x = fbm(p + vec2(0.0, 0.0), t * 0.8);
    q.y = fbm(p + vec2(1.0, 1.0), t * 0.6);

    vec2 r = vec2(0.0);
    r.x = fbm(p + q + vec2(1.7, 1.2), t * 0.7);
    r.y = fbm(p + q + vec2(8.3, 2.8), t * 0.9);

    float f = fbm(p + r, t);

    vec3 color1 = baseColor.rgb * 1.4;
    vec3 color2 = baseColor.rgb * 0.5;
    vec3 color3 = vec3(baseColor.r * 0.4, baseColor.g * 0.3, baseColor.b * 1.4);

    vec3 effect = mix(color1, color2, clamp((f * f) * 4.0, 0.0, 1.0));
    effect = mix(effect, color1, clamp(length(q), 0.0, 1.0));
    effect = mix(effect, color3, clamp(abs(r.x), 0.0, 1.0));
    effect = (f * f * f + 0.6 * f * f + 0.5 * f) * effect;
    effect = clamp(effect * 1.8, 0.0, 1.0);

    vec3 finalColor = mix(originalColor.rgb, effect, effectAlpha);
    outColor = vec4(finalColor, mask);
}
