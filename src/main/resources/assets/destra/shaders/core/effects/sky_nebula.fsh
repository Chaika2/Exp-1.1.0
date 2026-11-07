#version 150

in vec3 skyDir;
out vec4 fragColor;

uniform float time;
uniform float opacity;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;

const float TAU = 6.2831853;
const float PI = 3.14159265;

float destraHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float destraNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(destraHash(i), destraHash(i + vec2(1.0, 0.0)), u.x),
        mix(destraHash(i + vec2(0.0, 1.0)), destraHash(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += destraNoise(p) * a;
        p *= 2.03;
        a *= 0.5;
    }
    return v;
}

float seamlessFbm(vec2 p, float scale, float phase) {
    float angle = p.x * TAU;
    vec2 circle = vec2(cos(angle), sin(angle));
    vec2 coord = circle * scale + vec2(p.y * scale * 0.43 + phase, p.y * scale - phase * 0.37);
    return fbm(coord);
}

vec2 skyCoords(vec3 direction) {
    vec3 d = normalize(direction);
    float longitude = atan(d.z, d.x) / TAU + 0.5;
    float latitude = asin(clamp(d.y, -1.0, 1.0)) / 1.5707963;
    return vec2(fract(longitude), clamp(latitude, 0.0, 1.0));
}

vec3 nebula(vec2 p) {
    float n1 = seamlessFbm(p, 2.5, time * 0.06);
    float n2 = seamlessFbm(p + vec2(0.17, 0.0), 5.0, -time * 0.05);
    vec3 col = mix(color4 * 0.45, color1, n1);
    col = mix(col, color2, smoothstep(0.35, 0.85, n2));
    col += color3 * pow(n1 * n2, 1.7);
    return col;
}

void main() {
    vec3 direction = normalize(skyDir);
    vec2 p = skyCoords(direction);

    vec3 effect = nebula(p);
    effect = clamp(effect, 0.0, 1.8);

    float horizonFade = smoothstep(-0.34, -0.08, direction.y);
    float alpha = clamp(opacity, 0.0, 1.0) * horizonFade;
    fragColor = vec4(effect, alpha);
}
