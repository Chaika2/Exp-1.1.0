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
uniform int composeMode;
uniform int useItemColor;

mat2 rot(float a) {
    return mat2(cos(a), sin(a), -sin(a), cos(a));
}

float hash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.11369, 0.13787));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float noise(vec3 x) {
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash(p + vec3(0.0, 0.0, 0.0));
    float n100 = hash(p + vec3(1.0, 0.0, 0.0));
    float n010 = hash(p + vec3(0.0, 1.0, 0.0));
    float n110 = hash(p + vec3(1.0, 1.0, 0.0));
    float n001 = hash(p + vec3(0.0, 0.0, 1.0));
    float n101 = hash(p + vec3(1.0, 0.0, 1.0));
    float n011 = hash(p + vec3(0.0, 1.0, 1.0));
    float n111 = hash(p + vec3(1.0, 1.0, 1.0));

    float x00 = mix(n000, n100, f.x);
    float x10 = mix(n010, n110, f.x);
    float x01 = mix(n001, n101, f.x);
    float x11 = mix(n011, n111, f.x);
    float y0 = mix(x00, x10, f.y);
    float y1 = mix(x01, x11, f.y);
    return mix(y0, y1, f.z) * 2.0 - 1.0;
}

float fbm(vec3 p) {
    float value = 0.0;
    float amp = 0.5;
    vec3 q = p;

    for (int i = 0; i < 4; i++) {
        q.xy = vec2(q.x * 0.92 - q.y * 0.39, q.x * 0.39 + q.y * 0.92);
        q += time * vec3(0.17, -0.50, 0.11);
        value += amp * noise(q);
        q *= 2.1;
        amp *= 0.42;
    }

    return value;
}

float handMaskRaw(vec2 coord) {
    vec2 texelSize = 1.0 / textureSize(DepthTexture, 0);
    float centerDepth = texture(DepthTexture, coord).r;
    float minDepth = centerDepth;
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(-texelSize.x, 0.0)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(texelSize.x, 0.0)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(0.0, -texelSize.y)).r);
    minDepth = min(minDepth, texture(DepthTexture, coord + vec2(0.0, texelSize.y)).r);
    return smoothstep(0.99, 0.98, minDepth);
}

float expandedHandMask(vec2 coord) {
    vec2 px = 1.0 / max(resolution, vec2(1.0));
    float m = handMaskRaw(coord) * 1.35;

    const int count = 8;
    for (int i = 0; i < count; i++) {
        float a = (float(i) / float(count)) * 6.2831853;
        vec2 dir = vec2(cos(a), sin(a));
        m = max(m, handMaskRaw(coord + dir * px * 4.0) * 0.92);
        m = max(m, handMaskRaw(coord + dir * px * 11.0) * 0.52);
    }

    return clamp(m, 0.0, 1.0);
}

vec3 sampleItemTint(vec2 coord, vec3 fallbackColor) {
    if (useItemColor != 1) {
        return fallbackColor;
    }

    vec2 px = 1.0 / max(resolution, vec2(1.0));
    vec3 acc = vec3(0.0);
    float weight = 0.0;

    vec2 offsets[5] = vec2[5](
        vec2(0.0, 0.0),
        vec2(5.0, 0.0), vec2(-5.0, 0.0),
        vec2(0.0, 5.0), vec2(0.0, -5.0)
    );

    for (int i = 0; i < 5; i++) {
        vec2 p = clamp(coord + offsets[i] * px, vec2(0.001), vec2(0.999));
        float m = handMaskRaw(p);
        vec3 c = texture(ColorTexture, p).rgb;
        float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
        float w = m * smoothstep(0.03, 0.35, luma);
        acc += c * w;
        weight += w;
    }

    if (weight <= 0.001) {
        return fallbackColor;
    }

    vec3 tint = acc / weight;
    tint = mix(tint, fallbackColor, 0.22);
    return clamp(tint * 1.15, 0.0, 1.6);
}

vec4 buildCurrentSmoke(vec2 coord) {
    float mask = handMaskRaw(coord);
    float around = expandedHandMask(coord);
    if (around <= 0.001) {
        return vec4(0.0);
    }

    vec2 centered = (coord * 2.0 - 1.0) * vec2(resolution.x / max(resolution.y, 1.0), 1.0);
    float swirl = noise(vec3(centered * 1.8, time * 0.18));
    vec2 smokeUv = centered;
    smokeUv += vec2(sin(time * 0.7 + centered.y * 5.0), cos(time * 0.55 + centered.x * 4.0)) * 0.035;
    smokeUv *= rot(swirl * 0.45);

    float density = fbm(vec3(smokeUv * 4.2, time * 0.22));
    float detail = noise(vec3(smokeUv * 7.4 + 4.7, time * 0.37));
    float wisps = smoothstep(-0.28, 0.64, density + detail * 0.34);

    float edge = clamp(around - mask * 0.32, 0.0, 1.0);
    float pulse = 0.82 + 0.18 * sin(time * 2.1 + density * 3.0);
    float alpha = edge * wisps * pulse;
    alpha *= smoothstep(0.0, 0.18, around);
    alpha = clamp(alpha * 0.34, 0.0, 0.55);

    vec3 smokeTint = sampleItemTint(coord, baseColor.rgb);
    vec3 coolSmoke = mix(smokeTint * 0.55, vec3(0.78, 0.84, 0.92), 0.34);
    vec3 hotSmoke = mix(smokeTint * 1.35, vec3(1.0), 0.18);
    vec3 color = mix(coolSmoke, hotSmoke, clamp(wisps * around, 0.0, 1.0));

    return vec4(color, alpha);
}

void main() {
    if (composeMode == 1) {
        vec4 trail = texture(TrailTexture, uv);
        float alpha = trail.a * effectAlpha;
        if (alpha <= 0.003) {
            discard;
        }
        outColor = vec4(trail.rgb, alpha);
        return;
    }

    vec2 drift = vec2(
        sin(time * 0.8 + uv.y * 9.0) * 0.0018,
        -0.0042 - sin(time * 0.31 + uv.x * 10.0) * 0.0008
    ) * frameScale;
    vec4 previous = texture(TrailTexture, clamp(uv + drift, vec2(0.001), vec2(0.999)));
    previous.a *= pow(0.925, frameScale);
    previous.rgb *= pow(0.985, frameScale);

    vec4 current = buildCurrentSmoke(uv);
    current.a = 1.0 - pow(max(0.0, 1.0 - current.a), frameScale);
    float alpha = clamp(previous.a + current.a * (1.0 - previous.a), 0.0, 0.82);
    vec3 color = previous.rgb;
    if (current.a > 0.001) {
        float mixValue = current.a / max(alpha, 0.001);
        color = mix(previous.rgb, current.rgb, clamp(mixValue, 0.0, 1.0));
    }

    if (alpha <= 0.002) {
        discard;
    }

    outColor = vec4(color, alpha);
}
