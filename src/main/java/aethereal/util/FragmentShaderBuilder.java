package aethereal.util;
import aethereal.resource.ByteArrayResource;
import aethereal.resource.ClasspathResource;
import aethereal.event.ClientInitEvent;
import aethereal.event.ClientListener;
import aethereal.Expensive;
import aethereal.resource.ResourceRouter;
import aethereal.render.ShaderProgram;

import java.nio.charset.StandardCharsets;
import org.lwjgl.opengl.GL33;

public class FragmentShaderBuilder implements ClientListener {
    public static final String SHADER_HEADER = "#version 330 core\n\n#define COLOR_MODE 0\n#define TEXTURE_MODE 1\n#define ROUNDED_RECTANGLE_MODE 2\n#define ROUNDED_TEXTURE_MODE 3\n#define BLUR 4\n#define CHECKER_MODE 5\n#define CIRCLE_MODE 6\n#define OUTER_MASK 7\n#define ALPHA_MASK 8\n#define MSDF_FONT 9\n#define RADIAL_ROUNDED_RECTANGLE_MODE 10\n\nin vec2 meshPosition;\nin vec2 meshSize;\nin vec2 texCoord;\nin vec4 radius;\nin vec4 color;\nin vec4 outlineColor;\nin float thickness;\nin float softness;\n\nflat in int texIndex;\nflat in int drawMode;\nflat in int maskIndex;\n\nout vec4 fragColor;\n\n";
    public static final String SHADER_FUNCTIONS = "float sdRoundedBox(in vec2 point, in vec2 size, in vec4 r) {\n    r.xy = (point.x > 0.0) ? r.xy : r.zw;\n    r.x = (point.y > 0.0) ? r.x : r.y;\n    vec2 q = abs(point) - size + r.x;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;\n}\n\nfloat median(vec3 color) {\n    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));\n}\nvec4 sampleTex(int id, vec2 uv);\nivec2 sampleTexSize(int id);\n\n";
    public static final String SHADER_MAIN = "void main() {\n    switch (drawMode) {\n        case COLOR_MODE:\n        {\n            fragColor = color;\n            break;\n        }\n        case TEXTURE_MODE:\n        {\n            vec4 textureColor = sampleTex(texIndex, texCoord);\n\n            if (textureColor.a == 0.0) {\n                discard;\n            }\n\n            fragColor = textureColor * color;\n            break;\n        }\n        case ROUNDED_RECTANGLE_MODE:\n        {\n            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);\n            float dist = sdRoundedBox(center, meshSize / 2.0, radius);\n            float smoothedAlpha = 1.0 - smoothstep(-1.0, thickness > 0. ? 1. : softness + 1., dist);\n            float smoothedborderAlpha = (1.0 - smoothstep(-softness, softness, dist));\n            float borderAlpha = 1.0 - smoothstep(thickness - 2.0, thickness, abs(dist));\n\n            if (smoothedAlpha < 0.49 && thickness > 0.) {\n                fragColor = vec4(outlineColor.rgb, outlineColor.a * smoothedborderAlpha);\n            } else {\n                vec4 basicColor = vec4(color.rgb, color.a * smoothedAlpha);\n\n                fragColor = mix(vec4(color.rgb, 0.), mix(basicColor, thickness > 0. ? outlineColor : basicColor,\n                outlineColor.a *  borderAlpha), smoothedAlpha);\n            }\n            break;\n        }\n        case ROUNDED_TEXTURE_MODE:\n        {\n            vec4 textureColor = sampleTex(texIndex, texCoord);\n            vec4 textureColorMultipliedByInputColor = textureColor * color;\n\n            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);\n            float dist = sdRoundedBox(center, meshSize / 2.0, radius);\n            float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);\n\n            fragColor = vec4(textureColorMultipliedByInputColor.rgb, textureColorMultipliedByInputColor.a * alpha);\n            break;\n        }\n        case BLUR:\n        {\n            vec2 pos = gl_FragCoord.xy;\n            vec2 blurredPos = pos / resolution;\n            vec4 textureColor = sampleTex(texIndex, blurredPos);\n            vec3 blurredColor = textureColor.rgb;\n\n            vec2 center = pos - meshPosition - (meshSize / 2.0);\n            float dist = sdRoundedBox(center, meshSize / 2.0, radius);\n            float alpha = 1.0 - smoothstep(-softness, softness, dist);\n\n            fragColor = vec4(blurredColor.rgb * color.rgb, color.a * alpha);\n            break;\n        }\n        case CHECKER_MODE: {\n            vec2 local = gl_FragCoord.xy - meshPosition;\n\n            float rows = 2.0;\n\n            float cellSize = min(meshSize.x, meshSize.y) / rows;\n\n            ivec2 cell = ivec2(floor(local / cellSize));\n\n            bool isWhite = (cell.x + cell.y) % 2 == 0;\n\n            vec3 checkerColor = isWhite ? color.rgb : outlineColor.rgb;\n            float alpha = isWhite ? color.a : outlineColor.a;\n\n            vec2 center = local - (meshSize * 0.5);\n            float dist = sdRoundedBox(center, meshSize * 0.5, radius);\n            float mask = 1.0 - smoothstep(-softness, softness, dist);\n\n            fragColor = vec4(checkerColor, alpha * mask);\n            break;\n        }\n        case CIRCLE_MODE:{\n            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);\n            float len = length(center);\n            float dist = len - radius.x;\n            if (radius.y > 0.0) {\n                dist = max(dist, radius.y - len);\n            }\n            float smoothedAlpha = 1.0 - smoothstep(-1.0, thickness > 0. ? 1. : softness + 1., dist);\n            float smoothedborderAlpha = (1.0 - smoothstep(-softness, softness, dist));\n            float borderAlpha = 1.0 - smoothstep(thickness - 2.0, thickness, abs(dist));\n\n            float angle = atan(center.y, center.x);\n            if (angle < 0.0) angle += 6.28318530718;\n            float start = texCoord.x;\n            float end = texCoord.y;\n            bool insideArc = end >= start ? (angle >= start && angle <= end) : (angle >= start || angle <= end);\n            if (!insideArc) discard;\n\n            if (smoothedAlpha < 0.49 && thickness > 0.) {\n                fragColor = vec4(outlineColor.rgb, outlineColor.a * smoothedborderAlpha);\n            } else {\n                vec4 basicColor = vec4(color.rgb, color.a * smoothedAlpha);\n\n                fragColor = mix(vec4(color.rgb, 0.), mix(basicColor, thickness > 0. ? outlineColor : basicColor,\n                outlineColor.a *  borderAlpha), smoothedAlpha);\n            }\n            break;\n        }\n        case OUTER_MASK: {\n            vec4 original = sampleTex(texIndex, texCoord);\n            float mask = sampleTex(maskIndex, texCoord).a;\n\n            vec3 rgb = original.rgb * (1.0 - mask);\n            float a  = original.a   * (1.0 - mask);\n\n            fragColor = vec4(rgb, a);\n            break;\n        }\n        case ALPHA_MASK: {\n            vec4 textureColor = sampleTex(texIndex, texCoord);\n            textureColor.a *= color.a;\n            fragColor = textureColor;\n            break;\n        }\n        case MSDF_FONT: {\n            float dist = median(sampleTex(texIndex, texCoord).rgb) - 0.5 + thickness;\n            vec2 h = vec2(dFdx(texCoord.x), dFdy(texCoord.y)) * vec2(sampleTexSize(texIndex));\n            float pixels = 10 * inversesqrt(h.x * h.x + h.y * h.y);\n            float alpha = smoothstep(-softness, softness, dist * pixels);\n            vec4 outColor = vec4(color.rgb, color.a * alpha);\n            fragColor = outColor;\n            break;\n        }\n        case RADIAL_ROUNDED_RECTANGLE_MODE: {\n            vec2 center = gl_FragCoord.xy - meshPosition - (meshSize / 2.0);\n            float dist = sdRoundedBox(center, meshSize / 2.0, radius);\n            float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);\n            vec2 normalized = center / (meshSize / 2.0);\n            float radial = clamp(length(normalized), 0.0, 1.0);\n            vec4 gradientColor = mix(color, outlineColor, radial);\n\n            fragColor = vec4(gradientColor.rgb, gradientColor.a * alpha);\n            break;\n        }\n    }\n}\n";

    public FragmentShaderBuilder() {
        Expensive.INSTANCE.eventDispatcher().register(ClientInitEvent.class, class179Var -> {
            int iMax = Math.max(1, GL33.glGetInteger(34930));
            Expensive.INSTANCE.drawEngine(new DrawEngine(new ShaderProgram(new ByteArrayResource(buildSource(iMax).getBytes(StandardCharsets.UTF_8)), new ResourceRouter("/", ClasspathResource::new).route("shaders/core.vsh")), iMax));
            Expensive.INSTANCE.windowController().init();
            Expensive.INSTANCE.hudBlur().init();
        });
    }

    public static String buildSource(int i) {
        StringBuilder sb = new StringBuilder(SHADER_HEADER);
        sb.append("uniform sampler2D textureSampler[").append(i).append("];\n").append("uniform vec2 resolution;\n\n").append(SHADER_FUNCTIONS);
        appendSampleTex(sb, i);
        appendSampleTexSize(sb, i);
        sb.append(SHADER_MAIN);
        return sb.toString();
    }

    public static void appendSampleTex(StringBuilder sb, int i) {
        sb.append("vec4 sampleTex(int id, vec2 uv) {\n");
        for (int i2 = 0; i2 < i; i2++) {
            sb.append("    if (id == ").append(i2).append(") return texture(textureSampler[").append(i2).append("], uv);\n");
        }
        sb.append("    return vec4(1.0, 0.0, 1.0, 1.0);\n").append("}\n\n");
    }

    public static void appendSampleTexSize(StringBuilder sb, int i) {
        sb.append("ivec2 sampleTexSize(int id) {\n");
        for (int i2 = 0; i2 < i; i2++) {
            sb.append("    if (id == ").append(i2).append(") return textureSize(textureSampler[").append(i2).append("], 0);\n");
        }
        sb.append("    return ivec2(1, 1);\n").append("}\n\n");
    }
}
