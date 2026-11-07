#version 150

in vec3 Position;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 effectDirection;

void main() {
    gl_Position = vec4(Position, 1.0);
    texCoord = Position.xy * 0.5 + 0.5;
    vertexColor = vec4(1.0);
    effectDirection = normalize(vec3(Position.xy, 1.0));
}
