#version 150

#moj_import <destra:common.glsl>

in vec2 FragCoord;

uniform sampler2D Sampler0;

uniform vec2 size;
uniform float radius;
uniform float hurt_time;
uniform float alpha;

uniform float startX, endX;
uniform float startY, endY;

uniform float texXSize;
uniform float texYSize;

out vec4 fragColor;

float signedDistanceField(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, 0.0)) - r;
}

void main() {
    vec2 tex = FragCoord;

    vec2 clippedTexCoord = vec2(
        mix(startX / texXSize, endX / texXSize, tex.x),
        mix(startY / texYSize, endY / texYSize, tex.y));

    vec4 smpl = texture(Sampler0, clippedTexCoord);

    vec2 st = tex * size;
    vec2 centre = 0.5 * size;

    float dist = signedDistanceField(centre - st, centre - radius - 1.0, radius);
    float sa = smoothstep(0.0, 1.0, dist);

    vec4 c = mix(vec4(smpl.rgb, smpl.a), vec4(smpl.rgb, 0.0), sa);

    vec3 finalColor = mix(smpl.rgb, vec3(1.0, 0.0, 0.0), hurt_time);
    fragColor = vec4(finalColor, c.a * alpha);
}