#version 150

in vec3 skyDir;
out vec4 fragColor;

uniform float time;
uniform float opacity;
uniform vec2 resolution;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;

#define AURORA_STEPS 10
#define AURORA_FBM_OCTAVES 3

float destraRandom2(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 destraNoise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    vec2 df = 20.0 * f * f * (f * (f - 2.0) + 1.0);
    f = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);

    float a = destraRandom2(i + vec2(0.5));
    float b = destraRandom2(i + vec2(1.5, 0.5));
    float c = destraRandom2(i + vec2(0.5, 1.5));
    float d = destraRandom2(i + vec2(1.5, 1.5));

    float k = a - b - c + d;
    float n = mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    return vec3(n, vec2(b - a + k * f.y, c - a + k * f.x) * df);
}

mat2 terrainProps = mat2(0.8, -0.4, 0.5, 0.8);

float fbmL(vec2 p) {
    vec2 df = vec2(0.0);
    float f = 0.0;
    float w = 0.5;
    for (int i = 0; i < 2; i++) {
        vec3 n = destraNoise2(p);
        df += n.yz;
        f += abs(w * n.x / (1.0 + dot(df, df)));
        w *= 0.5;
        p = 2.0 * terrainProps * p;
    }
    return f;
}

mat2 mm2(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat2(c, s, -s, c);
}

float tri(float x) {
    return clamp(abs(fract(x) - 0.5), 0.01, 0.49);
}

vec2 tri2(vec2 p) {
    return vec2(tri(p.x) + tri(p.y), tri(p.y + tri(p.x)));
}

float fbmAurora(vec2 p, float spd) {
    float z = 1.8;
    float z2 = 2.5;
    float rz = 0.0;
    p *= mm2(p.x * 0.06);
    vec2 bp = p;
    mat2 flowRot = mm2(time * spd);
    float timeWarp = sin(time * 0.05) * cos(time * 0.01);

    for (int i = 0; i < AURORA_FBM_OCTAVES; i++) {
        vec2 dg = tri2(bp * 1.85) * 0.75;
        dg *= flowRot;
        p -= dg / z2;

        bp *= 1.3;
        z2 *= 0.45;
        z *= 0.42;
        p *= 1.21 + (rz - 1.0) * 0.02;

        rz += tri(p.x + tri(p.y)) * z;
        p *= timeWarp;
    }
    return clamp(1.0 / pow(rz * 20.0, 1.3), 0.0, 1.0);
}

vec4 aurora(vec3 rd) {
    vec4 col = vec4(0.0);
    vec4 avgCol = vec4(0.0);

    float fragRnd = destraRandom2(gl_FragCoord.xy);
    float decay = exp2(-2.5);
    const float decayStep = exp2(-0.09);
    for (int i = 0; i < AURORA_STEPS; i++) {
        float fi = float(i);
        float of = 0.006 * fragRnd * smoothstep(0.0, 12.0, fi);
        float pt = (0.8 + pow(fi, 1.4) * 0.002) / (rd.y * 2.0 + 0.4);
        pt -= of;

        vec3 bpos = vec3(5.5) + pt * rd;
        vec2 p = bpos.zx;
        float rzt = fbmAurora(p, 0.06);

        float palettePosition = fract(fi * 0.071 + rzt * 0.23);
        vec3 lowerPalette = mix(color1, color2, smoothstep(0.0, 0.5, palettePosition));
        vec3 upperPalette = mix(color3, color4, smoothstep(0.5, 1.0, palettePosition));
        vec3 auroraColor = mix(lowerPalette, upperPalette, smoothstep(0.32, 0.68, palettePosition));
        vec4 col2 = vec4(auroraColor * rzt, rzt);

        avgCol = mix(avgCol, col2, 0.5);
        col += avgCol * decay * min(fi * 0.25, 1.0);
        decay *= decayStep;
    }

    col *= smoothstep(-0.22, 0.25, rd.y);
    return smoothstep(0.0, 1.1, col * 1.5);
}

vec3 stars(vec2 p) {
    vec2 g = floor(p * vec2(240.0, 128.0));
    vec2 f = fract(p * vec2(240.0, 128.0)) - 0.5;
    float seed = destraRandom2(g);
    float star = step(0.996, seed);
    float dist = length(f);
    float twinkle = 0.65 + 0.35 * sin(time * 2.1 + seed * 120.0);
    float core = smoothstep(0.04, 0.0, dist) * star * twinkle;
    return vec3(core * 0.75);
}

void setSkyColor(vec2 uv, out vec3 color, vec3 dir) {
    vec3 themeA = mix(color1, color2, 0.5);
    vec3 themeB = mix(color3, color4, 0.5);
    vec3 theme = mix(themeA, themeB, 0.5);

    vec3 baseTheme = mix(themeB, themeA, uv.y);
    vec3 baseSky = baseTheme * mix(0.035, 0.085, uv.y);
    color = baseSky + theme * 0.035;

    float starMask = smoothstep(0.02, 0.35, dir.y);
    vec2 starUv = vec2(atan(dir.z, dir.x) / 6.2831853 + 0.5, clamp(dir.y * 0.5 + 0.5, 0.0, 1.0));
    color += stars(starUv) * starMask;

    float auroraMask = smoothstep(-0.08, 0.34, dir.y);
    if (auroraMask > 0.001) {
        color += aurora(dir).rgb * auroraMask;
    }
}

void main() {
    vec3 rd = normalize(skyDir);
    float skyMask = smoothstep(-1.0, -0.94, rd.y);
    if (skyMask <= 0.0001) {
        fragColor = vec4(0.0);
        return;
    }
    vec2 uv = vec2(0.5 + 0.5 * rd.x, clamp(0.5 + 0.5 * rd.y, 0.0, 1.0));

    vec3 color = vec3(0.0);
    setSkyColor(uv, color, rd);

    color = pow(color, vec3(1.0 / 2.2));
    color = smoothstep(0.0, 1.0, color);

    fragColor = vec4(color, clamp(opacity, 0.0, 1.0) * skyMask);
}
