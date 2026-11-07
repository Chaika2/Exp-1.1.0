#version 150

in vec3 skyDir;
out vec4 fragColor;

uniform float time;
uniform float opacity;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;

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
    vec3 direction = normalize(skyDir);
    vec3 ray = normalize(vec3(direction.x, direction.y * 0.8 + 0.24, direction.z));
    vec3 samplePos = ray * 3.0;
    float localTime = time * 0.9;
    float field = fbm(samplePos, localTime);
    float detail = fbm(samplePos + vec3(1.3, -0.8, 0.6), localTime * 0.82);
    float mask = smoothstep(0.18, 0.92, field);
    float veins = smoothstep(0.18, 0.86, abs(sin(field * 3.1 + detail * 1.6 - localTime * 0.55)));

    vec3 color = mix(color4 * 0.20, color1, mask);
    color = mix(color, color2, veins * 0.42);
    color = mix(color, color3, smoothstep(0.45, 1.0, detail) * 0.34);
    color += color2 * (detail * 0.05);
    color = clamp(color, 0.0, 1.8);

    float horizonFade = smoothstep(-0.34, -0.08, direction.y);
    float finalAlpha = clamp(opacity, 0.0, 1.0) * horizonFade;
    fragColor = vec4(color, finalAlpha);
}
