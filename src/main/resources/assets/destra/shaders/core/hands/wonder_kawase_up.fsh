#version 150

uniform sampler2D SourceTexture;
uniform vec2 uOffset;
uniform vec2 uHalfPixel;
uniform vec2 uSize;
uniform vec3 color;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 halfPixel = uHalfPixel * uOffset;

    vec4 sum = texture(SourceTexture, texCoord + vec2(-halfPixel.x * 2.0, 0.0));
    sum += texture(SourceTexture, texCoord + vec2(-halfPixel.x, halfPixel.y)) * 2.0;
    sum += texture(SourceTexture, texCoord + vec2(0.0, halfPixel.y * 2.0));
    sum += texture(SourceTexture, texCoord + vec2(halfPixel.x, halfPixel.y)) * 2.0;
    sum += texture(SourceTexture, texCoord + vec2(halfPixel.x * 2.0, 0.0));
    sum += texture(SourceTexture, texCoord + vec2(halfPixel.x, -halfPixel.y)) * 2.0;
    sum += texture(SourceTexture, texCoord + vec2(0.0, -halfPixel.y * 2.0));
    sum += texture(SourceTexture, texCoord + vec2(-halfPixel.x, -halfPixel.y)) * 2.0;

    vec4 result = sum / 12.0;
    fragColor = vec4(result.rgb * color, result.a);
}
