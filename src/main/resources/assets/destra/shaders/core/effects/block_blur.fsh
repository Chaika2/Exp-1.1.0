#version 150

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

uniform sampler2D BlurTexture;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;
uniform float blurStrength;

void main() {
    vec2 screenUv = gl_FragCoord.xy / max(screenSize, vec2(1.0));
    vec3 blurred = texture(BlurTexture, screenUv).rgb;
    vec3 tint = baseColor.rgb * vertexColor.rgb;
    vec3 glass = mix(blurred, tint, (1.0 - clamp(blurStrength, 0.0, 1.0)) * 0.38);
    glass = mix(glass, tint, 0.16);
    float edge = pow(clamp(1.0 - 2.0 * min(min(texCoord.x, 1.0 - texCoord.x),
                                            min(texCoord.y, 1.0 - texCoord.y)), 0.0, 1.0), 2.4);
    glass += tint * edge * 0.16;
    fragColor = vec4(clamp(glass, 0.0, 1.0), alpha * vertexColor.a);
}
