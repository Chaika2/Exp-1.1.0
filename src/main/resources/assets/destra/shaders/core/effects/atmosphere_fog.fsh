#version 330 core

layout(location = 0) out vec4 fragColor;

uniform sampler2D u_ScreenTexture;
uniform sampler2D u_DepthTexture;
uniform mat4 u_InverseProjectionMatrix;
uniform mat4 u_InverseViewMatrix;
uniform vec3 u_CameraPos;
uniform vec2 u_Resolution;
uniform float u_Time;
uniform vec3 u_SunDirection;
uniform vec3 u_SunScreen;
uniform float u_FogDensity;
uniform float u_FogMinHeight;
uniform float u_FogMaxHeight;
uniform float u_FogStart;
uniform float u_FogNoise;
uniform float u_FogScale;
uniform float u_FogSpeed;
uniform float u_Softness;
uniform float u_SkyGlow;
uniform float u_ViewDistance;
uniform vec3 u_PaletteZenith;
uniform vec3 u_PaletteHorizonWarm;
uniform vec3 u_PaletteHorizonCool;
uniform vec3 u_PaletteFogWarm;
uniform vec3 u_PaletteFogCool;
uniform vec3 u_PaletteRay;
uniform float u_GodRays;
uniform int u_RayStyle;
uniform float u_RayLength;
uniform float u_RayWidth;
uniform float u_RayCount;
uniform float u_RayMotion;
uniform vec3 u_RayColor;
uniform float u_WhiteRayMix;

const vec2 POISSON[8] = vec2[8](
    vec2(-0.326, -0.406), vec2(-0.840, -0.074), vec2(-0.696, 0.457), vec2(-0.203, 0.621),
    vec2(0.962, -0.195), vec2(0.473, -0.480), vec2(0.519, 0.767), vec2(0.185, -0.893)
);

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

float hash12(vec2 point) {
    vec3 p = fract(vec3(point.xyx) * 0.1031);
    p += dot(p, p.yzx + vec3(33.33));
    return fract((p.x + p.y) * p.z);
}

float valueNoise(vec3 point) {
    vec3 cell = floor(point);
    vec3 local = fract(point);
    local = local * local * (3.0 - 2.0 * local);

    float n000 = hash12(cell.xy + cell.z * 19.19);
    float n100 = hash12(cell.xy + vec2(1.0, 0.0) + cell.z * 19.19);
    float n010 = hash12(cell.xy + vec2(0.0, 1.0) + cell.z * 19.19);
    float n110 = hash12(cell.xy + vec2(1.0, 1.0) + cell.z * 19.19);
    float n001 = hash12(cell.xy + (cell.z + 1.0) * 19.19);
    float n101 = hash12(cell.xy + vec2(1.0, 0.0) + (cell.z + 1.0) * 19.19);
    float n011 = hash12(cell.xy + vec2(0.0, 1.0) + (cell.z + 1.0) * 19.19);
    float n111 = hash12(cell.xy + vec2(1.0, 1.0) + (cell.z + 1.0) * 19.19);

    float nearLayer = mix(mix(n000, n100, local.x), mix(n010, n110, local.x), local.y);
    float farLayer = mix(mix(n001, n101, local.x), mix(n011, n111, local.x), local.y);
    return mix(nearLayer, farLayer, local.z);
}

float fogTurbulence(vec3 point) {
    float scale = mix(0.026, 0.070, saturate((u_FogScale - 0.2) / 1.8));
    vec3 drift = vec3(u_Time * 0.55, u_Time * 0.08, u_Time * 0.31) * u_FogSpeed;
    float macro = valueNoise(point * scale + drift);
    float detail = valueNoise(point * scale * 2.13 - drift * 0.47 + vec3(11.7, 4.1, 23.9));
    return mix(1.0, mix(0.62, 1.38, macro * 0.72 + detail * 0.28), u_FogNoise);
}

vec3 worldPosition(vec2 uv, float depth) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = u_InverseProjectionMatrix * clip;
    view /= max(abs(view.w), 0.000001);
    return (u_InverseViewMatrix * view).xyz;
}

float heightIntegral(vec3 rayOrigin, vec3 rayDirection, float distanceLimit) {
    float thickness = max(u_FogMaxHeight - u_FogMinHeight, 1.0);
    float falloff = 2.0 / thickness;
    float rayY = abs(rayDirection.y) < 0.00002
            ? (rayDirection.y < 0.0 ? -0.00002 : 0.00002)
            : rayDirection.y;
    float base = exp(-clamp(falloff * max(rayOrigin.y - u_FogMinHeight, 0.0), 0.0, 60.0));
    float travel = 1.0 - exp(clamp(-falloff * rayY * distanceLimit, -60.0, 60.0));
    return clamp(base * travel / (falloff * rayY), 0.0, distanceLimit);
}

float henyeyGreenstein(float angle, float anisotropy) {
    float squared = anisotropy * anisotropy;
    return (1.0 - squared)
            / pow(max(1.0 + squared - 2.0 * anisotropy * angle, 0.0001), 1.5);
}

float godRaysMask(vec2 uv, vec2 sunUv, float jitter) {
    float lengthScale = mix(0.72, 1.12, saturate((u_RayLength - 0.15) / 1.10));
    int sampleCount = int(clamp(floor(u_RayCount + 0.5), 6.0, 32.0));
    vec2 stepUv = (sunUv - uv) * lengthScale / float(sampleCount);
    vec2 sampleUv = uv + stepUv * jitter;
    float light = 0.0;
    float weight = 1.0;
    float total = 0.0;

    for (int index = 0; index < 32; index++) {
        if (index >= sampleCount) {
            break;
        }
        vec2 safeUv = clamp(sampleUv, vec2(0.001), vec2(0.999));
        light += step(0.999999, texture(u_DepthTexture, safeUv).r) * weight;
        total += weight;
        weight *= 0.925;
        sampleUv += stepUv;
    }

    return light / max(total, 0.0001);
}

vec3 sampleSceneSoft(vec2 uv, float radius, float rotation) {
    vec3 center = texture(u_ScreenTexture, uv).rgb;
    if (radius < 0.5) {
        return center;
    }

    vec2 pixel = vec2(radius) / max(u_Resolution, vec2(1.0));
    float cosine = cos(rotation);
    float sine = sin(rotation);
    mat2 basis = mat2(cosine, sine, -sine, cosine);
    vec3 result = center * 0.2;

    for (int index = 0; index < 8; index++) {
        vec2 sampleUv = clamp(uv + basis * POISSON[index] * pixel, vec2(0.001), vec2(0.999));
        result += texture(u_ScreenTexture, sampleUv).rgb * 0.1;
    }

    return result;
}

vec3 atmosphereSky(vec3 rayDirection, float sunAngle) {
    float up = saturate(rayDirection.y);
    float below = saturate(-rayDirection.y);
    vec3 gradient = mix(u_PaletteHorizonCool, u_PaletteZenith, pow(up, 0.58));
    float horizonBand = pow(1.0 - up, 3.2);
    float sunNear = pow(saturate(sunAngle * 0.5 + 0.5), 3.0);
    gradient = mix(gradient, u_PaletteHorizonWarm,
            saturate(sunNear * (0.30 + 0.70 * horizonBand)));
    gradient += u_PaletteRay * henyeyGreenstein(sunAngle, 0.62) * 0.045 * u_SkyGlow;
    return mix(gradient, u_PaletteHorizonCool * 0.9, below * 0.8);
}

void main() {
    vec2 uv = gl_FragCoord.xy / max(u_Resolution, vec2(1.0));
    float depth = texture(u_DepthTexture, uv).r;
    float geometry = 1.0 - step(0.999999, depth);
    float skyMask = 1.0 - geometry;

    vec3 rayOrigin = u_CameraPos;
    vec3 farPosition = worldPosition(uv, 0.999999);
    vec3 rayDirection = normalize(farPosition - rayOrigin + vec3(0.0, 0.0000001, 0.0));
    vec3 surfacePosition = worldPosition(uv, min(depth, 0.999999));
    float sceneDistance = mix(2600.0, length(surfacePosition - rayOrigin), geometry);
    float edgeFade = smoothstep(u_FogStart, u_FogStart + 7.5, sceneDistance);

    float middleHeight = (u_FogMinHeight + u_FogMaxHeight) * 0.5;
    float distanceToLayer = abs(middleHeight - rayOrigin.y) / max(abs(rayDirection.y), 0.04);
    float probeDistance = clamp(min(distanceToLayer, sceneDistance * 0.5), 6.0, 220.0);
    vec3 probePosition = rayOrigin + rayDirection * probeDistance;
    float caveFade = smoothstep(u_FogMinHeight - 40.0, u_FogMinHeight - 12.0, rayOrigin.y);
    float densityModulation = fogTurbulence(probePosition);
    densityModulation = mix(densityModulation, 1.0, smoothstep(120.0, 220.0, probeDistance));

    float sigma = u_FogDensity * 0.058 * densityModulation * caveFade;
    float fogGeometry = saturate(
            (1.0 - exp(-sigma * heightIntegral(rayOrigin, rayDirection, min(sceneDistance, 900.0))))
            * edgeFade
    );
    float fogSky = saturate(
            1.0 - exp(-sigma * heightIntegral(rayOrigin, rayDirection, 4000.0))
    );
    float fogAmount = mix(fogSky, fogGeometry, geometry);

    vec3 sunDirection = normalize(u_SunDirection);
    float sunAngle = dot(rayDirection, sunDirection);
    float lowSun = saturate(1.0 - abs(sunDirection.y) * 2.2);
    float horizonGlow = pow(saturate(1.0 - abs(rayDirection.y) * 1.30), 2.3);
    vec3 skyColor = atmosphereSky(rayDirection, sunAngle);

    float sunward = pow(saturate(sunAngle * 0.5 + 0.5), 3.4);
    float vertical = saturate(rayDirection.y * 1.9 + 0.55);
    vec3 shadowFog = mix(u_PaletteFogCool * 0.92, u_PaletteFogCool * 1.10, vertical);
    vec3 warmFog = mix(u_PaletteFogWarm, u_PaletteHorizonWarm, pow(saturate(sunAngle), 2.4));
    vec3 fogColor = mix(shadowFog, warmFog, saturate(sunward * (0.30 + 0.70 * lowSun)));
    fogColor += u_PaletteRay * henyeyGreenstein(sunAngle, 0.56)
            * 0.072 * (0.35 + 0.65 * lowSun) * horizonGlow;
    fogColor /= 1.0 + max(luminance(fogColor) - 0.865, 0.0) * 2.4;

    float viewRange = max(u_ViewDistance, 64.0);
    float farHaze = smoothstep(viewRange * 0.45, viewRange * 0.92, sceneDistance) * geometry;
    farHaze *= mix(0.75, 1.0, pow(1.0 - saturate(rayDirection.y), 3.2));
    float skyBlend = saturate(0.42 + 0.58 * pow(1.0 - saturate(rayDirection.y), 2.6));

    float blurDrive = max(fogAmount, max(farHaze * 0.7, skyMask * skyBlend * 0.45));
    vec3 scene = sampleSceneSoft(
            uv,
            u_Softness * 7.5 * blurDrive,
            hash12(gl_FragCoord.xy) * 6.2831853
    );

    vec3 color = mix(scene, skyColor, skyMask * skyBlend * u_SkyGlow);
    vec3 veiled = color * mix(vec3(1.0), vec3(0.90, 0.94, 1.03), fogAmount * 0.30);
    color = mix(veiled, fogColor, fogAmount);
    color = mix(color, skyColor, farHaze * 0.92);

    if (u_GodRays > 0.001 && u_SunScreen.z > 0.001) {
        float jitter = hash12(gl_FragCoord.yx + vec2(17.0, 59.0)
                + u_Time * u_RayMotion * 0.013);
        float occlusion = godRaysMask(uv, u_SunScreen.xy, jitter);
        float stylePower = u_RayStyle == 0 ? 2.35 : u_RayStyle == 1 ? 3.85 : 3.0;
        stylePower = mix(stylePower + 0.65, max(1.45, stylePower - 0.75), u_RayWidth);
        float angular = pow(saturate(sunAngle * 0.5 + 0.5), stylePower);
        float rays = occlusion * angular * u_SunScreen.z * u_GodRays;
        rays *= 0.45 + 0.55 * saturate(fogAmount + skyMask * 0.85 + farHaze);

        float verticalDelta = rayDirection.y - sunDirection.y;
        float angleSquared = max(2.0 * (1.0 - sunAngle), 0.0);
        float disc = exp(-angleSquared * 2600.0);
        float horizontalSquared = max(angleSquared - verticalDelta * verticalDelta, 0.0);
        float streakWidth = mix(50.0, 18.0, u_RayWidth);
        float streak = exp(-(verticalDelta * verticalDelta * 850.0 + horizontalSquared * streakWidth));
        float glowVisibility = u_SunScreen.z * u_GodRays * (0.35 + 0.65 * lowSun);
        float sunCanvas = saturate(skyMask + farHaze);

        vec3 rayTint = mix(u_PaletteRay, u_RayColor, 0.24);
        rayTint = mix(rayTint, vec3(1.0), u_WhiteRayMix * 0.45);
        vec3 sunGlow = rayTint
                * (disc * 1.05 * sunCanvas + streak * 0.38 * mix(0.25, 1.0, sunCanvas))
                * glowVisibility;
        vec3 rayGlow = clamp(rayTint * rays * 0.85 + sunGlow, vec3(0.0), vec3(1.0));
        color = vec3(1.0) - (vec3(1.0) - clamp(color, vec3(0.0), vec3(1.0)))
                * (vec3(1.0) - rayGlow);
    }

    color /= 1.0 + max(luminance(color) - 0.985, 0.0) * 1.6;
    color += vec3((hash12(gl_FragCoord.xy + vec2(311.7, 74.3)) - 0.5) * 0.005);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
