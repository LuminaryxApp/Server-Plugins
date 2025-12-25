package com.luminary.ranks.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.text.NumberFormat;
import java.util.Locale;

public class TextUtil {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return SERIALIZER.deserialize(text);
    }

    public static String formatNumber(long number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public static String formatMultiplier(double multiplier) {
        return String.format("%.1fx", multiplier);
    }

    public static String formatPercent(double value) {
        return String.format("%.0f%%", value * 100);
    }
}
