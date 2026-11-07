#version 150

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

uniform float time;
uniform vec4 baseColor;
uniform float alpha;
uniform int wholeBoxProjection;
uniform vec4 textureBounds;

vec3 tintedBaseColor() { return baseColor.rgb * vertexColor.rgb; }

vec2 effectTexCoord() {
    if (wholeBoxProjection == 0) return texCoord;
    return clamp((gl_FragCoord.xy - textureBounds.xy) / max(textureBounds.zw, vec2(1.0)), 0.0, 1.0);
}

const mat2 M = mat2(1.6, 1.2, -1.2, 1.6);
const float CLOUD_SCALE = 1.1;
const float CLOUD_SPEED = 0.03;
const float CLOUD_DARK = 0.5;
const float CLOUD_LIGHT = 0.3;
const float CLOUD_COVER = 0.2;
const float CLOUD_ALPHA = 8.0;
const float SKY_TINT = 0.5;

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

void main() {
    vec2 p = effectTexCoord();
    vec2 uv = (p * 2.0 - 1.0) * vec2(1.25, 1.0);
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

    vec3 cLow = mix(tintedBaseColor() * 0.22, tintedBaseColor() * 0.62 + vec3(0.06, 0.08, 0.12), 0.6);
    vec3 cHigh = mix(tintedBaseColor() * 1.05, mix(tintedBaseColor(), vec3(1.0), 0.24), 0.5);
    vec3 skyColour = mix(cLow, cHigh, p.y);

    vec3 cloudBase = mix(tintedBaseColor(), vec3(1.0), 0.42);
    vec3 cloudColour = cloudBase * clamp(CLOUD_DARK + CLOUD_LIGHT * c, 0.0, 1.0);

    f = CLOUD_COVER + CLOUD_ALPHA * f * r;
    vec3 tintedSky = clamp(SKY_TINT * skyColour + cloudColour, 0.0, 1.0);
    vec3 result = mix(skyColour, tintedSky, clamp(f + c, 0.0, 1.0));

    fragColor = vec4(clamp(result, 0.0, 1.8), alpha * (0.18 + clamp(f + c, 0.0, 1.0) * 0.45) * vertexColor.a);
}
