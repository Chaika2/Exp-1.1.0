#version 150

#moj_import <destra:common.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 FragCoord;
out vec2 TexCoord;
out vec2 GlobalPos;
out float CornerRadius;
out vec4 FragColor;

void main() {
    FragCoord = rvertexcoord(gl_VertexID);
    TexCoord = UV0;
    GlobalPos = Position.xy;
    CornerRadius = Position.z;
    FragColor = Color;

    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
}
