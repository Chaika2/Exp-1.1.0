#version 330 core

in vec2 uv;
out vec4 out_color;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_tint;

#define iResolution vec3(max(u_resolution, vec2(1.0)), 1.0)
#define iTime u_time

void mainImage(out vec4 fragColor, vec2 fragCoord) {
    float minResolution = min(iResolution.x, iResolution.y);
    vec2 uvCoords = (fragCoord * 2.0 - iResolution.xy) / minResolution;

    float d = -iTime * 0.5;
    float a = 0.0;

    for (float i = 0.0; i < 8.0; ++i) {
        a += cos(i - d - a * uvCoords.x);
        d += sin(uvCoords.y * i + a);
    }

    d += iTime * 0.5;

    vec3 col = vec3(cos(uvCoords * vec2(d, a)) * 0.6 + 0.4, cos(a + d) * 0.5 + 0.5);
    col = cos(col * cos(vec3(d, a, 2.5)) * 0.5 + 0.5);

    fragColor = vec4(col, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, uv * max(u_resolution, vec2(1.0)));

    vec3 shaderColor = clamp(color.rgb, 0.0, 1.0);
    out_color = vec4(shaderColor * u_tint.rgb, u_tint.a);
}
