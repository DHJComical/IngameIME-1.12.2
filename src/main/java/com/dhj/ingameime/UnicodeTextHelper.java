package com.dhj.ingameime;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public final class UnicodeTextHelper {
    private UnicodeTextHelper() {
    }

    public static String repairUtf8Mojibake(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder out = new StringBuilder(text.length());
        StringBuilder segment = new StringBuilder();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            int b = mojibakeByte(codePoint);
            if (b >= 0) {
                segment.appendCodePoint(codePoint);
                bytes.write(b);
            } else {
                flushSegment(out, segment, bytes);
                out.appendCodePoint(codePoint);
            }
        }

        flushSegment(out, segment, bytes);
        return out.toString();
    }

    public static String debugCodePoints(String text) {
        if (text == null) {
            return "null";
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (out.length() > 0) {
                out.append(' ');
            }
            out.append("U+");
            appendHex(out, codePoint, codePoint <= 0xFFFF ? 4 : 6);
        }
        return out.toString();
    }

    public static String debugUtf16(String text) {
        if (text == null) {
            return "null";
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append("U+");
            appendHex(out, text.charAt(i), 4);
        }
        return out.toString();
    }

    public static String debugEscaped(String text) {
        if (text == null) {
            return "null";
        }

        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            switch (codePoint) {
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\'':
                    out.append("\\'");
                    break;
                default:
                    if (Character.isISOControl(codePoint)) {
                        out.append("\\u{");
                        appendHex(out, codePoint, 4);
                        out.append('}');
                    } else {
                        out.appendCodePoint(codePoint);
                    }
                    break;
            }
        }
        return out.toString();
    }

    public static boolean isEmojiCodePoint(int codePoint) {
        return isEmojiLikeCodePoint(codePoint);
    }

    public static boolean isEmojiFormatCodePoint(int codePoint) {
        return codePoint == 0x200D
                || codePoint == 0xFE0E
                || codePoint == 0xFE0F
                || (codePoint >= 0xE0020 && codePoint <= 0xE007F);
    }

    private static void appendHex(StringBuilder out, int value, int minWidth) {
        String hex = Integer.toHexString(value).toUpperCase();
        for (int i = hex.length(); i < minWidth; i++) {
            out.append('0');
        }
        out.append(hex);
    }

    private static void flushSegment(StringBuilder out, StringBuilder segment, ByteArrayOutputStream bytes) {
        if (segment.length() == 0) {
            return;
        }

        String original = segment.toString();
        String decoded = decodeUtf8(bytes.toByteArray());
        if (decoded != null && !decoded.equals(original) && containsEmojiCodepoint(decoded)) {
            out.append(decoded);
        } else {
            out.append(original);
        }

        segment.setLength(0);
        bytes.reset();
    }

    private static String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ignored) {
            return null;
        }
    }

    private static int mojibakeByte(int codePoint) {
        if (codePoint <= 0x00FF) {
            return codePoint;
        }

        switch (codePoint) {
            case 0x20AC: return 0x80;
            case 0x201A: return 0x82;
            case 0x0192: return 0x83;
            case 0x201E: return 0x84;
            case 0x2026: return 0x85;
            case 0x2020: return 0x86;
            case 0x2021: return 0x87;
            case 0x02C6: return 0x88;
            case 0x2030: return 0x89;
            case 0x0160: return 0x8A;
            case 0x2039: return 0x8B;
            case 0x0152: return 0x8C;
            case 0x017D: return 0x8E;
            case 0x2018: return 0x91;
            case 0x2019: return 0x92;
            case 0x201C: return 0x93;
            case 0x201D: return 0x94;
            case 0x2022: return 0x95;
            case 0x2013: return 0x96;
            case 0x2014: return 0x97;
            case 0x02DC: return 0x98;
            case 0x2122: return 0x99;
            case 0x0161: return 0x9A;
            case 0x203A: return 0x9B;
            case 0x0153: return 0x9C;
            case 0x017E: return 0x9E;
            case 0x0178: return 0x9F;
            default: return -1;
        }
    }

    private static boolean containsEmojiCodepoint(String text) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (isEmojiLikeCodePoint(codePoint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmojiLikeCodePoint(int codePoint) {
        return codePoint == 0x00A9
                || codePoint == 0x00AE
                || codePoint == 0x203C
                || codePoint == 0x2049
                || codePoint == 0x20E3
                || codePoint == 0x2122
                || codePoint == 0x2139
                || codePoint == 0x200D
                || codePoint == 0xFE0E
                || codePoint == 0xFE0F
                || (codePoint >= 0x2194 && codePoint <= 0x21AA)
                || (codePoint >= 0x231A && codePoint <= 0x23FF)
                || (codePoint >= 0x2460 && codePoint <= 0x24FF)
                || (codePoint >= 0x25A0 && codePoint <= 0x25FF)
                || (codePoint >= 0x2600 && codePoint <= 0x27BF)
                || (codePoint >= 0x2900 && codePoint <= 0x2BFF)
                || codePoint == 0x3030
                || codePoint == 0x303D
                || codePoint == 0x3297
                || codePoint == 0x3299
                || (codePoint >= 0x1F000 && codePoint <= 0x1FAFF)
                || (codePoint >= 0xE0020 && codePoint <= 0xE007F);
    }
}
