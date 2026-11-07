#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D SceneTexture;
uniform sampler2D DepthTexture;
uniform mat4 InvViewProjection;
uniform vec2 Resolution;
uniform int WaveCount;
uniform vec4 Wave0;
uniform vec4 Wave1;
uniform vec4 Wave2;
uniform vec4 Wave3;
uniform vec4 Params0;
uniform vec4 Params1;
uniform vec4 Params2;
uniform vec4 Params3;
uniform vec4 GlowColor;
uniform float GlowStrength;
uniform float GlowSpread;
uniform int EffectMode;

struct WaveResult {
    vec2 offset;
    float highlight;
};

vec3 reconstructWorldPosition(vec2 coordinate, float depth) {
    vec4 clip = vec4(coordinate * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 world = InvViewProjection * clip;
    return world.xyz / max(abs(world.w), 0.000001);
}

WaveResult evaluateWave(vec3 worldPosition, vec4 wave, vec4 params) {
    vec2 planarDelta = worldPosition.xz - wave.xz;
    float planarDistance = length(planarDelta);
    float width = max(params.x, 0.05);
    float signedBand = (planarDistance - wave.w) / width;

    float verticalMask = 1.0 - smoothstep(0.65, 2.35, abs(worldPosition.y - wave.y));
    float crestEnvelope = exp(-signedBand * signedBand * 2.15) * verticalMask * params.z;

    vec2 distanceGradient = vec2(dFdx(planarDistance), dFdy(planarDistance));
    float gradientLength = length(distanceGradient);
    vec2 screenNormal = gradientLength > 0.000001
            ? distanceGradient / gradientLength
            : vec2(0.0);

    float trail = (wave.w - planarDistance) / width;
    float trailingWave = step(0.0, trail)
            * exp(-trail * 0.42)
            * sin(trail * 3.65)
            * verticalMask
            * params.z;
    vec2 pixelSize = 1.0 / max(Resolution, vec2(1.0));

    WaveResult result;
    float lens = EffectMode == 1 ? trailingWave * 1.15 : 0.0;
    result.offset = screenNormal * pixelSize * params.y * lens;
    float glowSpread = max(GlowSpread, 0.25);
    float hotCore = exp(-signedBand * signedBand * 10.0) * verticalMask * params.z;
    float softHalo = exp(-signedBand * signedBand * (1.45 / glowSpread)) * verticalMask * params.z;
    result.highlight = EffectMode == 1 ? 0.0 : hotCore * 1.3 + softHalo * 0.5;
    return result;
}

void accumulateWave(inout vec2 offset, inout float highlight, vec3 worldPosition,
                    vec4 wave, vec4 params) {
    WaveResult result = evaluateWave(worldPosition, wave, params);
    offset += result.offset;
    highlight = max(highlight, result.highlight);
}

void main() {
    float depth = texture(DepthTexture, uv).r;
    vec4 scene = texture(SceneTexture, uv);
    if (depth >= 0.99995 || WaveCount <= 0) {
        outColor = scene;
        return;
    }

    vec3 worldPosition = reconstructWorldPosition(uv, depth);
    vec2 distortion = vec2(0.0);
    float highlight = 0.0;

    if (WaveCount > 0) accumulateWave(distortion, highlight, worldPosition, Wave0, Params0);
    if (WaveCount > 1) accumulateWave(distortion, highlight, worldPosition, Wave1, Params1);
    if (WaveCount > 2) accumulateWave(distortion, highlight, worldPosition, Wave2, Params2);
    if (WaveCount > 3) accumulateWave(distortion, highlight, worldPosition, Wave3, Params3);

    vec2 distortedUv = clamp(uv + distortion, vec2(0.001), vec2(0.999));
    vec4 distortedScene = texture(SceneTexture, distortedUv);
    vec3 luminousColor = mix(GlowColor.rgb, vec3(1.0), clamp(highlight * 0.16, 0.0, 0.28));
    distortedScene.rgb += luminousColor * highlight * GlowStrength * GlowColor.a;
    outColor = vec4(distortedScene.rgb, scene.a);
}
