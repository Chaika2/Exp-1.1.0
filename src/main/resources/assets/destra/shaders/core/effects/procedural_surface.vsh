#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 effectDirection;

void main() {
    texCoord = UV0;
    vertexColor = Color;
    effectDirection = normalize(Position + vec3(0.0001));
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
