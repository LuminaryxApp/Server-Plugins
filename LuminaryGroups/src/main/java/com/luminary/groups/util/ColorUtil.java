package com.luminary.groups.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for color code handling.
 */
public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Colorize a string with legacy color codes and hex colors.
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }

        // Handle hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00A7').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        message = buffer.toString();

        // Handle legacy color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Convert string to Adventure Component with colors.
     */
    public static Component toComponent(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(colorize(message));
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Check if a string contains color codes.
     */
    public static boolean hasColorCodes(String message) {
        if (message == null) {
            return false;
        }
        return message.contains("&") || message.contains("\u00A7") || HEX_PATTERN.matcher(message).find();
    }
}
