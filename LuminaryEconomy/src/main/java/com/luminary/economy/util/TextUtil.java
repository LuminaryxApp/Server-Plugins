package com.luminary.economy.util;

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
    }

    /**
     * Convert a string with color codes to a Component.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        if (text.contains("<") && text.contains(">")) {
            return MINI_MESSAGE.deserialize(text)
                    .decoration(TextDecoration.ITALIC, false);
        }

        return LEGACY_SERIALIZER.deserialize(text)
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert a string with color codes to legacy string (for scoreboards).
     */
    public static String colorizeString(String text) {
        if (text == null) return "";
        return text.replace("&", "\u00A7");
    }

    /**
     * Strip color codes from a string.
     */
    public static String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("\u00A7[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "");
    }

    /**
     * Format a number with commas.
     */
    public static String formatNumber(double number) {
        if (number == (long) number) {
            return String.format("%,d", (long) number);
        }
        return String.format("%,.2f", number);
    }

    /**
     * Format a number in compact form (1.5K, 2.3M, etc.).
     */
    public static String formatCompact(double number) {
        if (number >= 1_000_000_000_000L) {
            return String.format("%.2fT", number / 1_000_000_000_000.0);
        } else if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.2fK", number / 1_000.0);
        }
        return formatNumber(number);
    }

    /**
     * Parse a string amount that may include suffixes (K, M, B, T).
     */
    public static double parseAmount(String input) throws NumberFormatException {
        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }

        input = input.toUpperCase().replace(",", "").trim();

        double multiplier = 1;
        if (input.endsWith("K")) {
            multiplier = 1_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("M")) {
            multiplier = 1_000_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("B")) {
            multiplier = 1_000_000_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("T")) {
            multiplier = 1_000_000_000_000L;
            input = input.substring(0, input.length() - 1);
        }

        return Double.parseDouble(input) * multiplier;
    }
}
