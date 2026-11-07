#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 vertexPos;

void main() {
    vertexPos = Position.xy;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
