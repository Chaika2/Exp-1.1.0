#version 150

uniform vec4 u_rect;
uniform vec2 u_mouse;
uniform vec3 u_accent;
uniform float u_radius;
uniform float u_time;
uniform float u_hover;
uniform float u_alpha;

in vec2 vertexPos;

out vec4 fragColor;

float roundedBox(vec2 point, vec2 halfSize, float radius) {
    float safeRadius = min(radius, min(halfSize.x, halfSize.y));
    vec2 distanceToEdge = abs(point) - halfSize + safeRadius;
    return min(max(distanceToEdge.x, distanceToEdge.y), 0.0)
        + length(max(distanceToEdge, vec2(0.0))) - safeRadius;
}

float noise(vec2 point) {
    return fract(sin(dot(point, vec2(127.1, 311.7))) * 43758.5453123);
}

void main() {
    vec2 size = max(u_rect.zw, vec2(1.0));
    vec2 local = vertexPos - u_rect.xy;
    vec2 uv = local / size;
    vec2 halfSize = size * 0.5;
    vec2 point = local - halfSize;

    float baseDistance = roundedBox(point, halfSize - vec2(0.8), u_radius);
    vec2 mouseDelta = (uv - clamp(u_mouse, vec2(0.0), vec2(1.0)))
        * vec2(size.x / max(size.y, 1.0), 1.0);
    float attraction = exp(-dot(mouseDelta, mouseDelta) * 3.4) * u_hover;
    float edgeBand = exp(-abs(baseDistance) * 0.62);
    float angle = atan(point.y, point.x / max(size.x / max(size.y, 1.0), 1.0));
    float broadWave = 0.5 + 0.5 * sin(angle * 18.0 + u_time * 3.8 + sin(angle * 5.0 - u_time * 1.1));
    float fineWave = 0.5 + 0.5 * sin(angle * 31.0 - u_time * 5.2);
    float deformation = edgeBand * attraction * (0.45 + broadWave * 0.72 + fineWave * 0.34) * 2.35;
    float distance = baseDistance - deformation;

    float antialias = max(fwidth(distance), 0.45);
    float shape = 1.0 - smoothstep(-antialias, antialias, distance);
    float rim = (1.0 - smoothstep(0.0, 1.55, abs(distance))) * shape;
    float bevel = (1.0 - smoothstep(0.0, 5.5, abs(baseDistance))) * shape;
    float cursorGlow = exp(-dot(mouseDelta, mouseDelta) * 4.8) * shape * u_hover;

    float sweepPosition = fract(u_time * 0.11 + 0.18);
    float sweepDistance = uv.x - sweepPosition;
    float sweep = exp(-sweepDistance * sweepDistance * 88.0) * shape * u_hover;
    float topRail = exp(-pow(local.y - 1.5, 2.0) / 8.0)
        * smoothstep(0.04, 0.18, uv.x) * (1.0 - smoothstep(0.82, 0.96, uv.x));
    float veins = pow(0.5 + 0.5 * sin(local.x * 0.18 + sin(local.y * 0.28 + u_time) * 1.6 - u_time * 0.7), 5.0)
        * shape * u_hover;
    float grain = (noise(floor(local * 0.55) + floor(u_time * 5.0)) - 0.5) * 0.025;

    vec3 softAccent = mix(u_accent, vec3(1.0), 0.18);
    vec3 color = u_accent * (0.24 + cursorGlow * 0.52 + veins * 0.10);
    color += softAccent * rim * 0.62;
    color += softAccent * bevel * 0.10;
    color += softAccent * sweep * 0.14;
    color += vec3(1.0) * topRail * 0.08;
    color += vec3(grain);

    float alpha = shape * (0.035 + cursorGlow * 0.16 + veins * 0.035);
    alpha += rim * 0.28 + bevel * 0.045 + sweep * 0.065 + topRail * 0.045;
    alpha *= u_hover * u_alpha;

    if (alpha <= 0.002) {
        discard;
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), clamp(alpha, 0.0, 0.72));
}
