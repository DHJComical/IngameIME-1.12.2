package com.dhj.ingameime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CandidateDisplayText {
    private static final String FALLBACK = "\u00D7";

    private CandidateDisplayText() {
    }

    public static String forCurrentFont(String text) {
        if (text == null) {
            return text;
        }
        if (text.isEmpty()) {
            return FALLBACK;
        }

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc == null ? null : mc.fontRenderer;
        if (font == null) {
            return text;
        }

        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ) {
            int start = i;
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            while (i < text.length()) {
                int next = text.codePointAt(i);
                if (next == 0x200D && i + Character.charCount(next) < text.length()) {
                    i += Character.charCount(next);
                    i += Character.charCount(text.codePointAt(i));
                } else if (isEmojiClusterContinuation(next)) {
                    i += Character.charCount(next);
                } else {
                    break;
                }
            }

            String cluster = text.substring(start, i);
            if (shouldReplaceCluster(font, cluster)) {
                out.append(FALLBACK);
            } else {
                out.append(cluster);
            }
        }
        return out.toString();
    }

    private static boolean shouldReplaceCluster(FontRenderer font, String cluster) {
        boolean emoji = containsEmojiCodePoint(cluster);
        boolean neoActive = isNeoFontRenderActive();
        if (neoActive && emoji) {
            return false;
        }
        if (!neoActive && requiresEmojiFont(cluster)) {
            return true;
        }
        return font.getStringWidth(cluster) <= 0;
    }

    public static String debugForCurrentFont(String text) {
        String display = forCurrentFont(text);
        return UnicodeTextHelper.debugEscaped(display);
    }

    private static boolean isVariationSelector(int codePoint) {
        return codePoint == 0xFE0E
                || codePoint == 0xFE0F
                || (codePoint >= 0xE0100 && codePoint <= 0xE01EF);
    }

    private static boolean isEmojiClusterContinuation(int codePoint) {
        if (isVariationSelector(codePoint)) {
            return true;
        }
        int type = Character.getType(codePoint);
        if (type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || type == Character.ENCLOSING_MARK) {
            return true;
        }
        return codePoint == 0x200D
                || (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF)
                || (codePoint >= 0xE0020 && codePoint <= 0xE007F)
                || codePoint == 0x20E3;
    }

    private static boolean containsEmojiCodePoint(String text) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (UnicodeTextHelper.isEmojiCodePoint(codePoint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean requiresEmojiFont(String text) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint > 0xFFFF && UnicodeTextHelper.isEmojiCodePoint(codePoint)) {
                return true;
            }
            if (codePoint == 0x200D || (codePoint >= 0xE0020 && codePoint <= 0xE007F)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNeoFontRenderActive() {
        try {
            Class<?> fontManagerClass = Class.forName(
                    "neofontrender.core.font.FontManager",
                    false,
                    CandidateDisplayText.class.getClassLoader()
            );
            Field instanceField = fontManagerClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            return callBoolean(instance, fontManagerClass, "isSkiaActive")
                    || callBoolean(instance, fontManagerClass, "isSfrActive")
                    || callBoolean(instance, fontManagerClass, "isActive");
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean callBoolean(Object target, Class<?> targetClass, String methodName) {
        try {
            Method method = targetClass.getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Boolean && (Boolean) value;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
