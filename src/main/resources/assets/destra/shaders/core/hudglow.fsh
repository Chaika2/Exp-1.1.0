#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

uniform vec2 size;
uniform vec2 location;
uniform vec4 radius;
uniform float softness;
uniform float cornerPower;

out vec4 fragColor;

float lnorm(vec2 v, float p) {
    v = abs(v);
    return pow(pow(v.x, p) + pow(v.y, p), 1.0 / p);
}

float roundedBoxSDF(vec2 center, vec2 halfSize, vec4 radii) {
    radii.xy = (center.x > 0.0) ? radii.xy : radii.zw;
    radii.x = (center.y > 0.0) ? radii.x : radii.y;

    vec2 q = abs(center) - halfSize + radii.x;
    float n = max(cornerPower, 1.0);
    return min(max(q.x, q.y), 0.0) + lnorm(max(q, vec2(0.0)), n) - radii.x;
}

vec4 createGradient(vec2 coords, vec4 c1, vec4 c2, vec4 c3, vec4 c4) {
    vec2 t = clamp(coords, 0.0, 1.0);
    t = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    return mix(mix(c1, c2, t.y), mix(c3, c4, t.y), t.x);
}

void main() {
    vec2 safeSize = max(size, vec2(0.0001));
    vec2 frag = gl_FragCoord.xy - location;
    vec4 clampedRadius = min(radius, vec4(min(safeSize.x, safeSize.y) * 0.5));
    float distance = roundedBoxSDF(frag - (safeSize * 0.5), safeSize * 0.5, clampedRadius);

    float edge = clamp(fwidth(distance), 0.65, 1.25);
    float alphaSoftness = softness <= 1.0 ? edge : max(softness, edge);
    float outsideDistance = max(distance, 0.0);
    float normalized = outsideDistance / max(alphaSoftness, 0.0001);
    float smoothedAlpha = exp(-pow(normalized * 2.1, 1.35));
    smoothedAlpha *= 1.0 - smoothstep(alphaSoftness * 0.7, alphaSoftness * 1.65, outsideDistance);

    vec4 gradient = createGradient(frag / safeSize, color1, color2, color3, color4);
    fragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);
}
