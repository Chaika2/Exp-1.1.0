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

const mat2 MTX = mat2(0.80, 0.60, -0.60, 0.80);

float colormapRed(float x) {
    if (x < 0.0) return 54.0 / 255.0;
    if (x < 20049.0 / 82979.0) return (829.79 * x + 54.51) / 255.0;
    return 1.0;
}

float colormapGreen(float x) {
    if (x < 20049.0 / 82979.0) return 0.0;
    if (x < 327013.0 / 810990.0) return (8546482679670.0 / 10875673217.0 * x - 2064961390770.0 / 10875673217.0) / 255.0;
    if (x <= 1.0) return (103806720.0 / 483977.0 * x + 19607415.0 / 483977.0) / 255.0;
    return 1.0;
}

float colormapBlue(float x) {
    if (x < 0.0) return 54.0 / 255.0;
    if (x < 7249.0 / 82979.0) return (829.79 * x + 54.51) / 255.0;
    if (x < 20049.0 / 82979.0) return 127.0 / 255.0;
    if (x < 327013.0 / 810990.0) return (792.0224934136139 * x - 64.36479073560233) / 255.0;
    return 1.0;
}

vec3 colormap(float x) {
    return vec3(colormapRed(x), colormapGreen(x), colormapBlue(x));
}

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

float fbm(vec2 p, float localTime) {
    float f = 0.0;
    f += 0.500000 * destraNoise(p + localTime); p = MTX * p * 2.02;
    f += 0.031250 * destraNoise(p);            p = MTX * p * 2.01;
    f += 0.250000 * destraNoise(p);            p = MTX * p * 2.03;
    f += 0.125000 * destraNoise(p);            p = MTX * p * 2.01;
    f += 0.062500 * destraNoise(p);            p = MTX * p * 2.04;
    f += 0.015625 * destraNoise(p + sin(localTime));
    return f / 0.96875;
}

float pattern(vec2 p, float localTime) {
    return fbm(p + fbm(p + fbm(p, localTime), localTime), localTime);
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

    vec2 localUv = (uv * 2.0 - 1.0) * 1.35;
    float shade = pattern(localUv * 1.25, time * 0.9);
    vec3 heat = colormap(shade);

    float warm = heat.r;
    float mid = heat.g;
    float cool = heat.b;

    vec3 c1 = baseColor.rgb * 0.16;
    vec3 c2 = clamp(baseColor.rgb * 1.08, 0.0, 1.0);
    vec3 c3 = mix(baseColor.rgb, vec3(1.0), 0.18);
    vec3 cAccent = clamp(vec3(baseColor.r * 0.62 + 0.06, baseColor.g * 0.42 + 0.05, baseColor.b * 1.02 + 0.08), 0.0, 1.0);

    vec3 effect = mix(c1, c2, warm * 0.92);
    effect = mix(effect, c3, mid * 0.32);
    effect = mix(effect, cAccent, cool * 0.22);
    effect += baseColor.rgb * (shade * 0.10);
    effect = clamp(effect, 0.0, 1.8);

    vec3 finalColor = mix(originalColor.rgb, effect, effectAlpha);
    outColor = vec4(finalColor, mask);
}
