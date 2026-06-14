package com.dhj.ingameime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

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

            if (isRegionalIndicator(codePoint) && i < text.length()) {
                int next = text.codePointAt(i);
                if (isRegionalIndicator(next)) {
                    i += Character.charCount(next);
                }
            } else {
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

    public static String debugForCurrentFont(String text) {
        return UnicodeTextHelper.debugEscaped(forCurrentFont(text));
    }

    private static boolean shouldReplaceCluster(FontRenderer font, String cluster) {
        if (requiresEmojiFont(cluster)) {
            return true;
        }
        return font.getStringWidth(cluster) <= 0;
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
                || codePoint == 0x20E3
                || (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF)
                || (codePoint >= 0xE0020 && codePoint <= 0xE007F);
    }

    private static boolean isVariationSelector(int codePoint) {
        return codePoint == 0xFE0E
                || codePoint == 0xFE0F
                || (codePoint >= 0xE0100 && codePoint <= 0xE01EF);
    }

    private static boolean isRegionalIndicator(int codePoint) {
        return codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF;
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
}
