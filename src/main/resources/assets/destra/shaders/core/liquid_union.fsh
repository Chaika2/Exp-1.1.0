#version 150

in vec2 FragCoord;

uniform sampler2D Sampler0;
uniform vec2 boundsSize;
uniform vec4 blob0;
uniform vec4 blob1;
uniform vec4 fillColor;
uniform vec4 outlineColor;
uniform float radius0;
uniform float radius1;
uniform float mergeRadius;
uniform float outlineWidth;
uniform float opacity;
uniform float UseBlur;
uniform float GlassMode;
uniform float FresnelPower;
uniform float Distortion;
uniform vec2 FramebufferResolution;

out vec4 fragColor;

float roundedBoxSdf(vec2 point, vec4 rect, float radius) {
    vec2 halfSize = rect.zw * 0.5;
    vec2 center = point - rect.xy - halfSize;
    float safeRadius = min(max(radius, 0.0), min(halfSize.x, halfSize.y));
    vec2 edge = abs(center) - halfSize + safeRadius;
    return min(max(edge.x, edge.y), 0.0) + length(max(edge, vec2(0.0))) - safeRadius;
}

float smoothUnion(float first, float second, float radius) {
    if (radius <= 0.001) return min(first, second);
    float blend = clamp(0.5 + 0.5 * (second - first) / radius, 0.0, 1.0);
    return mix(second, first, blend) - radius * blend * (1.0 - blend);
}

void main() {
    vec2 point = FragCoord * boundsSize;
    float firstDistance = roundedBoxSdf(point, blob0, radius0);
    float secondDistance = roundedBoxSdf(point, blob1, radius1);

    vec2 firstMin = blob0.xy;
    vec2 firstMax = blob0.xy + blob0.zw;
    vec2 secondMin = blob1.xy;
    vec2 secondMax = blob1.xy + blob1.zw;
    vec2 separationVector = max(max(firstMin - secondMax, secondMin - firstMax), vec2(0.0));
    float separation = length(separationVector);
    float mergeDistance = max(1.0, mergeRadius * 0.72);
    float proximity = 1.0 - smoothstep(0.0, mergeDistance, separation);
    float effectiveMergeRadius = mergeRadius * proximity;
    float distance = smoothUnion(firstDistance, secondDistance, effectiveMergeRadius);
    float antialias = clamp(fwidth(distance), 0.55, 1.35);
    float mask = 1.0 - smoothstep(-antialias, antialias, distance);
    vec2 surfaceGradient = vec2(dFdx(distance), dFdy(distance));

    if (mask <= 0.001) discard;

    vec2 framebufferSize = max(FramebufferResolution, vec2(1.0));

    if (GlassMode > 0.5) {
        bool hasScene = UseBlur > 0.5;
        float innerDistance = max(-distance, 0.0);
        float edgeRange = max(min(blob0.w, blob1.w) * 0.5, 0.001);
        float edgeGradient = 1.0 - clamp(innerDistance / edgeRange, 0.0, 1.0);
        float fresnel = pow(clamp(edgeGradient, 0.001, 1.0), max(FresnelPower, 0.25));

        vec2 direction = length(surfaceGradient) > 0.00001
                ? normalize(surfaceGradient)
                : vec2(0.0);
        vec2 halfTexel = 0.5 / framebufferSize;
        vec2 glassUv = clamp(gl_FragCoord.xy / framebufferSize
                + direction * fresnel * max(Distortion, 0.0),
                halfTexel, vec2(1.0) - halfTexel);

        vec3 glassScene = hasScene ? texture(Sampler0, glassUv).rgb : fillColor.rgb;
        vec3 glassBody = mix(glassScene, fillColor.rgb,
                hasScene ? clamp(fillColor.a, 0.0, 1.0) : 1.0);
        glassBody = mix(glassBody, outlineColor.rgb, fresnel * clamp(outlineColor.a, 0.0, 1.0));

        float glassBorder = 0.0;
        if (outlineWidth > 0.001) {
            glassBorder = 1.0 - smoothstep(
                    max(0.0, outlineWidth - antialias),
                    outlineWidth + antialias,
                    innerDistance
            );
            glassBorder *= mask;
            glassBody = mix(glassBody, outlineColor.rgb,
                    clamp(glassBorder * outlineColor.a, 0.0, 1.0));
        }

        float glassBodyAlpha = hasScene ? 1.0 : clamp(fillColor.a, 0.0, 1.0);
        float glassAlpha = mask * clamp(opacity, 0.0, 1.0)
                * max(glassBodyAlpha, glassBorder * clamp(outlineColor.a, 0.0, 1.0));
        if (glassAlpha <= 0.001) discard;

        fragColor = vec4(glassBody, glassAlpha);
        return;
    }

    vec2 screenUv = gl_FragCoord.xy / framebufferSize;
    vec3 scene = UseBlur > 0.5 ? texture(Sampler0, screenUv).rgb : fillColor.rgb;
    vec3 body = UseBlur > 0.5
            ? mix(scene, fillColor.rgb, clamp(fillColor.a, 0.0, 1.0))
            : fillColor.rgb;

    float border = 1.0 - smoothstep(
            max(0.0, outlineWidth - antialias),
            outlineWidth + antialias,
            abs(distance)
    );
    border *= mask * outlineColor.a;
    body = mix(body, outlineColor.rgb, clamp(border, 0.0, 1.0));

    float baseAlpha = UseBlur > 0.5 ? 1.0 : fillColor.a;
    fragColor = vec4(body, mask * baseAlpha * clamp(opacity, 0.0, 1.0));
}
