package com.luminary.crates.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for text formatting and colorization.
 */
public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {
        // Utility class
    }

    /**
     * Convert a string with color codes to a Component.
     * Supports both legacy (&) codes and MiniMessage format.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Check if it uses MiniMessage format
        if (text.contains("<") && text.contains(">")) {
            return MINI_MESSAGE.deserialize(text)
                    .decoration(TextDecoration.ITALIC, false);
        }

        // Use legacy color codes
        return LEGACY_SERIALIZER.deserialize(text)
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Strip color codes from a string.
     */
    public static String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "");
    }

    /**
     * Convert a Component back to a legacy string.
     */
    public static String toLegacy(Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Format a number with commas (e.g., 1000000 -> 1,000,000).
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    /**
     * Format a decimal number with specified precision.
     */
    public static String formatDecimal(double number, int precision) {
        return String.format("%,." + precision + "f", number);
    }

    /**
     * Center a string in chat (approximately).
     */
    public static String centerText(String text) {
        final int CENTER_PX = 154;
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : text.toCharArray()) {
            if (c == '&') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                messagePxSize += getCharWidth(c, isBold);
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = 4;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();

        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + text;
    }

    private static int getCharWidth(char c, boolean bold) {
        int width;
        switch (c) {
            case ' ' -> width = 4;
            case '!' -> width = 2;
            case '"' -> width = 5;
            case '\'' -> width = 3;
            case '(' -> width = 5;
            case ')' -> width = 5;
            case '*' -> width = 5;
            case ',' -> width = 2;
            case '.' -> width = 2;
            case ':' -> width = 2;
            case ';' -> width = 2;
            case '<' -> width = 5;
            case '>' -> width = 5;
            case '@' -> width = 7;
            case 'I' -> width = 4;
            case '[' -> width = 4;
            case ']' -> width = 4;
            case 'f' -> width = 5;
            case 'i' -> width = 2;
            case 'k' -> width = 5;
            case 'l' -> width = 3;
            case 't' -> width = 4;
            case '|' -> width = 2;
            case '~' -> width = 7;
            case '`' -> width = 3;
            default -> width = 6;
        }
        return bold ? width + 1 : width;
    }
}
