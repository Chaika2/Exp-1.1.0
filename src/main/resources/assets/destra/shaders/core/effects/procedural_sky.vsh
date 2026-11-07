#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 effectDirection;

void main() {
    vec3 direction = normalize(Position);
    effectDirection = direction;
    texCoord = vec2(atan(direction.z, direction.x) / 6.2831853 + 0.5,
                    asin(clamp(direction.y, -1.0, 1.0)) / 3.14159265 + 0.5);
    vertexColor = vec4(1.0);

    mat4 skyView = ModelViewMat;
    skyView[3].xyz = vec3(0.0);
    vec4 clip = ProjMat * skyView * vec4(Position, 1.0);
    gl_Position = clip.xyww;
}
