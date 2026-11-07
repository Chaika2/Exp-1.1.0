#version 150

in vec2 TexCoord;
in vec4 FragColor;
in vec2 GlobalPos;

uniform sampler2D Sampler0;
uniform float Range;
uniform float Thickness;
uniform float Smoothness;
uniform bool Outline;
uniform float OutlineThickness;
uniform vec4 OutlineColor;
uniform int EnableFadeout;
uniform float FadeInStart;
uniform float FadeInEnd;
uniform float FadeoutStart;
uniform float FadeoutEnd;
uniform float MaxWidth;
uniform float TextPosX;

out vec4 OutColor;

void main() {
    float dist = texture(Sampler0, TexCoord).a - 0.5 + Thickness;
    vec2 h = vec2(dFdx(TexCoord.x), dFdy(TexCoord.y)) * textureSize(Sampler0, 0);
    float pixels = Range * inversesqrt(h.x * h.x + h.y * h.y);
    float alpha = smoothstep(-Smoothness, Smoothness, dist * pixels);
    vec4 color = vec4(FragColor.rgb, FragColor.a * alpha);

    if (Outline) {
        color = mix(OutlineColor, FragColor, alpha);
        color.a *= smoothstep(-Smoothness, Smoothness, (dist + OutlineThickness) * pixels);
    }

    if (EnableFadeout > 0 && MaxWidth > 0.0) {
        float fadeAlpha = 1.0;
        float normalizedX = (GlobalPos.x - TextPosX) / MaxWidth;

        if (FadeInEnd > FadeInStart && normalizedX < FadeInEnd) {
            fadeAlpha *= smoothstep(FadeInStart, FadeInEnd, normalizedX);
        }
        if (normalizedX > FadeoutStart) {
            fadeAlpha *= 1.0 - smoothstep(FadeoutStart, FadeoutEnd, normalizedX);
        }

        color.a *= fadeAlpha;
    }

    if (color.a <= 0.01) {
        discard;
    }

    OutColor = color;
}
