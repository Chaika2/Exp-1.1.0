package aethereal.util;
import aethereal.type.Mc;
import aethereal.render.MsdfFont;

import java.util.regex.Pattern;
import net.minecraft.util.StringHelper;

public final class StringUtil {
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");

    public static String cutoff(String str, float f, float f2, float f3, boolean z) {
        if (!z) {
            return str;
        }
        int iMax = Math.max(0, str.length() - ((int) Math.ceil((f - f2) / (f3 / str.length()))));
        String strSubstring = str.substring(0, iMax);
        if (iMax < str.length()) {
            strSubstring = strSubstring + "...";
        }
        return strSubstring;
    }

    public static void copyToClipboard(String str) {
        Mc.INSTANCE.getMinecraft().keyboard.setClipboard(str);
    }

    public static boolean hasIllegalCharacter(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!StringHelper.isValidChar(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String lowerCase = str.toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }

    public static String firstLetterUppercase(String str) {
        return str.length() <= 1 ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String formatTextToFitWidth(String str, float f, MsdfFont class161Var, int i) {
        StringBuilder sb = new StringBuilder(str.length() + 16);
        StringBuilder sb2 = new StringBuilder();
        float f2 = 0.0f;
        float width = class161Var.getWidth(" ", i);
        int length = str.length();
        int i2 = 0;
        while (i2 < length) {
            while (i2 < length && Character.isWhitespace(str.charAt(i2))) {
                if (str.charAt(i2) == '\n') {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append((CharSequence) sb2);
                    sb2.setLength(0);
                    f2 = 0.0f;
                }
                i2++;
            }
            if (i2 >= length) {
                break;
            }
            int i3 = i2;
            while (i2 < length && !Character.isWhitespace(str.charAt(i2))) {
                i2++;
            }
            String strSubstring = str.substring(i3, i2);
            float width2 = class161Var.getWidth(strSubstring, i);
            if (width2 > f) {
                if (!sb2.isEmpty()) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append((CharSequence) sb2);
                    sb2.setLength(0);
                    f2 = 0.0f;
                }
                int i4 = 0;
                while (true) {
                    int i5 = i4;
                    if (i5 >= strSubstring.length()) {
                        break;
                    }
                    {
                        int i6 = i5 + 1;
                        while (i6 <= strSubstring.length() && class161Var.getWidth(strSubstring.substring(i5, i6), i) <= f) {
                            i6++;
                        }
                        int i7 = i6 - 1;
                        if (i7 <= i5) {
                            i7 = i5 + 1;
                        }
                        if (!sb.isEmpty()) {
                            sb.append('\n');
                        }
                        sb.append((CharSequence) strSubstring, i5, i7);
                        i4 = i7;
                    }
                }
            } else {
                float f3 = f2 > 0.0f ? width : 0.0f;
                if (f2 + f3 + width2 <= f) {
                    if (f3 > 0.0f) {
                        sb2.append(' ');
                        f2 += width;
                    }
                    sb2.append(strSubstring);
                    f2 += width2;
                } else {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append((CharSequence) sb2);
                    sb2.setLength(0);
                    sb2.append(strSubstring);
                    f2 = width2;
                }
            }
        }
        if (sb2.length() > 0) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append((CharSequence) sb2);
        }
        return sb.toString();
    }

    public static String removeFormatting(String str) {
        return FORMATTING_CODE_PATTERN.matcher(str).replaceAll("");
    }

    public static String removeSymbols(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if ((c >= ' ' && c <= '~') || (c >= 1024 && c <= 1279)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public StringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
