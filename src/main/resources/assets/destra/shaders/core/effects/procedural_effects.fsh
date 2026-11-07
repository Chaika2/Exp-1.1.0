#version 150

in vec2 texCoord;
in vec4 vertexColor;
in vec3 effectDirection;
out vec4 fragColor;

uniform sampler2D ColorTexture;
uniform sampler2D MaskTexture;
uniform sampler2D SceneDepthTexture;
uniform sampler2D BodyDepthTexture;
uniform sampler2D ArmorMaskTexture;
uniform sampler2D DepthTexture;
uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float effectAlpha;
uniform float opacity;
uniform int effectMode;
uniform int styleIndex;
uniform int wholeBoxProjection;
uniform vec4 textureBounds;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

float hash12(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm2(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    mat2 rotation = mat2(0.80, 0.60, -0.60, 0.80);
    for (int i = 0; i < 6; i++) {
        value += amplitude * noise2(p);
        p = rotation * p * 2.03 + vec2(17.13, 9.71);
        amplitude *= 0.5;
    }
    return value;
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n = dot(i, vec3(1.0, 57.0, 113.0));
    return mix(mix(mix(hash12(vec2(n, n + 1.0)), hash12(vec2(n + 1.0, n + 2.0)), f.x),
                   mix(hash12(vec2(n + 57.0, n)), hash12(vec2(n + 58.0, n + 1.0)), f.x), f.y),
               mix(mix(hash12(vec2(n + 113.0, n)), hash12(vec2(n + 114.0, n + 1.0)), f.x),
                   mix(hash12(vec2(n + 170.0, n)), hash12(vec2(n + 171.0, n + 1.0)), f.x), f.y), f.z);
}

float effectAspect() {
    return effectMode == 2 ? 1.0 : screenSize.x / max(screenSize.y, 1.0);
}

vec3 abyssEffect(vec2 uv) {
    vec2 p = (uv - 0.5) * vec2(max(effectAspect(), 1.0), 1.0) * 4.0;
    float len = dot(p, p) * 0.3 - 0.4;
    vec3 z = sin((time * 2.0 + 15.0) * vec3(0.23, 0.19, 0.17));
    for (int i = 0; i < 20; i++) {
        z += cos(z.zxy + p.yxy * float(i) * len);
    }
    float value = z.r * 0.06 + 0.3;
    value -= smoothstep(0.1, -0.3, len) * 1.5 + len * 0.3 - 0.4;
    return vec3(max(value, 0.1));
}

vec3 cosmosEffect(vec2 uv) {
    vec2 p = uv * 2.0 - 1.0;
    p.x *= effectAspect();
    float radius = length(p);
    float angle = atan(p.y, p.x);
    vec2 swirl = vec2(angle * 1.25 + time * 0.055, radius * 4.6 - time * 0.035);
    float cloudA = fbm2(swirl * 1.45 + vec2(0.0, fbm2(swirl * 2.2)));
    float cloudB = fbm2(swirl.yx * 2.7 - vec2(time * 0.025, 0.0));
    float arm = pow(max(0.0, 1.0 - abs(fract(angle / TAU * 4.0 + radius * 1.7 - time * 0.018) - 0.5) * 2.0), 3.0);
    vec2 starCell = floor((uv + vec2(time * 0.0008, 0.0)) * screenSize / 5.0);
    float starSeed = hash12(starCell);
    float star = step(0.985, starSeed) * pow(starSeed, 18.0);
    float core = exp(-radius * 4.2);
    return vec3(cloudA * 0.58 + cloudB * 0.28 + arm * cloudA * 0.75 + core * 1.8 + star * 2.4);
}

mat2 rotate2(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat2(c, -s, s, c);
}

float crazinessMap(vec3 p) {
    p.xz = rotate2(time * 0.4) * p.xz;
    p.xy = rotate2(time * 0.3) * p.xy;
    vec3 q = p * 2.0 + time;
    return length(p + vec3(sin(time * 0.7))) * log(length(p) + 1.0)
           + sin(q.x + sin(q.z + sin(q.y))) * 5.5 - 1.0;
}

vec3 crazinessEffect(vec2 uv) {
    vec2 p = uv * vec2(effectAspect(), 1.0) - vec2(0.9, 0.5);
    vec3 color = vec3(0.0);
    float distanceAlongRay = 0.9;
    for (int i = 0; i <= 5; i++) {
        vec3 point = vec3(0.0, 0.0, 5.0) + normalize(vec3(p, -1.0)) * distanceAlongRay;
        float field = crazinessMap(point);
        float shade = clamp((field - crazinessMap(point + 0.1)) * 0.5, -0.1, 1.0);
        vec3 light = vec3(0.1, 0.3, 0.4) + vec3(5.0, 2.5, 3.0) * shade;
        color = color * light + (1.0 - smoothstep(0.0, 2.5, field)) * 0.7 * light;
        distanceAlongRay += min(field, 1.0);
    }
    return max(color, vec3(0.0));
}

vec3 flamestormEffect(vec2 uv) {
    vec2 p = uv * 2.0 - 1.0;
    p.x *= effectAspect();
    float radial = max(length(p), 0.08);
    float twist = atan(p.y, p.x) * 2.0 - time * 0.32;
    vec2 warped = vec2(twist, 3.2 / radial - time * 0.18);
    float density = fbm2(warped * vec2(1.1, 0.55));
    density += 0.52 * fbm2(warped * vec2(2.4, 1.15) + 7.0);
    float tongues = pow(max(0.0, density - 0.38), 1.35) * (1.3 - smoothstep(0.15, 1.25, radial));
    float core = exp(-radial * 5.0);
    return vec3(tongues * 1.75 + core * 1.45);
}

vec3 inversionShape(vec2 uv) {
    vec2 z = (-1.0 + 2.0 * uv) * 1.5;
    vec3 color = vec3(1.0);
    float localTime = time * 0.05 + 47.0;
    for (int j = 0; j < 36; j++) {
        float s = float(j) / 16.0;
        float f = 0.2 * (0.5 + fract(sin(s * 20.0)));
        vec2 c = 0.5 * vec2(cos(f * localTime + 17.0 * s), sin(f * localTime + 19.0 * s));
        z -= c;
        float zr = max(length(z), 0.055);
        float ar = atan(z.y, z.x) + zr * 0.6;
        z = vec2(cos(ar), sin(ar)) / zr;
        z += c;
        color -= 0.5 * exp(-10.0 * dot(z, z))
                 * (0.25 + 0.4 * sin(5.5 + 1.5 * s + vec3(1.6, 0.8, 0.5)));
    }
    return pow(clamp(color, 0.0, 1.0), vec3(0.9));
}

vec3 shinyShape(vec2 p) {
    p *= 2.0;
    vec3 accum = vec3(0.0);
    vec2 z = p;
    for (int i = 0; i < 8; i++) {
        z += cos(z.yx + cos(z.yx + cos(z.yx + 0.5 * time)));
        float d = dot(z - p, z - p);
        accum.x += 1.0 / (1.0 + d);
        accum.y += d;
        accum.z += sin(atan(z.y - p.y, z.x - p.x));
    }
    return accum / 8.0;
}

vec3 shinyEffect(vec2 uv) {
    float aspect = effectAspect();
    vec2 p = (2.0 * uv - 1.0) * vec2(max(aspect, 1.0), max(1.0 / max(aspect, 0.0001), 1.0));
    vec3 center = shinyShape(p);
    vec3 sideA = shinyShape(p + vec2(0.04, 0.0));
    vec3 sideB = shinyShape(p + vec2(0.0, 0.04));
    vec3 color = mix(vec3(0.08), vec3(1.15), center.x);
    color *= 0.15 * center.y;
    color += 0.4 * abs(center.z) - 0.1;
    vec3 normal = normalize(vec3(sideA.x - center.x, 0.01, sideB.x - center.x));
    float diffuse = clamp(0.5 + 0.5 * dot(normal, vec3(0.5773)), 0.0, 1.0);
    color *= 1.0 + 0.7 * diffuse * max(color, vec3(0.0));
    color += 0.3 * pow(max(normal.y, 0.0), 128.0);
    return max(color, vec3(0.0));
}

vec2 vortexForce(vec2 q, vec2 center) {
    vec2 d = q - center;
    return 0.25 * vec2(d.y, -d.x) / (dot(d, d) + 0.05);
}

vec2 vortexFlow(vec2 q) {
    vec2 velocity = vec2(0.0);
    float direction = 1.0;
    vec2 center = vec2(mod(time, 10.0) - 20.0, 0.6);
    for (int i = 0; i < 14; i++) {
        velocity += direction * vortexForce(4.0 * q, center);
        center = vec2(center.x + 1.0, -center.y);
        direction = -direction;
    }
    return velocity;
}

vec3 vortexEffect(vec2 uv) {
    vec2 p = uv - 0.5;
    p.x *= effectAspect();
    for (int i = 0; i < 7; i++) {
        p -= vortexFlow(p) * 0.035;
    }
    float field = fbm2(5.0 * p + vec2(-0.1 * time, 0.0));
    return vec3(field * 1.25);
}

vec3 evaluateEffect(vec2 uv) {
    if (styleIndex == 0) return abyssEffect(uv);
    if (styleIndex == 1) return cosmosEffect(uv);
    if (styleIndex == 2) return crazinessEffect(uv);
    if (styleIndex == 3) return flamestormEffect(uv);
    if (styleIndex == 4) return inversionShape(uv);
    if (styleIndex == 5) return shinyEffect(uv);
    return vortexEffect(uv);
}

vec2 surfaceCoordinates() {
    if (wholeBoxProjection == 0) return texCoord;
    return clamp((gl_FragCoord.xy - textureBounds.xy) / max(textureBounds.zw, vec2(1.0)), 0.0, 1.0);
}

vec2 skyCoordinates() {
    vec3 direction = normalize(effectDirection);
    float compression = mix(0.46, 0.40, smoothstep(0.0, 1.0, max(direction.y, 0.0)));
    return vec2(0.12, 0.08) + direction.xz * compression;
}

float handMaskValue(vec2 coord) {
    vec2 texel = 1.0 / vec2(textureSize(DepthTexture, 0));
    float depth = texture(DepthTexture, coord).r;
    depth = min(depth, texture(DepthTexture, coord + vec2(-texel.x, 0.0)).r);
    depth = min(depth, texture(DepthTexture, coord + vec2(texel.x, 0.0)).r);
    depth = min(depth, texture(DepthTexture, coord + vec2(0.0, -texel.y)).r);
    depth = min(depth, texture(DepthTexture, coord + vec2(0.0, texel.y)).r);
    return smoothstep(0.995, 0.975, depth);
}

float entityMaskValue(vec2 coord) {
    float mask = texture(MaskTexture, coord).a;
    vec2 texel = 1.0 / vec2(textureSize(SceneDepthTexture, 0));
    float sceneDepth = texture(SceneDepthTexture, coord).r;
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(-texel.x, 0.0)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(texel.x, 0.0)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(0.0, -texel.y)).r);
    sceneDepth = min(sceneDepth, texture(SceneDepthTexture, coord + vec2(0.0, texel.y)).r);
    float bodyDepth = texture(BodyDepthTexture, coord).r;
    float visibleBody = 1.0 - smoothstep(0.000003, 0.000035, max(bodyDepth - sceneDepth, 0.0));
    float armorMask = smoothstep(0.01, 0.08, texture(ArmorMaskTexture, coord).a);
    return smoothstep(0.01, 0.08, mask) * visibleBody * (1.0 - armorMask);
}

vec3 tintToTheme(vec3 source) {
    vec3 theme = max(baseColor.rgb * vertexColor.rgb, vec3(0.015));
    float brightness = clamp(dot(max(source, vec3(0.0)), vec3(0.299, 0.587, 0.114)), 0.0, 2.5);
    float highlight = smoothstep(0.72, 1.75, brightness);
    return clamp(theme * (0.18 + brightness * 1.55) + vec3(highlight * 0.34), 0.0, 2.2);
}

void main() {
    vec2 uv = effectMode == 2 ? skyCoordinates() : (effectMode == 1 ? texCoord : surfaceCoordinates());
    float mask = 1.0;
    vec4 original = vec4(0.0);

    if (effectMode == 1 || effectMode == 3) {
        mask = effectMode == 1 ? entityMaskValue(uv) : handMaskValue(uv);
        if (mask < 0.01) discard;
        original = texture(ColorTexture, uv);
    }

    vec3 effect = tintToTheme(evaluateEffect(uv));

    if (effectMode == 1 || effectMode == 3) {
        fragColor = vec4(mix(original.rgb, effect, clamp(effectAlpha, 0.0, 1.0)), mask);
        return;
    }

    if (effectMode == 2) {
        float horizon = smoothstep(-0.34, -0.08, normalize(effectDirection).y);
        fragColor = vec4(effect, clamp(opacity, 0.0, 1.0) * horizon);
        return;
    }

    float detailAlpha = 0.52 + 0.48 * clamp(dot(effect, vec3(0.3333)), 0.0, 1.0);
    fragColor = vec4(effect, clamp(effectAlpha, 0.0, 1.0) * vertexColor.a * detailAlpha);
}
