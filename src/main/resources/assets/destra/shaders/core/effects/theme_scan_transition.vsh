#version 330 core

layout(location = 0) in vec3 Position;

out vec2 v_Uv;

void main() {
    gl_Position = vec4(Position, 1.0);
    v_Uv = Position.xy * 0.5 + 0.5;
}
