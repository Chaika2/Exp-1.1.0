#version 150

in vec2 TexCoord;
in vec4 FragColor;
in vec2 LocalPos;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Range;
uniform float Thickness;
uniform float Smoothness;
uniform vec4 LogoRect;
uniform float LogoThreshold;
uniform float LogoUvMaxY;
uniform float UseLogoMask;
uniform vec4 ClipRect;
uniform float ClipRadius;

out vec4 OutColor;

float median(vec3 color) {
    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));
}

void main() {
    float maskAlpha;
    if (UseLogoMask > 0.5) {
        vec2 logoUv = (LocalPos - LogoRect.xy) / max(LogoRect.zw, vec2(1.0));
        if (logoUv.x < 0.0 || logoUv.x > 1.0 || logoUv.y < 0.0 || logoUv.y > 1.0) {
            discard;
        }

        logoUv.y *= clamp(LogoUvMaxY, 0.0, 1.0);
        vec4 logo = texture(Sampler1, logoUv);
        float logoMask = logo.a * max(max(logo.r, logo.g), logo.b);
        maskAlpha = smoothstep(LogoThreshold, LogoThreshold + 0.08, logoMask);
    } else {
        vec2 clipSize = max(ClipRect.zw, vec2(1.0));
        float radius = clamp(ClipRadius, 0.0, min(clipSize.x, clipSize.y) * 0.5);
        vec2 halfSize = clipSize * 0.5;
        vec2 local = abs(LocalPos - (ClipRect.xy + halfSize)) - (halfSize - vec2(radius));
        float distanceToEdge = length(max(local, vec2(0.0)))
                + min(max(local.x, local.y), 0.0) - radius;
        maskAlpha = 1.0 - smoothstep(-0.65, 0.65, distanceToEdge);
    }

    if (maskAlpha <= 0.001) {
        discard;
    }

    float dist = median(texture(Sampler0, TexCoord).rgb) - 0.5 + Thickness;
    vec2 h = vec2(dFdx(TexCoord.x), dFdy(TexCoord.y)) * textureSize(Sampler0, 0);
    float pixels = Range * inversesqrt(h.x * h.x + h.y * h.y);
    float glyphAlpha = smoothstep(-Smoothness, Smoothness, dist * pixels);

    float alpha = FragColor.a * glyphAlpha * maskAlpha;
    if (alpha <= 0.01) {
        discard;
    }

    OutColor = vec4(FragColor.rgb, alpha);
}
