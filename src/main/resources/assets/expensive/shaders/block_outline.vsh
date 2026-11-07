#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aSurfacePos;

uniform mat4 u_modelViewProjection;

out vec2 uv;

vec2 signNotZero(vec2 v) {
    return vec2(v.x >= 0.0 ? 1.0 : -1.0, v.y >= 0.0 ? 1.0 : -1.0);
}

vec2 octahedralUv(vec3 p) {
    vec3 n = p / max(abs(p.x) + abs(p.y) + abs(p.z), 0.0001);
    vec2 mapped = n.xy;
    if (n.z < 0.0) {
        mapped = (1.0 - abs(mapped.yx)) * signNotZero(mapped);
    }
    mapped.y = -mapped.y;
    return mapped * 0.5 + 0.5;
}

void main() {
    uv = octahedralUv(aSurfacePos * 2.0 - 1.0);
    gl_Position = u_modelViewProjection * vec4(aPosition, 1.0);
}
