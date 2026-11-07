#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

flat out int ShapeIndex;

void main() {
    ShapeIndex = gl_VertexID / 4;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
