#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 u_ModelMat;
uniform mat4 u_ProjMat;

out vec2 v_UV;

void main() {
    gl_Position = u_ProjMat * u_ModelMat * vec4(Position, 1.0);
    v_UV = UV0;
}
