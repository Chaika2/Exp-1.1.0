#version 150

uniform sampler2D Sampler0;
uniform float u_opacity;
uniform vec3 u_accent;
uniform float u_visuals_tint;
uniform float u_preserve_color;
uniform float u_full_tint;
uniform float u_uv_max_y;

in vec2 TexCoord;
in vec4 FragColor;

out vec4 OutColor;

void main() {
    vec2 uv = TexCoord;

    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
        discard;
    }

    vec2 sampleUv = vec2(uv.x, uv.y * clamp(u_uv_max_y, 0.0, 1.0));
    vec4 logo = texture(Sampler0, sampleUv) * FragColor;
    if (logo.a <= 0.001) {
        discard;
    }

    float preserveColor = clamp(u_preserve_color, 0.0, 1.0);
    float logoMask = logo.a * mix(max(max(logo.r, logo.g), logo.b), 1.0, preserveColor);
    float visualsMask = smoothstep(0.68, 0.72, sampleUv.y) * u_visuals_tint;
    float fullTint = clamp(u_full_tint, 0.0, 1.0);
    vec3 logoColor = mix(vec3(1.0), logo.rgb, preserveColor);
    logoColor = mix(logoColor, u_accent, visualsMask * (1.0 - fullTint));
    logoColor = mix(logoColor, logo.rgb * u_accent, fullTint);
    OutColor = vec4(logoColor, logoMask * clamp(u_opacity, 0.0, 1.0));
}
