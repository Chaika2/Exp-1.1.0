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
const mat2 M = mat2(1.6, 1.2, -1.2, 1.6);
const float CLOUD_SCALE = 1.1;
const float CLOUD_SPEED = 0.03;
const float CLOUD_DARK = 0.5;
const float CLOUD_LIGHT = 0.3;
const float CLOUD_COVER = 0.2;
const float CLOUD_ALPHA = 8.0;
const float SKY_TINT = 0.5;

float hash11(float n) {
    return fract(sin(n) * 43758.5453123);
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

float fbm(vec2 n, float localTime) {
    float total = 0.0;
    float amplitude = 0.1;

    for (int i = 0; i < 7; i++) {
        total += destraNoise(n) * amplitude;
        n = M * n;
        n += vec2(localTime * 0.02, localTime * 0.015);
        amplitude *= 0.4;
    }

    return total;
}

vec2 skyCoords(vec3 direction) {
    vec3 d = normalize(direction);
    float longitude = atan(d.z, d.x) / TAU + 0.5;
    float latitude = asin(clamp(d.y, -1.0, 1.0)) / 1.5707963;
    return vec2(fract(longitude), clamp(latitude, 0.0, 1.0));
}

vec2 periodicUv(vec2 p) {
    float angle = p.x * TAU;
    return vec2(cos(angle), sin(angle)) * 0.95 + vec2(0.0, p.y * 1.45);
}

void main() {
    vec3 direction = normalize(skyDir);
    vec2 p = skyCoords(direction);
    vec2 uv = periodicUv(p);
    float localTime = time * CLOUD_SPEED;
    float q = fbm(uv * CLOUD_SCALE * 0.5, localTime);

    float r = 0.0;
    vec2 uvRidged = uv * CLOUD_SCALE;
    uvRidged -= q - localTime;
    float weight = 0.8;
    for (int i = 0; i < 4; i++) {
        r += abs(weight * destraNoise(uvRidged));
        uvRidged = M * uvRidged + localTime;
        weight *= 0.7;
    }

    float f = 0.0;
    vec2 uvShape = uv * CLOUD_SCALE;
    uvShape -= q - localTime;
    weight = 0.7;
    for (int i = 0; i < 6; i++) {
        f += weight * destraNoise(uvShape);
        uvShape = M * uvShape + localTime;
        weight *= 0.6;
    }
    f *= r + f;

    float c = 0.0;
    float colourTime = time * CLOUD_SPEED * 2.0;
    vec2 uvColour = uv * CLOUD_SCALE * 2.0;
    uvColour -= q - colourTime;
    weight = 0.4;
    for (int i = 0; i < 4; i++) {
        c += weight * destraNoise(uvColour);
        uvColour = M * uvColour + colourTime;
        weight *= 0.6;
    }

    float c1 = 0.0;
    float ridgeTime = time * CLOUD_SPEED * 3.0;
    vec2 uvRidgeColour = uv * CLOUD_SCALE * 3.0;
    uvRidgeColour -= q - ridgeTime;
    weight = 0.4;
    for (int i = 0; i < 2; i++) {
        c1 += abs(weight * destraNoise(uvRidgeColour));
        uvRidgeColour = M * uvRidgeColour + ridgeTime;
        weight *= 0.6;
    }
    c += c1;

    vec3 skyColourLow = mix(color4 * 0.45, color2 * 0.72 + color4 * 0.18, 0.6);
    vec3 skyColourHigh = mix(color1 * 0.82 + color3 * 0.18, color3 * 0.88, 0.5);
    vec3 skyColour = mix(skyColourLow, skyColourHigh, p.y);

    vec3 cloudBase = mix(color3, vec3(1.0), 0.42);
    vec3 cloudColour = cloudBase * clamp(CLOUD_DARK + CLOUD_LIGHT * c, 0.0, 1.0);

    f = CLOUD_COVER + CLOUD_ALPHA * f * r;
    vec3 tintedSky = clamp(SKY_TINT * skyColour + cloudColour, 0.0, 1.0);
    vec3 result = mix(skyColour, tintedSky, clamp(f + c, 0.0, 1.0));

    float horizonFade = smoothstep(-0.34, -0.08, direction.y);
    fragColor = vec4(clamp(result, 0.0, 1.8), clamp(opacity, 0.0, 1.0) * horizonFade);
}
