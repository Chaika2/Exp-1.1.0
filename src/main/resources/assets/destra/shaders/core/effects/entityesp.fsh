#version 150

uniform sampler2D glowSampler;
uniform sampler2D entitySampler;

uniform vec4 primaryColor;
uniform vec4 outlineTint;
uniform vec4 gradientColorA;
uniform vec4 gradientColorB;
uniform vec4 gradientColorC;
uniform vec4 gradientColorD;
uniform int colorMode;
uniform int gradientColorCount;
uniform float gradientPhase;
uniform float glowRadius;
uniform float glowAlphaMult;
uniform int glowThinOutline;
uniform vec2 uTexelSize;

in vec2 texCoord;
out vec4 fragColor;

const float ALPHA_EPSILON = 0.001;
const float TAU = 6.28318530718;

vec3 resolveEffectColor() {
    if (colorMode == 1) {
        vec2 gradientUv = texCoord * 2.0 - 1.0;
        float angle = sin(gradientPhase * TAU) * 0.45;
        float cosAngle = cos(angle);
        float sinAngle = sin(angle);
        gradientUv = mat2(cosAngle, -sinAngle, sinAngle, cosAngle) * gradientUv;
        gradientUv += vec2(
            sin(gradientPhase * TAU) * 0.18,
            cos(gradientPhase * TAU * 0.82) * 0.16
        );
        gradientUv = clamp(gradientUv * 0.5 + 0.5, 0.0, 1.0);

        if (gradientColorCount <= 1) {
            return gradientColorA.rgb;
        }

        if (gradientColorCount == 2) {
            float diagonalMix = clamp((gradientUv.x + gradientUv.y) * 0.5, 0.0, 1.0);
            return mix(gradientColorA.rgb, gradientColorB.rgb, diagonalMix);
        }

        if (gradientColorCount == 3) {
            vec3 topColor = mix(gradientColorA.rgb, gradientColorB.rgb, gradientUv.x);
            return mix(topColor, gradientColorC.rgb, gradientUv.y);
        }

        vec3 topBlend = mix(gradientColorA.rgb, gradientColorB.rgb, gradientUv.x);
        vec3 bottomBlend = mix(gradientColorD.rgb, gradientColorC.rgb, gradientUv.x);
        return mix(topBlend, bottomBlend, gradientUv.y);
    }

    return primaryColor.rgb;
}

void main() {
    vec4 prevColor = texture(entitySampler, texCoord);
    if (prevColor.a > ALPHA_EPSILON) {
        discard;
    }

    if (glowThinOutline == 1) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }

                if (texture(entitySampler, texCoord + vec2(float(x), float(y)) * uTexelSize).a > ALPHA_EPSILON) {
                    float thinAlpha = clamp(outlineTint.a, 0.0, 1.0);
                    if (thinAlpha <= ALPHA_EPSILON) {
                        discard;
                    }

                    fragColor = vec4(outlineTint.rgb, thinAlpha);
                    return;
                }
            }
        }
    }

    float blurAlpha = texture(glowSampler, texCoord).a;
    if (blurAlpha <= ALPHA_EPSILON) {
        discard;
    }

    float radiusGain = mix(1.25, 2.35, clamp((glowRadius - 1.0) / 19.0, 0.0, 1.0));
    float glowAlpha = pow(clamp(blurAlpha * radiusGain, 0.0, 1.0), 0.88);
    float alpha = glowAlpha * glowAlphaMult;

    if (alpha < 0.002) {
        discard;
    }

    fragColor = vec4(resolveEffectColor(), clamp(alpha, 0.0, 1.0));
}
