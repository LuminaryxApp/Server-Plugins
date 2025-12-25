package com.luminary.core.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for time parsing and formatting.
 */
public class TimeUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdwMy])");

    /**
     * Parse a time string like "1d", "2h30m", "1w" into milliseconds.
     * Supports: s (seconds), m (minutes), h (hours), d (days), w (weeks), M (months), y (years)
     */
    public static long parseTime(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }

        long total = 0;
        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase().replace("M", "O")); // Temp replace M for months
        String adjustedInput = input;

        // Handle months specially
        if (input.contains("M")) {
            adjustedInput = input.replace("M", "O");
        }

        matcher = TIME_PATTERN.matcher(adjustedInput.replace("O", "M"));

        Matcher actualMatcher = TIME_PATTERN.matcher(input);
        while (actualMatcher.find()) {
            long amount = Long.parseLong(actualMatcher.group(1));
            String unit = actualMatcher.group(2);

            switch (unit) {
                case "s":
                    total += TimeUnit.SECONDS.toMillis(amount);
                    break;
                case "m":
                    total += TimeUnit.MINUTES.toMillis(amount);
                    break;
                case "h":
                    total += TimeUnit.HOURS.toMillis(amount);
                    break;
                case "d":
                    total += TimeUnit.DAYS.toMillis(amount);
                    break;
                case "w":
                    total += TimeUnit.DAYS.toMillis(amount * 7);
                    break;
                case "M":
                    total += TimeUnit.DAYS.toMillis(amount * 30);
                    break;
                case "y":
                    total += TimeUnit.DAYS.toMillis(amount * 365);
                    break;
                default:
                    return -1;
            }
        }

        return total > 0 ? total : -1;
    }

    /**
     * Format milliseconds into a human-readable string.
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "Permanent";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            long remainingMonths = (days % 365) / 30;
            if (remainingMonths > 0) {
                return years + "y " + remainingMonths + "mo";
            }
            return years + " year" + (years > 1 ? "s" : "");
        }
        if (months > 0) {
            long remainingDays = days % 30;
            if (remainingDays > 0) {
                return months + "mo " + remainingDays + "d";
            }
            return months + " month" + (months > 1 ? "s" : "");
        }
        if (weeks > 0) {
            long remainingDays = days % 7;
            if (remainingDays > 0) {
                return weeks + "w " + remainingDays + "d";
            }
            return weeks + " week" + (weeks > 1 ? "s" : "");
        }
        if (days > 0) {
            long remainingHours = hours % 24;
            if (remainingHours > 0) {
                return days + "d " + remainingHours + "h";
            }
            return days + " day" + (days > 1 ? "s" : "");
        }
        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            if (remainingMinutes > 0) {
                return hours + "h " + remainingMinutes + "m";
            }
            return hours + " hour" + (hours > 1 ? "s" : "");
        }
        if (minutes > 0) {
            long remainingSeconds = seconds % 60;
            if (remainingSeconds > 0) {
                return minutes + "m " + remainingSeconds + "s";
            }
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }
        return seconds + " second" + (seconds != 1 ? "s" : "");
    }

    /**
     * Format remaining time until expiry.
     */
    public static String formatRemaining(long expiryTime) {
        if (expiryTime < 0) {
            return "Permanent";
        }
        long remaining = expiryTime - System.currentTimeMillis();
        if (remaining <= 0) {
            return "Expired";
        }
        return formatDuration(remaining);
    }

    /**
     * Get a timestamp string.
     */
    public static String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}
