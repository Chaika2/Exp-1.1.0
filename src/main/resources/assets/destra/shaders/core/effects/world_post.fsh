#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D SceneTexture;
uniform sampler2D DepthTexture;
uniform sampler2D BlurTexture;
uniform vec2 resolution;
uniform float nearPlane;
uniform float farPlane;
uniform float saturation;
uniform float fogBlur;
uniform float fogDistance;
uniform float fogSaturation;
uniform int fogClientColor;
uniform vec3 fogColor1;
uniform vec3 fogColor2;
uniform vec3 fogColor3;
uniform vec3 fogColor4;
uniform float fragDesaturation;
uniform int colorGradeEnabled;
uniform vec3 colorGradeColor;
uniform float colorGradeIntensity;
uniform float colorGradeBrightness;
uniform int colorGradeVignette;
uniform float colorGradeVignetteStrength;
uniform int outlineEnabled;
uniform float outlineThickness;
uniform float outlineOpacity;
uniform vec3 outlineColor;
uniform float tintStrength;
uniform float tintWidth;
uniform vec3 tintColor;
uniform mat4 invViewMat;
uniform mat4 invProjMat;
uniform vec3 cameraPos;
uniform vec3 scanCenter;
uniform float scanRadius;
uniform float scanWidth;
uniform float scanStrength;
uniform vec3 scanColor;

float linearDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * nearPlane * farPlane)
            / max(0.0001, farPlane + nearPlane - z * (farPlane - nearPlane));
}

vec3 reconstructWorldPosition(vec2 coord, float depth) {
    float clipDepth = depth * 2.0 - 1.0;
    vec4 clipPosition = vec4(coord * 2.0 - 1.0, clipDepth, 1.0);
    vec4 viewPosition = invProjMat * clipPosition;
    viewPosition /= max(abs(viewPosition.w), 1e-6);
    vec4 worldOffset = invViewMat * vec4(viewPosition.xyz, 1.0);
    return cameraPos + worldOffset.xyz;
}

void main() {
    vec2 sampleUv = uv;
    vec3 color = texture(SceneTexture, sampleUv).rgb;
    float depth = texture(DepthTexture, sampleUv).r;
    float normalizedDepth = clamp(linearDepth(depth) / max(farPlane, 1.0), 0.0, 1.0);

    if (fogBlur > 0.001) {
        float fog = smoothstep(fogDistance, min(1.0, fogDistance + 0.1), normalizedDepth) * fogBlur;
        vec3 blurred = texture(BlurTexture, sampleUv).rgb;
        vec3 fogTarget = blurred;
        if (fogClientColor == 1) {
            vec3 gradient = mix(mix(fogColor1, fogColor2, sampleUv.y),
                                mix(fogColor3, fogColor4, sampleUv.y), sampleUv.x);
            float dither = mix(0.5 / 255.0, -0.5 / 255.0,
                    fract(sin(dot(sampleUv, vec2(12.9898, 78.233))) * 43758.5453));
            fogTarget = mix(gradient + dither, blurred, fogSaturation);
        }
        color = mix(color, fogTarget, smoothstep(0.0, 1.0, fog));
    }

    float edgeDistance = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float tintRange = clamp(tintWidth / max(min(resolution.x, resolution.y), 1.0), 0.01, 0.45);
    float tintEdge = 1.0 - smoothstep(0.0, tintRange, edgeDistance);
    tintEdge *= tintEdge;
    color = mix(color, tintColor, clamp(tintEdge * tintStrength * 0.8, 0.0, 0.9));

    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luminance), color, max(0.0, 1.0 + saturation));
    if (saturation > 0.0) {
        color = mix(color, color * color * (3.0 - 2.0 * color), saturation * 0.22);
    }

    float fragGray = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(color, vec3(fragGray), clamp(fragDesaturation, 0.0, 1.0));

    if (colorGradeEnabled == 1) {
        color = mix(color, colorGradeColor, clamp(colorGradeIntensity, 0.0, 0.95));
        if (colorGradeBrightness < 0.0) {
            color = mix(color, vec3(0.0), clamp(-colorGradeBrightness, 0.0, 0.8));
        } else {
            color = mix(color, vec3(1.0), clamp(colorGradeBrightness, 0.0, 0.8));
        }
        if (colorGradeVignette == 1) {
            vec2 centered = sampleUv * 2.0 - 1.0;
            centered.x *= resolution.x / max(resolution.y, 1.0);
            float vignette = smoothstep(0.25, 1.25, length(centered));
            color *= 1.0 - vignette * clamp(colorGradeVignetteStrength, 0.0, 0.95);
        }
    }

    if (outlineEnabled == 1) {
        vec2 pixel = (1.0 / max(resolution, vec2(1.0))) * max(0.5, outlineThickness);
        float center = normalizedDepth;
        float edge = 0.0;
        edge = max(edge, abs(center - linearDepth(texture(DepthTexture, sampleUv + vec2(pixel.x, 0.0)).r) / farPlane));
        edge = max(edge, abs(center - linearDepth(texture(DepthTexture, sampleUv - vec2(pixel.x, 0.0)).r) / farPlane));
        edge = max(edge, abs(center - linearDepth(texture(DepthTexture, sampleUv + vec2(0.0, pixel.y)).r) / farPlane));
        edge = max(edge, abs(center - linearDepth(texture(DepthTexture, sampleUv - vec2(0.0, pixel.y)).r) / farPlane));
        edge = smoothstep(0.00035, 0.0045, edge) * step(depth, 0.99995);
        color = mix(color, outlineColor, clamp(edge * outlineOpacity, 0.0, 1.0));
    }

    if (scanStrength > 0.001 && scanRadius > 0.001 && depth < 0.99995) {
        vec3 worldPosition = reconstructWorldPosition(sampleUv, depth);
        float distanceToCenter = distance(worldPosition, scanCenter);
        float signedDistance = scanRadius - distanceToCenter;
        float safeWidth = max(scanWidth, 0.05);
        float leadingGlow = exp(-pow(abs(signedDistance) / (safeWidth * 0.30), 2.0) * 1.45);
        float softGlow = exp(-pow(abs(signedDistance) / safeWidth, 2.0) * 1.65);
        float afterGlow = step(0.0, signedDistance)
                * exp(-max(signedDistance, 0.0) / (safeWidth * 1.65)) * 0.34;
        float surfaceVariation = 0.93 + 0.07 * sin(worldPosition.y * 1.8 + worldPosition.x * 0.13 + worldPosition.z * 0.11);
        float scanGlow = clamp((softGlow * 0.48 + leadingGlow * 0.88 + afterGlow) * surfaceVariation, 0.0, 1.35);
        vec3 illuminated = scanColor * (1.08 + leadingGlow * 0.40);
        color += illuminated * scanGlow * scanStrength * (1.0 - color * 0.42);
    }

    outColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
