#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D ColorTexture;
uniform sampler2D DepthTexture;
uniform sampler2D TrailTexture;
uniform float time;
uniform vec4 baseColor;
uniform float effectAlpha;
uniform vec2 resolution;
uniform float frameScale;
uniform float flameIntensity;
uniform float flameRadius;
uniform float flameHeight;
uniform float trailLength;
uniform int composeMode;
uniform int useItemColor;
uniform int burnThrough;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x),
               mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.52;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p = mat2(1.62, 1.18, -1.18, 1.62) * p + 7.13;
        a *= 0.48;
    }
    return v;
}

float handMask(vec2 p) {
    vec2 px = 1.0 / max(resolution, vec2(1.0));
    float d = texture(DepthTexture, p).r;
    d = min(d, texture(DepthTexture, p + vec2(px.x, 0)).r);
    d = min(d, texture(DepthTexture, p - vec2(px.x, 0)).r);
    d = min(d, texture(DepthTexture, p + vec2(0, px.y)).r);
    d = min(d, texture(DepthTexture, p - vec2(0, px.y)).r);
    return smoothstep(0.995, 0.978, d);
}

vec3 itemTint(vec2 p) {
    if (useItemColor == 0) return baseColor.rgb;
    vec2 px = 1.0 / max(resolution, vec2(1.0));
    vec3 sum = vec3(0.0);
    float weight = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 q = clamp(p + vec2(x, y) * px * 7.0, vec2(0.001), vec2(0.999));
            float w = handMask(q);
            sum += texture(ColorTexture, q).rgb * w;
            weight += w;
        }
    }
    return weight > 0.01 ? clamp(sum / weight * 1.22, 0.0, 1.8) : baseColor.rgb;
}

void main() {
    if (composeMode == 1) {
        vec4 flame = texture(TrailTexture, uv);
        if (flame.a <= 0.002) discard;
        vec3 bloom = flame.rgb * (0.86 + flame.a * 0.9) * flameIntensity;
        outColor = vec4(bloom, clamp(flame.a * effectAlpha, 0.0, 1.0));
        return;
    }

    float dt = max(frameScale, 0.05);
    float heightUv = flameHeight / max(resolution.y, 1.0);
    float sideWave = sin(uv.y * 44.0 + time * 2.7) + sin(uv.y * 91.0 - time * 1.6);
    vec2 advect = vec2(sideWave * 0.00065, -heightUv * 0.058) * dt * max(trailLength, 0.05);
    vec2 advectedUv = clamp(uv + advect, vec2(0.001), vec2(0.999));
    vec4 previous = texture(TrailTexture, advectedUv);
    float turbulence = fbm(vec2(uv.x * 12.0 + time * 0.47, uv.y * 9.0 - time * 0.72));
    float decay = mix(0.87, 0.972, clamp(trailLength * 0.5, 0.0, 1.0));
    previous.a *= pow(decay - turbulence * 0.018, dt);
    previous.rgb *= pow(0.982, dt);

    float mask = handMask(uv);
    vec2 px = 1.0 / max(resolution, vec2(1.0));
    float expanded = mask;
    for (int i = 0; i < 10; i++) {
        float a = float(i) * 0.6283185;
        vec2 o = vec2(cos(a), sin(a)) * px * flameRadius;
        expanded = max(expanded, handMask(clamp(uv + o, vec2(0.001), vec2(0.999))) * 0.66);
    }
    float rim = clamp(expanded - mask * 0.22, 0.0, 1.0);
    float lick = smoothstep(0.18, 0.9, turbulence + 0.23 * sin(uv.x * 53.0 - time * 3.2));
    float injection = clamp(max(mask * 0.26, rim * lick), 0.0, 1.0);
    injection = 1.0 - pow(1.0 - injection * 0.52, dt);

    vec3 tint = itemTint(uv);
    float heat = clamp(rim + mask * 0.9 + turbulence * 0.32, 0.0, 1.0);
    vec3 fire = mix(tint * vec3(0.58, 0.12, 0.025), tint * 1.55 + vec3(0.5, 0.19, 0.02), heat);
    fire = mix(fire, vec3(1.0, 0.9, 0.55), pow(heat, 4.0) * 0.46);

    float alpha = clamp(previous.a + injection * (1.0 - previous.a), 0.0, 0.96);
    vec3 color = previous.rgb;
    if (injection > 0.001) color = mix(color, fire, injection / max(alpha, 0.001));
    if (burnThrough == 0) alpha *= mix(0.76, 1.0, rim);
    if (alpha <= 0.002) discard;
    outColor = vec4(color, alpha);
}
