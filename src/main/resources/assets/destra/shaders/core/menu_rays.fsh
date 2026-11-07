#version 150

uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_color1;
uniform vec4 u_color2;
uniform float u_intensity;
uniform float u_rays;
uniform float u_reach;
uniform float u_width;

out vec4 outColor;
in vec3 vertexPos;

#ifndef FNC_MOD289
#define FNC_MOD289
float mod289(const in float x) { return x - floor(x * (1. / 289.)) * 289.; }
vec2 mod289(const in vec2 x) { return x - floor(x * (1. / 289.)) * 289.; }
vec3 mod289(const in vec3 x) { return x - floor(x * (1. / 289.)) * 289.; }
vec4 mod289(const in vec4 x) { return x - floor(x * (1. / 289.)) * 289.; }
#endif

#ifndef FNC_PERMUTE
#define FNC_PERMUTE
float permute(const in float x) { return mod289(((x * 34.0) + 1.0) * x); }
vec2 permute(const in vec2 x) { return mod289(((x * 34.0) + 1.0) * x); }
vec3 permute(const in vec3 x) { return mod289(((x * 34.0) + 1.0) * x); }
vec4 permute(const in vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
#endif

#ifndef FNC_TAYLORINVSQRT
#define FNC_TAYLORINVSQRT
float taylorInvSqrt(in float r) { return 1.79284291400159 - 0.85373472095314 * r; }
vec2 taylorInvSqrt(in vec2 r) { return 1.79284291400159 - 0.85373472095314 * r; }
vec3 taylorInvSqrt(in vec3 r) { return 1.79284291400159 - 0.85373472095314 * r; }
vec4 taylorInvSqrt(in vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
#endif

#ifndef FNC_QUINTIC
#define FNC_QUINTIC
float quintic(const in float v) { return v*v*v*(v*(v*6.0-15.0)+10.0); }
vec2  quintic(const in vec2 v)  { return v*v*v*(v*(v*6.0-15.0)+10.0); }
vec3  quintic(const in vec3 v)  { return v*v*v*(v*(v*6.0-15.0)+10.0); }
vec4  quintic(const in vec4 v)  { return v*v*v*(v*(v*6.0-15.0)+10.0); }
#endif

float rayStrength(vec2 raySource, vec2 rayRefDirection, vec2 coord, float seedA, float seedB, float speed)
{
    vec2 sourceToCoord = coord - raySource;
    float cosAngle = dot(normalize(sourceToCoord), rayRefDirection);

    return clamp(
        (.45 + 0.15 * sin(cosAngle * seedA + u_time * speed)) +
        (0.3 + 0.2 * cos(-cosAngle * seedB + u_time * speed)),
        u_reach, 1.0) *
        clamp((u_resolution.x - length(sourceToCoord)) / u_resolution.x, u_reach, 1.0);
}

void main()
{
    vec2 fragCoord = gl_FragCoord.xy;
    vec2 uv = fragCoord.xy / u_resolution.xy;
    uv.y = 1.0 - uv.y;
    vec2 coord = vec2(fragCoord.x, u_resolution.y - fragCoord.y);
    float speed = u_rays * 10.0;

    float rayWidth = clamp(u_width, 0.0, 0.5);

    vec2 rayPos1 = vec2(u_resolution.x * (0.5 - rayWidth * 0.5), u_resolution.y * -0.4);
    vec2 rayRefDir1 = normalize(vec2(-1.0, -0.116));
    float raySeedA1 = 36.2214 * speed;
    float raySeedB1 = 21.11349 * speed;
    float raySpeed1 = 1.5 * speed;

    vec2 rayPos2 = vec2(u_resolution.x * (0.5 + rayWidth * 0.5), u_resolution.y * -0.6);
    vec2 rayRefDir2 = normalize(vec2(1.0, 0.241));
    float raySeedA2 = 22.39910 * speed;
    float raySeedB2 = 18.0234 * speed;
    float raySpeed2 = 1.1 * speed;

    vec4 themeColor = vec4(u_color1.rgb * u_color1.a, u_color1.a);
    vec4 whiteColor = vec4(u_color2.rgb * u_color2.a, u_color2.a);

    vec4 rays1 = rayStrength(rayPos1, rayRefDir1, coord, raySeedA1, raySeedB1, raySpeed1) * themeColor;
    vec4 rays2 = rayStrength(rayPos2, rayRefDir2, coord, raySeedA2, raySeedB2, raySpeed2) * whiteColor;

    vec4 fragColor = (rays1) + (rays2);

    float brightness = 1.0 * u_reach - (coord.y / u_resolution.y);
    float intensityBoost = 1.85 + u_intensity * 2.65;
    fragColor.rgb *= (brightness + (0.5 + u_intensity)) * intensityBoost;
    fragColor.rgb += pow(max(fragColor.rgb, vec3(0.0)), vec3(0.74)) * (0.18 + u_intensity * 0.22);
    fragColor.a = clamp(rays1.a + rays2.a, 0.0, 1.0);

    outColor = fragColor;
}
