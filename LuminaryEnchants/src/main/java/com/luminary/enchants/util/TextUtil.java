package com.luminary.enchants.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Text and formatting utilities.
 */
public class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    /**
     * Convert legacy color codes (&a, &b, etc.) to Adventure Component.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert MiniMessage format to Component.
     */
    public static Component miniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert legacy color codes to MiniMessage format then to Component.
     */
    public static Component legacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String converted = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");

        return MINI_MESSAGE.deserialize(converted).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Format a number with commas.
     */
    public static String formatNumber(long number) {
        return NUMBER_FORMAT.format(number);
    }

    /**
     * Format a number with commas and decimals.
     */
    public static String formatDecimal(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * Format a percentage (0-1 to 0%-100%).
     */
    public static String formatPercent(double value) {
        return DECIMAL_FORMAT.format(value * 100) + "%";
    }

    /**
     * Format milliseconds to a human-readable duration.
     */
    public static String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        } else if (ms < 60000) {
            double seconds = ms / 1000.0;
            return DECIMAL_FORMAT.format(seconds) + "s";
        } else if (ms < 3600000) {
            double minutes = ms / 60000.0;
            return DECIMAL_FORMAT.format(minutes) + "m";
        } else {
            double hours = ms / 3600000.0;
            return DECIMAL_FORMAT.format(hours) + "h";
        }
    }

    /**
     * Format ticks to seconds.
     */
    public static String formatTicks(int ticks) {
        double seconds = ticks / 20.0;
        return DECIMAL_FORMAT.format(seconds) + "s";
    }

    /**
     * Capitalize first letter.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Convert snake_case or UPPER_CASE to Title Case.
     */
    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.toLowerCase().replace("_", " ").split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(capitalize(word));
            }
        }

        return result.toString();
    }

    /**
     * Strip color codes from text.
     */
    public static String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "");
    }

    /**
     * Truncate text with ellipsis.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Replace placeholders in text.
     */
    public static String replace(String text, String placeholder, String value) {
        if (text == null) return null;
        return text.replace("{" + placeholder + "}", value);
    }
}
