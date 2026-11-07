#version 150

uniform sampler2D BlurredTexture;
uniform sampler2D MaskTexture;
uniform vec3 color;
uniform vec3 color2;
uniform float exposure;
uniform float opacity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 bloom = texture(BlurredTexture, texCoord);
    vec4 mask = texture(MaskTexture, texCoord);
    float outer = bloom.a * (1.0 - mask.a);
    vec3 grad = mix(color, color2, texCoord.y);
    float intensity = clamp(outer * exposure * opacity, 0.0, 1.0);

    if (intensity <= 0.001) {
        discard;
    }

    fragColor = vec4(grad, intensity);
}
