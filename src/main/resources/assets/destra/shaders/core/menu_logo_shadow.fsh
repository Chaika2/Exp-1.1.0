#version 150

uniform sampler2D Sampler0;
uniform float u_opacity;
uniform vec2 u_texel;

in vec2 TexCoord;
in vec4 FragColor;

out vec4 OutColor;

float sampleAlpha(vec2 uv) {
    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
        return 0.0;
    }
    return texture(Sampler0, uv).a;
}

void main() {
    vec2 one = u_texel * 1.8;
    vec2 two = u_texel * 3.6;
    float alpha = sampleAlpha(TexCoord) * 0.12;
    alpha += sampleAlpha(TexCoord + vec2(one.x, 0.0)) * 0.10;
    alpha += sampleAlpha(TexCoord - vec2(one.x, 0.0)) * 0.10;
    alpha += sampleAlpha(TexCoord + vec2(0.0, one.y)) * 0.10;
    alpha += sampleAlpha(TexCoord - vec2(0.0, one.y)) * 0.10;
    alpha += sampleAlpha(TexCoord + one) * 0.075;
    alpha += sampleAlpha(TexCoord - one) * 0.075;
    alpha += sampleAlpha(TexCoord + vec2(one.x, -one.y)) * 0.075;
    alpha += sampleAlpha(TexCoord + vec2(-one.x, one.y)) * 0.075;
    alpha += sampleAlpha(TexCoord + vec2(two.x, 0.0)) * 0.04;
    alpha += sampleAlpha(TexCoord - vec2(two.x, 0.0)) * 0.04;
    alpha += sampleAlpha(TexCoord + vec2(0.0, two.y)) * 0.04;
    alpha += sampleAlpha(TexCoord - vec2(0.0, two.y)) * 0.04;

    alpha *= u_opacity * FragColor.a;
    if (alpha <= 0.001) {
        discard;
    }
    OutColor = vec4(0.0, 0.0, 0.0, alpha);
}
