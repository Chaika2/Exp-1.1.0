#version 330 core

in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Resolution;
uniform float Offset;
uniform float Saturation;
uniform float TintIntensity;
uniform vec3 TintColor;

out vec4 OutColor;

vec3 adjustSaturation(vec3 color, float saturation) {
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    return mix(vec3(gray), color, saturation);
}

void main() {
    vec2 texel = Resolution;
    float off = Offset + 0.5;

    vec2 d = texel * off;
    vec3 sum = texture(Sampler0, TexCoord + vec2(-2.0 * d.x, 0.0)).rgb;
    sum += texture(Sampler0, TexCoord + vec2(-d.x, d.y)).rgb * 2.0;
    sum += texture(Sampler0, TexCoord + vec2(0.0, 2.0 * d.y)).rgb;
    sum += texture(Sampler0, TexCoord + vec2(d.x, d.y)).rgb * 2.0;
    sum += texture(Sampler0, TexCoord + vec2(2.0 * d.x, 0.0)).rgb;
    sum += texture(Sampler0, TexCoord + vec2(d.x, -d.y)).rgb * 2.0;
    sum += texture(Sampler0, TexCoord + vec2(0.0, -2.0 * d.y)).rgb;
    sum += texture(Sampler0, TexCoord + vec2(-d.x, -d.y)).rgb * 2.0;

    vec3 color = sum / 12.0;
    color = adjustSaturation(color, Saturation);
    color = mix(color, TintColor, TintIntensity);

    OutColor = vec4(color, 1.0) * FragColor;
}
