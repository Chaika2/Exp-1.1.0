#version 330 core

in vec2 uv;
out vec4 out_color;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_tint;

#define iResolution vec3(max(u_resolution, vec2(1.0)), 1.0)
#define iTime u_time
#define TAU 6.28318530718
#define MAX_ITER 5

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    float time = iTime * 0.5 + 23.0;
    vec2 uvCoords = fragCoord.xy / iResolution.xy;
    vec2 p = mod(uvCoords * TAU, TAU) - 250.0;
    vec2 i = p;

    float c = 1.0;
    float intensity = 0.005;

    for (int n = 0; n < MAX_ITER; n++) {
        float t = time * (1.0 - (3.5 / float(n + 1)));
        i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
        c += 1.0 / length(vec2(
                p.x / (sin(i.x + t) / intensity),
                p.y / (cos(i.y + t) / intensity)
        ));
    }

    c /= float(MAX_ITER);
    c = 1.17 - pow(c, 1.4);

    vec3 color = vec3(pow(abs(c), 8.0));
    color = clamp(color + vec3(0.0, 0.35, 0.5), 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, uv * max(u_resolution, vec2(1.0)));

    vec3 shaderColor = clamp(color.rgb, 0.0, 1.0);
    out_color = vec4(shaderColor * u_tint.rgb, u_tint.a);
}
