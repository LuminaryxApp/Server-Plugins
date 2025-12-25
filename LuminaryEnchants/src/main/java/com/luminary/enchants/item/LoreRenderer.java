package com.luminary.enchants.item;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.pickaxe.EnchantRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Renders enchant lore for pickaxes based on PDC data and config.
 */
public class LoreRenderer {

    private final LuminaryEnchants plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Config-driven formatting
    private String prefix;
    private String bulletStyle;
    private String levelFormat;
    private String separator;
    private Map<EnchantRarity, String> rarityColors;
    private String sortOrder; // "rarity", "level", "alphabetical"

    public LoreRenderer(LuminaryEnchants plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        ConfigurationSection loreSection = plugin.getConfigManager().getMainConfig()
                .getConfigurationSection("lore");

        if (loreSection == null) {
            // Defaults
            prefix = "";
            bulletStyle = "&7• ";
            levelFormat = "&7(&f{level}&7)";
            separator = " ";
            sortOrder = "rarity";
            rarityColors = new EnumMap<>(EnchantRarity.class);
            rarityColors.put(EnchantRarity.COMMON, "&f");
            rarityColors.put(EnchantRarity.UNCOMMON, "&a");
            rarityColors.put(EnchantRarity.RARE, "&b");
            rarityColors.put(EnchantRarity.EPIC, "&5");
            rarityColors.put(EnchantRarity.LEGENDARY, "&6");
            return;
        }

        prefix = loreSection.getString("prefix", "");
        bulletStyle = loreSection.getString("bullet-style", "&7• ");
        levelFormat = loreSection.getString("level-format", "&7(&f{level}&7)");
        separator = loreSection.getString("separator", " ");
        sortOrder = loreSection.getString("sort-order", "rarity");

        rarityColors = new EnumMap<>(EnchantRarity.class);
        ConfigurationSection colorsSection = loreSection.getConfigurationSection("rarity-colors");
        if (colorsSection != null) {
            for (EnchantRarity rarity : EnchantRarity.values()) {
                String color = colorsSection.getString(rarity.name().toLowerCase(),
                        getDefaultRarityColor(rarity));
                rarityColors.put(rarity, color);
            }
        } else {
            for (EnchantRarity rarity : EnchantRarity.values()) {
                rarityColors.put(rarity, getDefaultRarityColor(rarity));
            }
        }
    }

    private String getDefaultRarityColor(EnchantRarity rarity) {
        return switch (rarity) {
            case COMMON -> "&f";
            case UNCOMMON -> "&a";
            case RARE -> "&b";
            case EPIC -> "&5";
            case LEGENDARY -> "&6";
        };
    }

    /**
     * Update the lore of an item based on its enchants.
     */
    public void updateLore(ItemMeta meta, Map<String, Integer> enchants) {
        List<Component> lore = new ArrayList<>();

        if (enchants.isEmpty()) {
            meta.lore(lore);
            return;
        }

        // Get enchant definitions and sort
        List<EnchantLoreEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(entry.getKey());
            if (def != null) {
                entries.add(new EnchantLoreEntry(def, entry.getValue()));
            }
        }

        // Sort based on config
        sortEntries(entries);

        // Add blank line before enchants if there's existing lore
        if (!prefix.isEmpty()) {
            lore.add(translateLegacy(prefix));
        }

        // Build lore lines
        for (EnchantLoreEntry entry : entries) {
            String line = buildLoreLine(entry);
            lore.add(translateLegacy(line));
        }

        meta.lore(lore);
    }

    private void sortEntries(List<EnchantLoreEntry> entries) {
        switch (sortOrder.toLowerCase()) {
            case "level" -> entries.sort((a, b) -> Integer.compare(b.level, a.level));
            case "alphabetical" -> entries.sort(Comparator.comparing(e -> e.definition.getDisplayName()));
            case "rarity" -> entries.sort((a, b) -> {
                int rarityCompare = Integer.compare(
                        b.definition.getRarity().ordinal(),
                        a.definition.getRarity().ordinal()
                );
                if (rarityCompare != 0) return rarityCompare;
                return Integer.compare(b.level, a.level);
            });
            default -> {} // Keep original order
        }
    }

    private String buildLoreLine(EnchantLoreEntry entry) {
        EnchantDefinition def = entry.definition;
        String color = rarityColors.getOrDefault(def.getRarity(), "&f");

        StringBuilder line = new StringBuilder();
        line.append(bulletStyle);
        line.append(color);
        line.append(def.getDisplayName());
        line.append(separator);
        line.append(levelFormat.replace("{level}", formatLevel(entry.level, def.getMaxLevel())));

        return line.toString();
    }

    private String formatLevel(int level, int maxLevel) {
        // Could add formatting like "MAX" when at max level
        if (level >= maxLevel) {
            return String.valueOf(level) + " &7[MAX]";
        }
        return String.valueOf(level);
    }

    /**
     * Translate legacy color codes and return a Component.
     */
    private Component translateLegacy(String text) {
        // Convert legacy & codes to MiniMessage format
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

        return miniMessage.deserialize(converted).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Create a display component for GUI use.
     */
    public Component createDisplayComponent(String text) {
        return translateLegacy(text);
    }

    private static class EnchantLoreEntry {
        final EnchantDefinition definition;
        final int level;

        EnchantLoreEntry(EnchantDefinition definition, int level) {
            this.definition = definition;
            this.level = level;
        }
    }
}
