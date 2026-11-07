#version 150

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;
uniform int wholeBoxProjection;
uniform vec4 textureBounds;

vec3 tintedBaseColor() { return baseColor.rgb * vertexColor.rgb; }

vec2 effectTexCoord() {
    if (wholeBoxProjection == 0) return texCoord;
    return clamp((gl_FragCoord.xy - textureBounds.xy) / max(textureBounds.zw, vec2(1.0)), 0.0, 1.0);
}

#define NUM_OCTAVES 5

float random(vec2 pos) {
    return fract(sin(dot(pos.xy, vec2(13.9898, 78.233))) * 43758.5453123);
}

float destraNoise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i + vec2(0.0, 0.0));
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos, float t) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));

    for (int i = 0; i < NUM_OCTAVES; i++) {
        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;
        v += a * destraNoise(pos + dir * t * 0.3);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }

    return v;
}

void main(void) {
    vec2 p = (effectTexCoord() * 2.0 - 1.0) * 3.0;
    float t = time;

    vec2 q = vec2(0.0);
    q.x = fbm(p + vec2(0.0, 0.0), t * 0.8);
    q.y = fbm(p + vec2(1.0, 1.0), t * 0.6);

    vec2 r = vec2(0.0);
    r.x = fbm(p + 1.0 * q + vec2(1.7, 1.2), t * 0.7);
    r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8), t * 0.9);

    float f = fbm(p + r, t);

    vec3 color1 = tintedBaseColor() * 1.4;
    vec3 color2 = tintedBaseColor() * 0.5;
    vec3 color3 = vec3(tintedBaseColor().r * 0.4, tintedBaseColor().g * 0.3, tintedBaseColor().b * 1.4);

    vec3 color = mix(color1, color2, clamp((f * f) * 4.0, 0.0, 1.0));
    color = mix(color, color1, clamp(length(q), 0.0, 1.0));
    color = mix(color, color3, clamp(length(r.x), 0.0, 1.0));

    color = (f * f * f + 0.6 * f * f + 0.5 * f) * color;
    color = clamp(color * 1.8, 0.0, 1.0);

    float finalAlpha = alpha * (0.15 + f * 0.25) * vertexColor.a;
    fragColor = vec4(color, finalAlpha);
}
