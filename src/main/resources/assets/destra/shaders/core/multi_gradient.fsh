#version 150

in vec2 FragCoord;

uniform vec2 size;
uniform vec4 colors[8];
uniform float stops[8];
uniform int colorCount;
uniform vec4 cornerRadius;
uniform int gradientDirection;

out vec4 fragColor;

float roundedBoxSDF(vec2 centerPos, vec2 boxSize, vec4 radii) {
    float radius;
    
    if (centerPos.x < 0.0) { 
        radius = (centerPos.y < 0.0) ? radii.x : radii.z;
    } else { 
        radius = (centerPos.y < 0.0) ? radii.y : radii.w;
    }
    
    vec2 q = abs(centerPos) - boxSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

vec4 getGradientColor(float pos) {
    if (colorCount <= 1) return colors[0];
    
    for (int i = 0; i < colorCount - 1; i++) {
        if (pos >= stops[i] && pos <= stops[i + 1]) {
            float range = stops[i + 1] - stops[i];
            float blend = range > 0.0 ? (pos - stops[i]) / range : 0.0;
            return mix(colors[i], colors[i + 1], blend);
        }
    }
    
    return pos < stops[0] ? colors[0] : colors[colorCount - 1];
}

void main() {
    vec2 centerPos = (FragCoord - 0.5) * size;
    vec2 halfSize = 0.5 * size;
    
    float distance = roundedBoxSDF(centerPos, halfSize, cornerRadius);
    float smoothedAlpha = 1.0 - smoothstep(0.0, 1.5, distance);
    
    float gradientPos = (gradientDirection == 0) ? FragCoord.x : FragCoord.y;
    
    vec4 gradientColor = getGradientColor(gradientPos);
    gradientColor.a *= smoothedAlpha;
    
    fragColor = gradientColor;
}