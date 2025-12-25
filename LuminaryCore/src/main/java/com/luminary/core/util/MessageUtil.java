package com.luminary.core.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for message formatting and sending.
 */
public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Colorize a string with & color codes and hex colors.
     */
    public static String colorize(String message) {
        if (message == null) return "";

        // Handle hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder(ChatColor.COLOR_CHAR + "x");
            for (char c : hex.toCharArray()) {
                replacement.append(ChatColor.COLOR_CHAR).append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Send a colorized message to a sender.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Send multiple colorized messages.
     */
    public static void send(CommandSender sender, String... messages) {
        for (String message : messages) {
            send(sender, message);
        }
    }

    /**
     * Send a message with replacements.
     */
    public static void send(CommandSender sender, String message, Object... replacements) {
        String msg = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        send(sender, msg);
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Format a number with commas.
     */
    public static String formatNumber(double number) {
        if (number >= 1000000000) {
            return String.format("%.2fB", number / 1000000000);
        } else if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.2fK", number / 1000);
        } else {
            return String.format("%.0f", number);
        }
    }

    /**
     * Format a number with commas (integer).
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
}
