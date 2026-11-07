#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Thickness;
uniform float Progress;
uniform float Smoothness;

out vec4 OutColor;

const float PI = 3.14159265358979323846;
const float TAU = 6.28318530717958647692;

float roundedBoxSdf(vec2 centered, vec2 halfSize, vec4 radius) {
    vec4 localRadius = radius;
    localRadius.xy = (centered.x > 0.0) ? localRadius.xy : localRadius.wz;
    localRadius.x = (centered.y > 0.0) ? localRadius.x : localRadius.y;

    vec2 q = abs(centered) - halfSize + localRadius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - localRadius.x;
}

float outlineAlpha(vec2 centered, vec2 halfSize, vec4 radius, float thickness, float smoothness) {
    vec4 clampedRadius = min(radius, vec4(min(halfSize.x, halfSize.y)));
    float outer = roundedBoxSdf(centered, halfSize, clampedRadius);

    vec2 innerHalf = max(halfSize - vec2(thickness), vec2(0.0001));
    vec4 innerRadius = max(clampedRadius - thickness, 0.0);
    float inner = roundedBoxSdf(centered, innerHalf, innerRadius);

    float aa = clamp(fwidth(outer) * max(smoothness, 0.25), 0.3, 0.75);
    float outerAlpha = 1.0 - smoothstep(-aa, aa, outer);
    float innerAlpha = 1.0 - smoothstep(-aa, aa, inner);
    return clamp(outerAlpha - innerAlpha, 0.0, 1.0);
}

float rectPerimeterCoord(vec2 centered, vec2 halfSize, float radius) {
    float ex = max(halfSize.x - radius, 0.0);
    float ey = max(halfSize.y - radius, 0.0);
    float topLen = ex * 2.0;
    float rightLen = ey * 2.0;
    float bottomLen = topLen;
    float leftLen = rightLen;
    float arcLen = 0.5 * PI * radius;

    if (centered.y <= -ey && abs(centered.x) <= ex) {
        return centered.x + ex;
    }

    if (centered.x >= ex && abs(centered.y) <= ey) {
        return topLen + arcLen + (centered.y + ey);
    }

    if (centered.y >= ey && abs(centered.x) <= ex) {
        return topLen + arcLen + rightLen + arcLen + (ex - centered.x);
    }

    if (centered.x <= -ex && abs(centered.y) <= ey) {
        return topLen + arcLen + rightLen + arcLen + bottomLen + arcLen + (ey - centered.y);
    }

    if (centered.x > ex && centered.y < -ey) {
        vec2 local = centered - vec2(ex, -ey);
        float angle = atan(local.y, local.x);
        return topLen + (angle + 0.5 * PI) * radius;
    }

    if (centered.x > ex && centered.y > ey) {
        vec2 local = centered - vec2(ex, ey);
        float angle = atan(local.y, local.x);
        return topLen + arcLen + rightLen + angle * radius;
    }

    if (centered.x < -ex && centered.y > ey) {
        vec2 local = centered - vec2(-ex, ey);
        float angle = atan(local.y, local.x);
        return topLen + arcLen + rightLen + arcLen + bottomLen + (angle - 0.5 * PI) * radius;
    }

    vec2 local = centered - vec2(-ex, -ey);
    float angle = atan(local.y, local.x);
    if (angle < 0.0) {
        angle += TAU;
    }
    return topLen + arcLen + rightLen + arcLen + bottomLen + arcLen + leftLen + (angle - PI) * radius;
}

float progressAlpha(vec2 centered, vec2 halfSize, float radius, float progress) {
    if (progress >= 0.9999) {
        return 1.0;
    }

    float ex = max(halfSize.x - radius, 0.0);
    float ey = max(halfSize.y - radius, 0.0);
    float perimeter = 4.0 * (ex + ey) + 2.0 * PI * radius;
    float s = rectPerimeterCoord(centered, halfSize, radius);
    float target = clamp(progress, 0.0, 1.0) * perimeter;
    float aa = max(fwidth(s), 0.75);

    return 1.0 - smoothstep(target - aa, target + aa, s);
}

void main() {
    vec2 centered = (FragCoord - 0.5) * Size;
    vec2 halfSize = Size * 0.5 - vec2(0.6);
    float radius = min(Radius.x, min(halfSize.x, halfSize.y));
    float progressRadius = max(radius - Thickness * 0.5, 0.0);

    float alpha = outlineAlpha(centered, halfSize, vec4(radius), Thickness, Smoothness)
            * progressAlpha(centered, max(halfSize - vec2(Thickness * 0.5), vec2(0.0001)), progressRadius, clamp(Progress, 0.0, 1.0));

    if (alpha < 0.001) {
        discard;
    }

    OutColor = vec4(FragColor.rgb, FragColor.a * alpha);
}
