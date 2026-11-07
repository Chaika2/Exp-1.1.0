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

float gyroid(vec3 p) {
    return dot(cos(p), sin(p.yzx));
}

float fbm(vec3 p, float localTime) {
    float result = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; ++i) {
        p += result * 0.08;
        p.z += localTime * 0.12;
        result += abs(gyroid(p / a) * a);
        a /= 1.75;
    }
    return result;
}

void main() {
    vec2 uv = (effectTexCoord() * 2.0 - 1.0) * 1.9;
    vec3 ray = normalize(vec3(uv, 0.35));
    vec3 samplePos = ray * 2.8;
    float localTime = time * 0.9;
    float field = fbm(samplePos, localTime);
    float detail = fbm(samplePos + vec3(1.3, -0.8, 0.6), localTime * 0.82);
    float mask = smoothstep(0.18, 0.92, field);
    float veins = smoothstep(0.18, 0.86, abs(sin(field * 3.1 + detail * 1.6 - localTime * 0.55)));

    vec3 cLow = tintedBaseColor() * 0.18;
    vec3 cMid = clamp(tintedBaseColor() * 1.02 + vec3(0.05), 0.0, 1.0);
    vec3 cHigh = mix(tintedBaseColor(), vec3(1.0), 0.20);
    vec3 cAccent = clamp(vec3(tintedBaseColor().r * 0.55 + 0.08, tintedBaseColor().g * 0.34 + 0.06, tintedBaseColor().b * 1.10 + 0.10), 0.0, 1.0);

    vec3 color = mix(cLow, cMid, mask);
    color = mix(color, cHigh, veins * 0.42);
    color = mix(color, cAccent, smoothstep(0.45, 1.0, detail) * 0.32);
    color += cMid * (detail * 0.05);
    color = clamp(color, 0.0, 1.8);

    fragColor = vec4(color, alpha * (0.20 + mask * 0.34 + veins * 0.12) * vertexColor.a);
}
