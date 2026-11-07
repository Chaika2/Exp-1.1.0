#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uTexelSize;
uniform float uOffset;
uniform float uAlphaMultiplier;

void main() {
    vec2 d = uTexelSize * uOffset;

    vec4 color = vec4(0.0);
    color += texture(uTexture, texCoord + vec2(-d.x, 0.0));
    color += texture(uTexture, texCoord + vec2( d.x, 0.0));
    color += texture(uTexture, texCoord + vec2(0.0, -d.y));
    color += texture(uTexture, texCoord + vec2(0.0,  d.y));

    color += texture(uTexture, texCoord + vec2(-d.x, -d.y)) * 2.0;
    color += texture(uTexture, texCoord + vec2( d.x, -d.y)) * 2.0;
    color += texture(uTexture, texCoord + vec2(-d.x,  d.y)) * 2.0;
    color += texture(uTexture, texCoord + vec2( d.x,  d.y)) * 2.0;

    color *= (1.0 / 12.0);
    fragColor = vec4(color.rgb, color.a * uAlphaMultiplier);
}
