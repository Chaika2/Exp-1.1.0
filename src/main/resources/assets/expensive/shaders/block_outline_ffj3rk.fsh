#version 330 core

in vec2 uv;
out vec4 out_color;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_tint;

#define TAU 6.28318530718

vec2 rotate(vec2 p, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c) * p;
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

vec3 palette(float t) {
    return 0.55 + 0.45 * cos(vec3(0.2, 1.1, 2.0) + t * vec3(1.15, 1.35, 1.55));
}

float ribbonField(vec2 p, float time, float warp) {
    float sum = 0.0;
    float weight = 0.55;
    vec2 q = p;

    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        q = rotate(q, 0.45 + fi * 0.23 + time * (0.05 + fi * 0.004));
        q += 0.22 * vec2(
                sin(q.y * (2.1 + warp * 0.06) + time + fi * 0.9),
                cos(q.x * (2.4 + warp * 0.05) - time * 1.1 - fi * 0.6)
        );

        float line = 0.08 / (0.028 + abs(
                sin(q.x * (3.0 + warp * 0.03) + time * 1.4) +
                cos(q.y * (3.6 + warp * 0.025) - time * 1.1)
        ));
        float haze = exp(-2.2 * dot(q, q));
        sum += (line + haze * 0.42) * weight;

        q *= 1.34 + fi * 0.015;
        q = rotate(q, -0.78 - fi * 0.03);
        weight *= 0.66;
    }

    return sum;
}

float sparkleField(vec2 p, float time) {
    vec2 q = p * 4.0;
    vec2 cell = floor(q);
    vec2 fractCell = fract(q) - 0.5;
    float sparkle = 0.0;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 id = cell + vec2(x, y);
            float hash = hash21(id);
            vec2 offset = vec2(hash21(id + 1.3), hash21(id + 7.1)) - 0.5;
            float distanceToSpark = length(fractCell - vec2(x, y) - offset * 0.7);
            float twinkle = 0.5 + 0.5 * sin(time * 2.0 + hash * TAU);
            sparkle += smoothstep(0.16, 0.0, distanceToSpark) * twinkle * hash;
        }
    }

    return sparkle;
}

float nebulaField(vec2 p, float time) {
    float value = 0.0;
    float amplitude = 0.55;
    vec2 q = p;

    for (int i = 0; i < 5; i++) {
        float fi = float(i);
        q = rotate(q, 0.6 + fi * 0.35 + time * 0.03);
        q += vec2(
                sin(q.y * 1.8 + time * (0.4 + fi * 0.07)),
                cos(q.x * 1.6 - time * (0.5 + fi * 0.05))
        ) * 0.22;
        value += amplitude * exp(-1.6 * dot(q, q));
        q *= 1.7;
        amplitude *= 0.58;
    }

    return value;
}

void mainImage(out vec4 fragColor, vec2 fragCoord) {
    vec2 resolution = max(u_resolution, vec2(1.0));
    vec2 p = (fragCoord * 2.0 - resolution) / resolution.y;
    float time = u_time * 0.45;

    float radial = length(p);
    vec2 drift = 0.16 * vec2(sin(time + p.y * 3.0), cos(time * 0.8 - p.x * 3.6));
    vec2 q = rotate(p + drift, time * 0.08);

    float depthMask = smoothstep(1.45, 0.15, radial);

    float farField = ribbonField(q * 0.72, time * 0.72, 1.0);
    float midField = ribbonField(q * 1.12 + farField * 0.014, time, 2.0);
    float nearField = ribbonField(q * 1.95 + midField * 0.02, time * 1.18, 3.0);

    float nebula = nebulaField(rotate(p, -0.22) * 0.95, time);
    float glow = exp(-2.4 * radial * radial);
    float ring = pow(max(0.0, cos(radial * 9.5 - time * 1.55 + nearField * 0.055)), 7.0);
    float sparkles = sparkleField(q + nearField * 0.018, time) * (0.4 + glow);

    vec3 farColor = palette(farField * 0.032 - time * 0.10 + 0.8) * (0.18 + nebula * 0.65);
    vec3 midColor = palette(midField * 0.04 - time * 0.13 + 1.9) * (0.32 + glow * 0.75);
    vec3 nearColor = palette(nearField * 0.05 - time * 0.16 + 3.1) * (0.30 + ring * 0.45);

    vec3 color = farColor * 0.75 + midColor * 1.1 + nearColor * 1.25;
    color += palette(nebula * 0.12 + 4.4) * nebula * 0.55;
    color += vec3(1.0, 0.97, 0.92) * sparkles * 0.42;
    color += palette(radial * 0.6 + time * 0.05 + 5.1) * ring * 0.26;

    color *= depthMask;
    color = 1.0 - exp(-color * (1.35 + nearField * 0.08 + nebula * 0.18));
    color = pow(clamp(color, 0.0, 1.0), vec3(0.82));
    color = mix(vec3(dot(color, vec3(0.299, 0.587, 0.114))), color, 1.28);
    color = clamp(color * 1.15, 0.0, 1.0);

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

void main() {
    vec4 color;
    mainImage(color, uv * max(u_resolution, vec2(1.0)));

    vec3 shaderColor = clamp(color.rgb, 0.0, 1.0);
    out_color = vec4(shaderColor * u_tint.rgb, u_tint.a);
}
