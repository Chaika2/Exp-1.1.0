#version 150

uniform sampler2D Sampler0;
uniform vec4 fillColor;
uniform vec4 outlineColor;
uniform float radius;
uniform float thickness;
uniform float start;
uniform float end;
uniform float outlineWidth;
uniform float UseBlur;
uniform vec2 size;
uniform vec2 location;
uniform vec2 FramebufferResolution;

out vec4 fragColor;

const float PI = 3.141592653589793;
const float RAD = 0.017453292519943;

void main() {
    float startAngle = start * RAD;
    float endAngle = startAngle + end * RAD;
    float smoothThreshold = 6.0 / length(size);
    vec2 centerPosition = ((gl_FragCoord.xy - location) / size) * 2.0 - 1.0;
    float distanceToCenter = length(centerPosition);
    float angle = atan(centerPosition.y, centerPosition.x) + PI;
    float innerRadius = radius;
    float outerRadius = radius + thickness;

    float bandAlpha = smoothstep(innerRadius, innerRadius + smoothThreshold, distanceToCenter)
            * smoothstep(outerRadius, outerRadius - smoothThreshold, distanceToCenter);

    float angleAlpha;
    if (end >= 360.0) {
        angleAlpha = 1.0;
    } else if (endAngle <= PI * 2.0) {
        angleAlpha = smoothstep(angle, angle - smoothThreshold, startAngle)
                * smoothstep(angle, angle + smoothThreshold, endAngle);
    } else {
        float wrappedEnd = endAngle - PI * 2.0;
        float pastStart = smoothstep(angle, angle - smoothThreshold, startAngle);
        float beforeEnd = 1.0 - smoothstep(wrappedEnd - smoothThreshold, wrappedEnd, angle);
        angleAlpha = max(pastStart, beforeEnd);
    }

    float maskAlpha = bandAlpha * angleAlpha;
    if (maskAlpha <= 0.0) {
        discard;
    }

    float distanceToArc = min(abs(distanceToCenter - innerRadius), abs(distanceToCenter - outerRadius));
    float distanceToRadial;
    if (end >= 360.0) {
        distanceToRadial = 1.0;
    } else {
        float startDistance = abs(distanceToCenter * sin(angle - startAngle));
        float endDistance = abs(distanceToCenter * sin(angle - endAngle));
        if (cos(angle - startAngle) < 0.0) startDistance = 1.0;
        if (cos(angle - endAngle) < 0.0) endDistance = 1.0;
        distanceToRadial = min(startDistance, endDistance);
    }

    float edgeDistance = min(distanceToArc, distanceToRadial);
    float halfOutline = outlineWidth * 0.5;
    float outlineAlpha = 1.0 - smoothstep(
            halfOutline - smoothThreshold,
            halfOutline + smoothThreshold,
            edgeDistance
    );
    outlineAlpha *= maskAlpha;

    vec2 uv = gl_FragCoord.xy / max(FramebufferResolution, vec2(1.0));
    vec3 blurred = UseBlur > 0.5 ? texture(Sampler0, uv).rgb : fillColor.rgb;
    vec3 color = mix(blurred, fillColor.rgb, fillColor.a);
    color = mix(color, outlineColor.rgb, clamp(outlineAlpha * outlineColor.a, 0.0, 1.0));
    fragColor = vec4(color, maskAlpha);
}
