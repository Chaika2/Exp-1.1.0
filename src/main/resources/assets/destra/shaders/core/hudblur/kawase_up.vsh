#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

out vec2 TexCoord;
out vec4 FragColor;

void main() {
    gl_Position = vec4(Position, 1.0);
    TexCoord = UV0;
    FragColor = Color;
}
