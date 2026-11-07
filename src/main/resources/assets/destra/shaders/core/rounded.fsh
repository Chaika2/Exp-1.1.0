#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;

uniform vec2 size;
uniform vec4 round;
uniform vec2 smoothness;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

out vec4 fragColor;

float alpha(vec2 d, vec2 d1, vec4 radius) {
    vec2 selectedRadius;
    if (d.x >= 0.0) {
        selectedRadius = (d.y >= 0.0) ? radius.yy : radius.xx;
    } else {
        selectedRadius = (d.y >= 0.0) ? radius.ww : radius.zz;
    }
    
    vec2 v = abs(d) - d1 + selectedRadius;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - selectedRadius.x;
}

vec4 createGradient(vec2 coords, vec4 c1, vec4 c2, vec4 c3, vec4 c4) {
    return mix(mix(c1, c2, coords.y), mix(c3, c4, coords.y), coords.x);
}

void main() {
    vec2 texCoord = FragCoord;

    vec4 color = createGradient(texCoord, color1, color2, color3, color4);
    
    vec2 st = texCoord * size;
    vec2 halfSize = 0.5 * size;
    
    float sa = 1.0 - smoothstep(smoothness.x, smoothness.y, alpha(halfSize - st, halfSize - 1.0, round));
    
    fragColor = mix(vec4(color.rgb, 0.0), vec4(color.rgb, color.a), sa);
}