#version 150

uniform sampler2D Sampler0;
uniform vec3 u_accent;
uniform float u_opacity;

in vec2 TexCoord;
in vec4 FragColor;

out vec4 OutColor;

void main() {
    vec4 source = texture(Sampler0, TexCoord) * FragColor;
    float brightest = max(max(source.r, source.g), source.b);
    float luminance = dot(source.rgb, vec3(0.2126, 0.7152, 0.0722));
    float detail = mix(luminance, brightest, 0.82);
    float accentPeak = max(max(u_accent.r, u_accent.g), max(u_accent.b, 0.001));
    vec3 accentHue = u_accent / accentPeak;
    vec3 tinted = clamp(accentHue * detail, 0.0, 1.0);
    OutColor = vec4(tinted, source.a * clamp(u_opacity, 0.0, 1.0));
}
