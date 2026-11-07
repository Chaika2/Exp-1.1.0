#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D ColorTexture;
uniform sampler2D DepthTexture;
uniform float time;
uniform vec4 baseColor;
uniform float effectAlpha;

const int WAVELET_LAYERS = 2;
const int VOLUMETRIC_STEPS = 10;

vec3 paletteA() { return clamp(baseColor.rgb * 1.22, 0.0, 1.0); }
vec3 paletteB() { return mix(baseColor.rgb, vec3(1.0), 0.35); }
vec3 paletteC() { return clamp(vec3(baseColor.r * 0.55 + 0.12, baseColor.g * 0.30 + 0.05, baseColor.b * 1.35 + 0.18), 0.0, 1.0); }
vec3 paletteD() { return clamp(baseColor.rgb * 0.24, 0.0, 1.0); }

vec3 erot(vec3 p, vec3 ax, float ro) {
    return mix(dot(p, ax) * ax, p, cos(ro)) + sin(ro) * cross(ax, p);
}

float waveletNoise(vec3 p, float z, float k) {
    float d = 0.0;
    float s = 1.0;
    float m = 0.0;
    for (int i = 0; i < WAVELET_LAYERS; i++) {
        vec3 q = p * s;
        vec3 g = fract(floor(q) * vec3(123.34, 233.53, 314.15));
        g += dot(g, g + 23.234);
        float a = fract(g.x * g.y) * 1e3 + z * (mod(g.x + g.y, 2.0) - 1.0);
        q = fract(q) - 0.5;
        q = erot(q, normalize(tan(g + 0.1)), a);
        d += sin(q.x * 10.0 + z) * smoothstep(0.25, 0.0, dot(q, q)) / s;
        p = erot(p, normalize(vec3(-1.0, 1.0, 0.0)), atan(sqrt(2.0))) + float(i);
        m += 1.0 / s;
        s *= k;
    }
    return d / m;
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float starLayer(vec3 dir, float scale, float threshold) {
    vec3 p = dir * scale;
    vec3 cell = floor(p);
    vec3 local = fract(p) - 0.5;
    float seed = hash13(cell);
    float radius = mix(0.08, 0.20, seed);
    float star = smoothstep(radius, 0.0, length(local));
    star *= smoothstep(threshold, 1.0, seed);
    return star * mix(0.4, 1.0, smoothstep(threshold, 1.0, seed));
}

float handMaskValue(vec2 coord) {
    vec2 texelSize = 1.0 / textureSize(DepthTexture, 0);
    float centerDepth = texture(DepthTexture, coord).r;
    float minDepth = centerDepth;
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(-texelSize.x, 0.0)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(texelSize.x, 0.0)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(0.0, -texelSize.y)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(0.0, texelSize.y)).r);
    return smoothstep(0.99, 0.98, minDepth);
}

void main() {
    vec4 originalColor = texture(ColorTexture, uv);
    float mask = handMaskValue(uv);
    if (mask < 0.01) {
        discard;
    }

    vec2 localUv = (uv * 2.0 - 1.0) * 1.65;
    vec3 dir = normalize(vec3(localUv * 0.8, 1.0));
    float localTime = time * 0.032;

    float a1 = 0.86;
    float a2 = 1.08;
    mat2 rot1 = mat2(cos(a1), sin(a1), -sin(a1), cos(a1));
    mat2 rot2 = mat2(cos(a2), sin(a2), -sin(a2), cos(a2));
    dir.xz *= rot1;
    dir.xy *= rot2;

    vec3 from = vec3(1.0, 0.5, 0.75);
    from += vec3(localTime * 1.8, localTime, -5.0);
    from.xz *= rot1;
    from.xy *= rot2;

    float s = 0.1;
    float fade = 1.0;
    vec3 v = vec3(0.0);
    for (int r = 0; r < VOLUMETRIC_STEPS; r++) {
        vec3 p = from + s * dir * 0.5;
        float a = waveletNoise(p * 2.9, 0.0, 1.9) * 2.0 - 1.0;
        a *= a * a;
        v += vec3(s, s * s, s * s * s * s) * a * -8.8 * fade;
        fade *= 0.91;
        s += 0.032 * 1.55;
    }

    float stars = starLayer(from + dir * 1.05, 34.0, 0.965);
    stars = max(stars, starLayer(from + dir * 1.4, 58.0, 0.982));

    v = mix(vec3(length(v)), v, 0.95);
    v = clamp(v.gbr * 0.01, vec3(0.0), vec3(1.0));
    v += v * stars;

    float intensity = clamp(max(max(v.r, v.g), v.b) * 1.75, 0.0, 1.0);
    vec3 effect = mix(paletteD(), paletteA(), clamp(v.b * 2.2 + v.g, 0.0, 1.0));
    effect = mix(effect, paletteB(), clamp(v.r * 2.4, 0.0, 1.0));
    effect = mix(effect, paletteC(), clamp(stars * 0.55 + v.g, 0.0, 1.0));
    effect += paletteD() * (stars * 0.18);
    effect = clamp(effect * intensity + effect * 0.35, 0.0, 1.8);

    vec3 finalColor = mix(originalColor.rgb, effect, effectAlpha);
    outColor = vec4(finalColor, mask);
}
