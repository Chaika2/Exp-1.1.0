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

const float TAU = 6.2831853;

float colormapRed(float x) {
    if (x < 0.0) {
        return 54.0 / 255.0;
    } else if (x < 20049.0 / 82979.0) {
        return (829.79 * x + 54.51) / 255.0;
    }
    return 1.0;
}

float colormapGreen(float x) {
    if (x < 20049.0 / 82979.0) {
        return 0.0;
    } else if (x < 327013.0 / 810990.0) {
        return (8546482679670.0 / 10875673217.0 * x - 2064961390770.0 / 10875673217.0) / 255.0;
    } else if (x <= 1.0) {
        return (103806720.0 / 483977.0 * x + 19607415.0 / 483977.0) / 255.0;
    }
    return 1.0;
}

float colormapBlue(float x) {
    if (x < 0.0) {
        return 54.0 / 255.0;
    } else if (x < 7249.0 / 82979.0) {
        return (829.79 * x + 54.51) / 255.0;
    } else if (x < 20049.0 / 82979.0) {
        return 127.0 / 255.0;
    } else if (x < 327013.0 / 810990.0) {
        return (792.0224934136139 * x - 64.36479073560233) / 255.0;
    }
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

const mat2 MTX = mat2(0.80, 0.60, -0.60, 0.80);

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

vec2 skyCoords(vec3 direction) {
    vec3 d = normalize(direction);
    float longitude = atan(d.z, d.x) / TAU + 0.5;
    float latitude = asin(clamp(d.y, -1.0, 1.0)) / 1.5707963;
    return vec2(fract(longitude), clamp(latitude, 0.0, 1.0));
}

vec2 periodicUv(vec2 p) {
    float angle = p.x * TAU;
    return vec2(cos(angle), sin(angle)) * 0.72 + vec2(0.0, p.y * 1.7);
}

void main() {
    vec3 direction = normalize(skyDir);
    vec2 p = skyCoords(direction);
    vec2 uv = periodicUv(p);
    float shade = pattern(uv * 1.25, time * 0.9);
    vec3 heat = colormap(shade);

    float warm = heat.r;
    float mid = heat.g;
    float cool = heat.b;

    vec3 effect = mix(color4 * 0.16, color1, warm * 0.92);
    effect = mix(effect, color2, mid * 0.32);
    effect = mix(effect, color3, cool * 0.22);
    effect += color1 * (shade * 0.10);
    effect = clamp(effect, 0.0, 1.8);

    float horizonFade = smoothstep(-0.34, -0.08, direction.y);
    fragColor = vec4(effect, clamp(opacity, 0.0, 1.0) * horizonFade * shade);
}
