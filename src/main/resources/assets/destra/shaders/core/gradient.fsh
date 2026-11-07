#version 150

in vec2 FragCoord;

uniform vec2 size;
uniform vec4 leftColor;
uniform vec4 centerColor;
uniform vec4 rightColor;
uniform vec4 cornerRadius;

out vec4 fragColor;

float roundedBox(vec2 p, vec2 halfSize, vec4 radius) {
    vec2 q = abs(p) - halfSize + radius.xy;
    if (p.x < 0.0) q.x = abs(p.x) - halfSize.x + radius.zw.x;
    if (p.y < 0.0) q.y = abs(p.y) - halfSize.y + radius.zw.y;

    radius.xy = (p.x > 0.0) ? radius.xy : radius.zw;
    radius.x  = (p.y > 0.0) ? radius.x  : radius.y;

    vec2 q2 = abs(p) - halfSize + radius.x;

    return length(max(q2, 0.0)) + min(max(q2.x, q2.y), 0.0) - radius.x;
}

void main() {
    vec2 uv = FragCoord;
    
    vec2 p = (uv - 0.5) * size;
    vec2 halfSize = size * 0.5;
    
    float d = roundedBox(p, halfSize, cornerRadius);
    
    float alpha = 1.0 - smoothstep(-1.0, 1.0, d);
    
    float t = uv.x;
    vec4 color;
    
    if (t < 0.5) {
        float f = t / 0.5;
        color = mix(leftColor, centerColor, f);
    } else {
        float f = (t - 0.5) / 0.5;
        color = mix(centerColor, rightColor, f);
    }
    
    fragColor = vec4(color.rgb, color.a * alpha);
}