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

float gyroid(vec3 p) {
    return dot(cos(p), sin(p.yzx));
}

float fbm(vec3 p, float localTime) {
    float result = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; ++i) {
        p += result * 0.08;
        p.z += localTime * 0.12;
        result += abs(gyroid(p / a) * a);
        a /= 1.75;
    }
    return result;
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

    vec2 localUv = (uv * 2.0 - 1.0) * 1.9;
    vec3 ray = normalize(vec3(localUv, 0.35));
    vec3 samplePos = ray * 2.8;
    float localTime = time * 0.9;
    float field = fbm(samplePos, localTime);
    float detail = fbm(samplePos + vec3(1.3, -0.8, 0.6), localTime * 0.82);
    float maskField = smoothstep(0.18, 0.92, field);
    float veins = smoothstep(0.18, 0.86, abs(sin(field * 3.1 + detail * 1.6 - localTime * 0.55)));

    vec3 cLow = baseColor.rgb * 0.18;
    vec3 cMid = clamp(baseColor.rgb * 1.02 + vec3(0.05), 0.0, 1.0);
    vec3 cHigh = mix(baseColor.rgb, vec3(1.0), 0.20);
    vec3 cAccent = clamp(vec3(baseColor.r * 0.55 + 0.08, baseColor.g * 0.34 + 0.06, baseColor.b * 1.10 + 0.10), 0.0, 1.0);

    vec3 effect = mix(cLow, cMid, maskField);
    effect = mix(effect, cHigh, veins * 0.42);
    effect = mix(effect, cAccent, smoothstep(0.45, 1.0, detail) * 0.32);
    effect += cMid * (detail * 0.05);
    effect = clamp(effect, 0.0, 1.8);

    vec3 finalColor = mix(originalColor.rgb, effect, effectAlpha);
    outColor = vec4(finalColor, mask);
}
