#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 skyDir;

void main() {
    skyDir = normalize(Position);

    mat4 skyView = ModelViewMat;
    skyView[3].xyz = vec3(0.0);

    vec4 clip = ProjMat * skyView * vec4(Position, 1.0);
    gl_Position = clip.xyww;
}
