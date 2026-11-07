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

vec2 domeUv(vec3 dir) {
    dir = normalize(dir);
    float k = 1.0 / (1.0 + max(dir.y, -0.35));
    return dir.xz * k * 0.55 + 0.5;
}

void main() {
    vec3 dir = normalize(skyDir);
    vec2 uv = (domeUv(dir) - 0.5) * 2.0 * 1.1;
    for (float i = 1.0; i < 10.0; i++) {
        uv.x += 0.6 / i * cos(i * 2.5 * uv.y + time);
        uv.y += 0.6 / i * cos(i * 1.5 * uv.x + time);
    }
    float wave = 0.1 / max(abs(sin(time - uv.y - uv.x)), 0.035);
    float h = clamp(dir.y * 0.5 + 0.5, 0.0, 1.0);
    vec3 base = mix(color1 * 0.04, color2 * 0.10, h);
    vec3 color = base + mix(color1, color2, smoothstep(0.0, 1.2, wave)) * wave;
    color *= 0.72 + smoothstep(-0.65, 0.85, dir.y) * 0.34;
    fragColor = vec4(color, opacity);
}
