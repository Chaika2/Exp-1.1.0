#version 330 core

in vec2 v_UV;
out vec4 fragColor;

uniform float u_Time;
uniform float u_Progress;
uniform vec4 u_Color;

#define MOD3 vec3(0.1031, 0.11369, 0.13787)

vec3 hash33(vec3 p3) {
    p3 = fract(p3 * MOD3);
    p3 += dot(p3, p3.yxz + 19.19);
    return -1.0 + 2.0 * fract(vec3(
        (p3.x + p3.y) * p3.z,
        (p3.x + p3.z) * p3.y,
        (p3.y + p3.z) * p3.x
    ));
}

float simplexNoise(vec3 p) {
    const float K1 = 0.333333333;
    const float K2 = 0.166666667;
    vec3 i = floor(p + (p.x + p.y + p.z) * K1);
    vec3 d0 = p - (i - (i.x + i.y + i.z) * K2);
    vec3 e = step(vec3(0.0), d0 - d0.yzx);
    vec3 i1 = e * (1.0 - e.zxy);
    vec3 i2 = 1.0 - e.zxy * (1.0 - e);
    vec3 d1 = d0 - (i1 - K2);
    vec3 d2 = d0 - (i2 - 2.0 * K2);
    vec3 d3 = d0 - (1.0 - 3.0 * K2);
    vec4 h = max(0.6 - vec4(dot(d0, d0), dot(d1, d1), dot(d2, d2), dot(d3, d3)), 0.0);
    vec4 n = h * h * h * h * vec4(
        dot(d0, hash33(i)),
        dot(d1, hash33(i + i1)),
        dot(d2, hash33(i + i2)),
        dot(d3, hash33(i + 1.0))
    );
    return dot(vec4(31.316), n);
}

void main() {
    vec2 uv = v_UV - 0.5;
    float angleWave = sin(atan(uv.y, uv.x));
    float angularMask = abs(angleWave - 0.5) / 4.0;
    float radius = length(uv);
    float innerMask = clamp(0.1 / smoothstep(0.0, 1.75, radius), 0.0, 1.0);
    float outerMask = clamp(0.1 / smoothstep(0.42, 0.0, radius), 0.0, 1.0);
    float noise1 = simplexNoise(vec3(uv * 2.0, 1.0 + u_Time * 0.525))
            * max(1.0 - radius * 1.75, 0.0) + 0.9;
    float noise2 = simplexNoise(vec3(uv, 15.0 + u_Time * 0.525))
            * max(radius, 0.025) + 1.25;
    float noise3 = simplexNoise(vec3(vec2(angularMask, angularMask * 100.0 + u_Time) * 0.15,
            30.0 + u_Time * 0.525)) * max(radius, 0.025) + 1.25;
    noise3 *= smoothstep(0.0, 0.3345, radius);
    float shape = innerMask * outerMask * (noise1 * noise2 * noise3)
            * (1.0 - radius)
            * smoothstep(0.15, 0.35, radius)
            * smoothstep(0.75, 0.3, radius);
    shape *= shape;
    float fade = 1.0 - smoothstep(0.6, 1.0, u_Progress);
    float border = 1.0 - smoothstep(0.4, 0.5, radius);
    vec3 result = u_Color.rgb * shape * border * fade;
    fragColor = vec4(result, shape * fade * u_Color.a);
}
