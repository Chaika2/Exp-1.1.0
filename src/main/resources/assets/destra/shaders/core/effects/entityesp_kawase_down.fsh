#version 150

uniform sampler2D SourceTexture;
uniform vec2 uOffset;
uniform vec2 uHalfPixel;
uniform vec2 uSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 halfPixel = uHalfPixel * uOffset;

    vec4 sum = texture(SourceTexture, texCoord) * 4.0;
    sum += texture(SourceTexture, texCoord - halfPixel);
    sum += texture(SourceTexture, texCoord + halfPixel);
    sum += texture(SourceTexture, texCoord + vec2(halfPixel.x, -halfPixel.y));
    sum += texture(SourceTexture, texCoord - vec2(halfPixel.x, -halfPixel.y));

    fragColor = sum / 8.0;
}
