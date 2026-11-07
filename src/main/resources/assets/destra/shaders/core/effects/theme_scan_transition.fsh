#version 330 core

in vec2 v_Uv;

uniform sampler2D u_OldTexture;
uniform sampler2D u_NewTexture;
uniform vec2 u_Resolution;
uniform float u_Time;
uniform float u_Progress;
uniform float u_LinearProgress;
uniform vec2 u_Origin;
uniform float u_Aspect;
uniform float u_Radius;
uniform float u_MaxRadius;

out vec4 fragColor;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float smootherstep(float value) {
    float t = saturate(value);
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 srgbToLinear(vec3 c) {
    return mix(c / 12.92, pow((c + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), c));
}

vec3 linearToSrgb(vec3 c) {
    c = clamp(c, 0.0, 1.0);
    return mix(c * 12.92, 1.055 * pow(c, vec3(1.0 / 2.4)) - 0.055,
               step(vec3(0.0031308), c));
}

vec3 linearToOklab(vec3 c) {
    vec3 lms = mat3(
        0.4122214708, 0.2119034982, 0.0883024619,
        0.5363325363, 0.6806995451, 0.2817188376,
        0.0514459929, 0.1073969566, 0.6299787005
    ) * c;
    lms = sign(lms) * pow(abs(lms), vec3(1.0 / 3.0));
    return mat3(
        0.2104542553, 1.9779984951, 0.0259040371,
        0.7936177850, -2.4285922050, 0.7827717662,
        -0.0040720468, 0.4505937099, -0.8086757660
    ) * lms;
}

vec3 oklabToLinear(vec3 c) {
    vec3 lms = mat3(
        1.0, 1.0, 1.0,
        0.3963377774, -0.1055613458, -0.0894841775,
        0.2158037573, -0.0638541728, -1.2914855480
    ) * c;
    lms = lms * lms * lms;
    return mat3(
        4.0767416621, -1.2684380046, -0.0041960863,
        -3.3077115913, 2.6097574011, -0.7034186147,
        0.2309699292, -0.3413193965, 1.7076147010
    ) * lms;
}

vec3 perceptualMix(vec3 a, vec3 b, float amount) {
    vec3 labA = linearToOklab(srgbToLinear(a));
    vec3 labB = linearToOklab(srgbToLinear(b));
    return linearToSrgb(oklabToLinear(mix(labA, labB, saturate(amount))));
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
}

vec3 chromaticSample(sampler2D source, vec2 uv, vec2 split) {
    return vec3(
        texture(source, safeUv(uv + split)).r,
        texture(source, safeUv(uv)).g,
        texture(source, safeUv(uv - split)).b
    );
}

void main() {
    float p = saturate(u_Progress);
    float linearProgress = saturate(u_LinearProgress);
    vec2 resolution = max(u_Resolution, vec2(1.0));
    float aspect = max(u_Aspect, 0.001);
    vec2 origin = vec2(u_Origin.x / resolution.x, 1.0 - u_Origin.y / resolution.y);
    vec2 delta = vec2((v_Uv.x - origin.x) * aspect, v_Uv.y - origin.y);
    float distanceToOrigin = length(delta);
    vec2 directionAspect = distanceToOrigin > 0.00001 ? delta / distanceToOrigin : vec2(0.0, 1.0);
    vec2 directionUv = normalize(vec2(directionAspect.x / aspect, directionAspect.y));
    vec2 tangentUv = vec2(-directionUv.y, directionUv.x);

    float radius = max(u_Radius, 0.0);
    float bandWidth = mix(0.115, 0.045, smootherstep(p));
    float bandCoordinate = (distanceToOrigin - radius) / max(bandWidth, 0.0001);
    float envelope = exp(-bandCoordinate * bandCoordinate * 1.75);

    // The wave is visible only through refraction. No reveal circle, rim,
    // highlight or accent-coloured edge is drawn.
    float birth = smootherstep(linearProgress * 9.0);
    float death = 1.0 - smootherstep((linearProgress - 0.78) / 0.22);
    float energy = envelope * birth * death;
    float angularNoise = hash12(floor(directionAspect * 93.0) + floor(u_Time * 7.0));
    float ripple = sin(bandCoordinate * 4.6 - u_Time * 8.5 + angularNoise * 1.8);
    vec2 radialWarp = directionUv * ripple * energy * 0.0095;
    vec2 shearWarp = tangentUv * (angularNoise - 0.5) * energy * 0.0022;
    vec2 warpedUv = v_Uv - radialWarp - shearWarp;
    vec2 split = directionUv * energy * 0.00065;

    vec3 oldColor = chromaticSample(u_OldTexture, warpedUv, split * 0.45);
    vec3 newColor = chromaticSample(u_NewTexture, warpedUv, -split);

    // Theme colour changes globally; the travelling distortion is purely a
    // post-effect and therefore never leaves an old circular region behind.
    float themeBlend = smootherstep(linearProgress / 0.62);
    vec3 color = perceptualMix(oldColor, newColor, themeBlend);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
