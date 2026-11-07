#version 150

in vec2 FragCoord;

uniform sampler2D Sampler0;
uniform vec2 PanelSize;
uniform vec4 TintColor;
uniform vec4 OutlineColor;
uniform vec4 Radii;
uniform float OutlineWidth;
uniform float FresnelPower;
uniform float Distortion;
uniform float Opacity;
uniform float UseBlur;
uniform vec2 FramebufferResolution;

out vec4 fragColor;

float roundedBoxSdf(vec2 point, vec2 halfSize, vec4 radii) {
    float radius = point.x > 0.0
            ? (point.y > 0.0 ? radii.z : radii.y)
            : (point.y > 0.0 ? radii.w : radii.x);
    float safeRadius = min(max(radius, 0.0), min(halfSize.x, halfSize.y));
    vec2 edge = abs(point) - halfSize + safeRadius;
    return min(max(edge.x, edge.y), 0.0)
            + length(max(edge, vec2(0.0))) - safeRadius;
}

void main() {
    vec2 safeSize = max(PanelSize, vec2(1.0));
    vec2 halfSize = safeSize * 0.5;
    vec2 localPosition = FragCoord * safeSize - halfSize;
    float distance = roundedBoxSdf(localPosition, halfSize, Radii);
    float antialias = clamp(fwidth(distance), 0.55, 1.5);
    float mask = 1.0 - smoothstep(-antialias, antialias, distance);

    if (mask <= 0.001) {
        discard;
    }

    float innerDistance = max(-distance, 0.0);
    float edgeRange = max(min(halfSize.x, halfSize.y), 0.001);
    float edgeGradient = 1.0 - clamp(innerDistance / edgeRange, 0.0, 1.0);
    float fresnel = pow(clamp(edgeGradient, 0.001, 1.0), max(FresnelPower, 0.25));

    vec2 direction = length(localPosition) > 0.001
            ? normalize(localPosition)
            : vec2(0.0);
    vec2 framebufferSize = max(FramebufferResolution, vec2(1.0));
    vec2 screenUv = gl_FragCoord.xy / framebufferSize;
    vec2 refractedUv = screenUv + vec2(direction.x, -direction.y)
            * fresnel * max(Distortion, 0.0);
    vec2 halfTexel = 0.5 / framebufferSize;
    refractedUv = clamp(refractedUv, halfTexel, vec2(1.0) - halfTexel);

    bool hasScene = UseBlur > 0.5;
    vec3 scene = hasScene ? texture(Sampler0, refractedUv).rgb : TintColor.rgb;
    float tintMix = hasScene ? clamp(TintColor.a, 0.0, 1.0) : 1.0;
    vec3 result = mix(scene, TintColor.rgb, tintMix);

    float fresnelMix = fresnel * clamp(OutlineColor.a, 0.0, 1.0);
    result = mix(result, OutlineColor.rgb, fresnelMix);

    float border = 0.0;
    if (OutlineWidth > 0.001) {
        border = 1.0 - smoothstep(
                max(0.0, OutlineWidth - antialias),
                OutlineWidth + antialias,
                innerDistance
        );
        border *= mask;
        result = mix(result, OutlineColor.rgb,
                clamp(border * OutlineColor.a, 0.0, 1.0));
    }

    float bodyAlpha = hasScene ? 1.0 : clamp(TintColor.a, 0.0, 1.0);
    float surfaceAlpha = max(bodyAlpha, border * clamp(OutlineColor.a, 0.0, 1.0));
    float finalAlpha = mask * surfaceAlpha * clamp(Opacity, 0.0, 1.0);
    if (finalAlpha <= 0.001) {
        discard;
    }

    fragColor = vec4(result, finalAlpha);
}
