#version 150

#define MAX_BATCH 30
#define STRIDE 30

uniform float shapeData[MAX_BATCH * STRIDE];

flat in int ShapeIndex;

out vec4 fragColor;

const float PI = 3.14159265358979323846;
const float TAU = 6.28318530717958647692;

float roundedBoxSDF(vec2 p, vec2 halfSize, vec4 radius) {
    float cornerRadius = p.x > 0.0
            ? (p.y > 0.0 ? radius.y : radius.z)
            : (p.y > 0.0 ? radius.x : radius.w);
    vec2 q = abs(p) - halfSize + cornerRadius;
    return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - cornerRadius;
}

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4) {
    vec2 clamped = clamp(coords, 0.0, 1.0);
    vec4 top = mix(color1, color3, clamped.x);
    vec4 bottom = mix(color2, color4, clamped.x);
    return mix(bottom, top, clamped.y);
}

float radialOutlineAlpha(vec2 centered, vec2 halfSize, float radius, float thickness, float smoothness) {
    float clampedRadius = min(radius, min(halfSize.x, halfSize.y));
    float outer = roundedBoxSDF(centered, halfSize, vec4(clampedRadius));
    vec2 innerHalf = max(halfSize - vec2(thickness), vec2(0.0001));
    float innerRadius = max(clampedRadius - thickness, 0.0);
    float inner = roundedBoxSDF(centered, innerHalf, vec4(innerRadius));
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

    if (centered.y <= -ey && abs(centered.x) <= ex) return centered.x + ex;
    if (centered.x >= ex && abs(centered.y) <= ey) return topLen + arcLen + centered.y + ey;
    if (centered.y >= ey && abs(centered.x) <= ex) return topLen + arcLen + rightLen + arcLen + ex - centered.x;
    if (centered.x <= -ex && abs(centered.y) <= ey) return topLen + arcLen + rightLen + arcLen + bottomLen + arcLen + ey - centered.y;

    if (centered.x > ex && centered.y < -ey) {
        vec2 corner = centered - vec2(ex, -ey);
        return topLen + (atan(corner.y, corner.x) + 0.5 * PI) * radius;
    }
    if (centered.x > ex && centered.y > ey) {
        vec2 corner = centered - vec2(ex, ey);
        return topLen + arcLen + rightLen + atan(corner.y, corner.x) * radius;
    }
    if (centered.x < -ex && centered.y > ey) {
        vec2 corner = centered - vec2(-ex, ey);
        return topLen + arcLen + rightLen + arcLen + bottomLen
                + (atan(corner.y, corner.x) - 0.5 * PI) * radius;
    }

    vec2 corner = centered - vec2(-ex, -ey);
    float angle = atan(corner.y, corner.x);
    if (angle < 0.0) angle += TAU;
    return topLen + arcLen + rightLen + arcLen + bottomLen + arcLen + leftLen
            + (angle - PI) * radius;
}

float radialProgressAlpha(vec2 centered, vec2 halfSize, float radius, float progress) {
    if (progress >= 0.9999) return 1.0;
    float ex = max(halfSize.x - radius, 0.0);
    float ey = max(halfSize.y - radius, 0.0);
    float perimeter = 4.0 * (ex + ey) + 2.0 * PI * radius;
    float perimeterPosition = rectPerimeterCoord(centered, halfSize, radius);
    float target = clamp(progress, 0.0, 1.0) * perimeter;
    float aa = max(fwidth(perimeterPosition), 0.75);
    return 1.0 - smoothstep(target - aa, target + aa, perimeterPosition);
}

void main() {
    int base = ShapeIndex * STRIDE;

    vec2 location = vec2(shapeData[base], shapeData[base + 1]);
    vec2 size = max(vec2(shapeData[base + 2], shapeData[base + 3]), vec2(0.001));
    vec4 radius = vec4(shapeData[base + 4], shapeData[base + 5], shapeData[base + 6], shapeData[base + 7]);
    float thickness = shapeData[base + 8];
    vec4 color1 = vec4(shapeData[base + 9], shapeData[base + 10], shapeData[base + 11], shapeData[base + 12]);
    vec4 color2 = vec4(shapeData[base + 13], shapeData[base + 14], shapeData[base + 15], shapeData[base + 16]);
    vec4 color3 = vec4(shapeData[base + 17], shapeData[base + 18], shapeData[base + 19], shapeData[base + 20]);
    vec4 color4 = vec4(shapeData[base + 21], shapeData[base + 22], shapeData[base + 23], shapeData[base + 24]);
    vec4 outlineColor = vec4(shapeData[base + 25], shapeData[base + 26], shapeData[base + 27], shapeData[base + 28]);
    float renderMode = shapeData[base + 29];

    vec2 local = gl_FragCoord.xy - location;
    vec2 coord = local / size;

    if (renderMode > 2.5 && renderMode < 3.5) {
        float radialRadius = shapeData[base + 4];
        float radialThickness = shapeData[base + 8];
        float radialProgress = shapeData[base + 13];
        float radialSmoothness = shapeData[base + 14];
        vec2 radialCoord = vec2(coord.x, 1.0 - coord.y);
        vec2 centered = (radialCoord - 0.5) * size;
        vec2 radialHalfSize = size * 0.5 - vec2(0.6);
        float clampedRadialRadius = min(radialRadius, min(radialHalfSize.x, radialHalfSize.y));
        float progressRadius = max(clampedRadialRadius - radialThickness * 0.5, 0.0);
        float alpha = radialOutlineAlpha(centered, radialHalfSize, clampedRadialRadius,
                radialThickness, radialSmoothness)
                * radialProgressAlpha(centered,
                max(radialHalfSize - vec2(radialThickness * 0.5), vec2(0.0001)),
                progressRadius, radialProgress);
        if (alpha < 0.001 || color1.a < 0.001) discard;
        fragColor = vec4(color1.rgb, color1.a * alpha);
        return;
    }

    vec4 color;
    if (renderMode > 0.5 && renderMode < 1.5) {
        float horizontal = clamp(coord.x, 0.0, 1.0);
        color = horizontal < 0.5
                ? mix(color1, color2, horizontal * 2.0)
                : mix(color2, color3, (horizontal - 0.5) * 2.0);
    } else {
        color = createGradient(coord, color1, color2, color3, color4);
    }

    vec2 halfSize = size * 0.5;
    vec4 clampedRadius = min(radius, vec4(min(halfSize.x, halfSize.y)));
    float distance = roundedBoxSDF(local - halfSize, halfSize, clampedRadius);
    float edge = clamp(fwidth(distance), 0.65, 1.25);
    float shapeAlpha = 1.0 - smoothstep(-edge * 0.5, edge * 0.5, distance);

    vec4 fillColor = vec4(color.rgb, color.a * shapeAlpha);
    vec4 finalColor = fillColor;

    if (thickness > 0.0 && outlineColor.a > 0.001) {
        float outerAlpha = 1.0 - smoothstep(-edge, edge, distance);
        float innerAlpha = 1.0 - smoothstep(-thickness - edge, -thickness + edge, distance);
        float borderAlpha = clamp(outerAlpha - innerAlpha, 0.0, 1.0);
        vec4 borderColor = vec4(outlineColor.rgb, outlineColor.a * borderAlpha);
        float resultAlpha = borderColor.a + fillColor.a * (1.0 - borderColor.a);
        if (resultAlpha > 0.001) {
            vec3 resultRgb = (borderColor.rgb * borderColor.a
                    + fillColor.rgb * fillColor.a * (1.0 - borderColor.a)) / resultAlpha;
            finalColor = vec4(resultRgb, resultAlpha);
        } else {
            finalColor = vec4(0.0);
        }
    }

    if (finalColor.a < 0.001) {
        discard;
    }

    fragColor = finalColor;
}
