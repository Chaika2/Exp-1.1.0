#version 150

in vec3 skyDir;
out vec4 fragColor;

uniform float time;
uniform float opacity;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;

const int WAVELET_LAYERS = 2;
const int VOLUMETRIC_STEPS = 12;
const float VOLUMETRIC_BRIGHTNESS = -8.8;

vec3 erot(vec3 p, vec3 ax, float ro) {
    return mix(dot(p, ax) * ax, p, cos(ro)) + sin(ro) * cross(ax, p);
}

float WaveletNoise(vec3 p, float z, float k) {
    float d = 0.0;
    float s = 1.0;
    float m = 0.0;
    float a;

    for (int i = 0; i < WAVELET_LAYERS; i++) {
        vec3 q = p * s;
        vec3 g = fract(floor(q) * vec3(123.34, 233.53, 314.15));
        g += dot(g, g + 23.234);
        a = fract(g.x * g.y) * 1e3 + z * (mod(g.x + g.y, 2.0) - 1.0);
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
    vec3 p = normalize(dir) * scale;
    vec3 cell = floor(p);
    vec3 local = fract(p) - 0.5;
    float seed = hash13(cell);
    float radius = mix(0.08, 0.20, seed);
    float star = smoothstep(radius, 0.0, length(local));
    star *= smoothstep(threshold, 1.0, seed);
    return star * mix(0.4, 1.0, smoothstep(threshold, 1.0, seed));
}

void main() {
    vec3 direction = normalize(skyDir);
    vec3 dir = normalize(vec3(direction.x, direction.y * 0.72 + 0.28, direction.z));
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
        vec3 p3 = from + s * dir * 0.5;
        vec3 p2 = p3 * 2.9;
        float a = WaveletNoise(p2, 0.0, 1.9) * 2.0 - 1.0;
        a *= a * a;
        v += vec3(s, s * s, s * s * s * s) * a * VOLUMETRIC_BRIGHTNESS * fade;
        fade *= 0.91;
        s += 0.032 * 1.55;
    }

    float f = starLayer(from + dir * 1.05, 34.0, 0.965);
    f = max(f, starLayer(from + dir * 1.4, 58.0, 0.982));

    v = mix(vec3(length(v)), v, 0.95);
    v = clamp(v.gbr * 0.01, vec3(0.0), vec3(1.0));
    v += v * f;

    float intensity = clamp(max(max(v.r, v.g), v.b) * 1.75, 0.0, 1.0);
    vec3 palette = mix(color4 * 0.18, color1, clamp(v.b * 2.2 + v.g, 0.0, 1.0));
    palette = mix(palette, color2, clamp(v.r * 2.4, 0.0, 1.0));
    palette = mix(palette, color3, clamp(f * 0.55 + v.g, 0.0, 1.0));
    vec3 effect = palette * intensity + color4 * (f * 0.18);
    effect = clamp(effect, 0.0, 1.8);

    float horizonFade = smoothstep(-0.36, -0.08, direction.y);
    float finalAlpha = clamp(opacity, 0.0, 1.0) * horizonFade;
    fragColor = vec4(effect, finalAlpha);
}
