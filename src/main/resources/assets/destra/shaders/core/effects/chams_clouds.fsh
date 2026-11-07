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

const mat2 M = mat2(1.6, 1.2, -1.2, 1.6);
const float CLOUD_SCALE = 1.1;
const float CLOUD_SPEED = 0.03;
const float CLOUD_DARK = 0.5;
const float CLOUD_LIGHT = 0.3;
const float CLOUD_COVER = 0.2;
const float CLOUD_ALPHA = 8.0;
const float SKY_TINT = 0.5;

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

float destraNoise(vec2 p) {
    vec2 ip = floor(p);
    vec2 u = fract(p);
    u = u * u * (3.0 - 2.0 * u);
    float res = mix(
        mix(rand(ip), rand(ip + vec2(1.0, 0.0)), u.x),
        mix(rand(ip + vec2(0.0, 1.0)), rand(ip + vec2(1.0, 1.0)), u.x),
        u.y
    );
    return res * res;
}

float fbm(vec2 n, float localTime) {
    float total = 0.0;
    float amplitude = 0.1;
    for (int i = 0; i < 7; i++) {
        total += destraNoise(n) * amplitude;
        n = M * n;
        n += vec2(localTime * 0.02, localTime * 0.015);
        amplitude *= 0.4;
    }
    return total;
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

    vec2 p = uv;
    vec2 localUv = (uv * 2.0 - 1.0) * vec2(1.25, 1.0);
    float localTime = time * CLOUD_SPEED;
    float q = fbm(localUv * CLOUD_SCALE * 0.5, localTime);

    float r = 0.0;
    vec2 uvRidged = localUv * CLOUD_SCALE;
    uvRidged -= q - localTime;
    float weight = 0.8;
    for (int i = 0; i < 4; i++) {
        r += abs(weight * destraNoise(uvRidged));
        uvRidged = M * uvRidged + localTime;
        weight *= 0.7;
    }

    float f = 0.0;
    vec2 uvShape = localUv * CLOUD_SCALE;
    uvShape -= q - localTime;
    weight = 0.7;
    for (int i = 0; i < 6; i++) {
        f += weight * destraNoise(uvShape);
        uvShape = M * uvShape + localTime;
        weight *= 0.6;
    }
    f *= r + f;

    float c = 0.0;
    float colourTime = time * CLOUD_SPEED * 2.0;
    vec2 uvColour = localUv * CLOUD_SCALE * 2.0;
    uvColour -= q - colourTime;
    weight = 0.4;
    for (int i = 0; i < 4; i++) {
        c += weight * destraNoise(uvColour);
        uvColour = M * uvColour + colourTime;
        weight *= 0.6;
    }

    float c1 = 0.0;
    float ridgeTime = time * CLOUD_SPEED * 3.0;
    vec2 uvRidgeColour = localUv * CLOUD_SCALE * 3.0;
    uvRidgeColour -= q - ridgeTime;
    weight = 0.4;
    for (int i = 0; i < 2; i++) {
        c1 += abs(weight * destraNoise(uvRidgeColour));
        uvRidgeColour = M * uvRidgeColour + ridgeTime;
        weight *= 0.6;
    }
    c += c1;

    vec3 cLow = mix(baseColor.rgb * 0.22, baseColor.rgb * 0.62 + vec3(0.06, 0.08, 0.12), 0.6);
    vec3 cHigh = mix(baseColor.rgb * 1.05, mix(baseColor.rgb, vec3(1.0), 0.24), 0.5);
    vec3 skyColour = mix(cLow, cHigh, p.y);

    vec3 cloudBase = mix(baseColor.rgb, vec3(1.0), 0.42);
    vec3 cloudColour = cloudBase * clamp(CLOUD_DARK + CLOUD_LIGHT * c, 0.0, 1.0);

    f = CLOUD_COVER + CLOUD_ALPHA * f * r;
    vec3 tintedSky = clamp(SKY_TINT * skyColour + cloudColour, 0.0, 1.0);
    vec3 effect = mix(skyColour, tintedSky, clamp(f + c, 0.0, 1.0));
    effect = clamp(effect, 0.0, 1.8);

    vec3 finalColor = mix(originalColor.rgb, effect, effectAlpha);
    outColor = vec4(finalColor, mask);
}
