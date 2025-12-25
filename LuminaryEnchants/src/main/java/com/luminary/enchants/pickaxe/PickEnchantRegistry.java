package com.luminary.enchants.pickaxe;

import com.luminary.enchants.LuminaryEnchants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Registry for all pickaxe enchants.
 * Loads enchant definitions from configuration.
 */
public class PickEnchantRegistry {

    private final LuminaryEnchants plugin;
    private final Map<String, EnchantDefinition> enchants = new LinkedHashMap<>();
    private final Map<EnchantTrigger, List<EnchantDefinition>> enchantsByTrigger = new EnumMap<>(EnchantTrigger.class);

    public PickEnchantRegistry(LuminaryEnchants plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all enchants from the enchants.yml configuration.
     */
    public void loadEnchants() {
        enchants.clear();
        enchantsByTrigger.clear();

        // Initialize trigger map
        for (EnchantTrigger trigger : EnchantTrigger.values()) {
            enchantsByTrigger.put(trigger, new ArrayList<>());
        }

        FileConfiguration config = plugin.getConfigManager().getEnchantsConfig();
        ConfigurationSection enchantsSection = config.getConfigurationSection("enchants");

        if (enchantsSection == null) {
            plugin.getLogger().warning("No enchants section found in enchants.yml!");
            return;
        }

        for (String key : enchantsSection.getKeys(false)) {
            ConfigurationSection enchantSection = enchantsSection.getConfigurationSection(key);
            if (enchantSection == null) continue;

            try {
                EnchantDefinition definition = new EnchantDefinition(enchantSection);
                enchants.put(definition.getId(), definition);

                // Index by trigger
                for (EnchantTrigger trigger : definition.getTriggers()) {
                    enchantsByTrigger.get(trigger).add(definition);
                }

                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("Loaded enchant: " + definition.getId() +
                            " (" + definition.getRarity() + ", max " + definition.getMaxLevel() + ")");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load enchant '" + key + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + enchants.size() + " pickaxe enchants.");
    }

    /**
     * Get an enchant definition by ID.
     */
    public EnchantDefinition getEnchant(String id) {
        return enchants.get(id);
    }

    /**
     * Get all registered enchants.
     */
    public Collection<EnchantDefinition> getAllEnchants() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    /**
     * Get all enchants with a specific trigger.
     */
    public List<EnchantDefinition> getEnchantsByTrigger(EnchantTrigger trigger) {
        return Collections.unmodifiableList(enchantsByTrigger.getOrDefault(trigger, Collections.emptyList()));
    }

    /**
     * Get all enchant IDs.
     */
    public Set<String> getEnchantIds() {
        return Collections.unmodifiableSet(enchants.keySet());
    }

    /**
     * Check if an enchant exists.
     */
    public boolean hasEnchant(String id) {
        return enchants.containsKey(id);
    }

    /**
     * Get the total number of registered enchants.
     */
    public int getEnchantCount() {
        return enchants.size();
    }

    /**
     * Get enchants by rarity.
     */
    public List<EnchantDefinition> getEnchantsByRarity(EnchantRarity rarity) {
        List<EnchantDefinition> result = new ArrayList<>();
        for (EnchantDefinition def : enchants.values()) {
            if (def.getRarity() == rarity) {
                result.add(def);
            }
        }
        return result;
    }

    /**
     * Get enchants sorted by rarity (legendary first).
     */
    public List<EnchantDefinition> getEnchantsSortedByRarity() {
        List<EnchantDefinition> sorted = new ArrayList<>(enchants.values());
        sorted.sort((a, b) -> Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal()));
        return sorted;
    }
}
