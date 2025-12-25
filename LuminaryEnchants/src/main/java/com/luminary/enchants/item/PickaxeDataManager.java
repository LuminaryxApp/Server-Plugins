package com.luminary.enchants.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.luminary.enchants.LuminaryEnchants;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pickaxe enchant data storage using PersistentDataContainer.
 */
public class PickaxeDataManager {

    private static final Set<Material> VALID_PICKAXES = EnumSet.of(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    );

    private final LuminaryEnchants plugin;
    private final NamespacedKey enchantsKey;
    private final NamespacedKey metaKey;
    private final Gson gson;
    private final LoreRenderer loreRenderer;

    // Cache for parsed enchant data (TTL-based)
    private final Map<Integer, CachedEnchantData> enchantCache = new ConcurrentHashMap<>();

    private static final Type ENCHANT_MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

    public PickaxeDataManager(LuminaryEnchants plugin) {
        this.plugin = plugin;
        this.enchantsKey = new NamespacedKey(plugin, "pick_enchants");
        this.metaKey = new NamespacedKey(plugin, "pick_meta");
        this.gson = new GsonBuilder().create();
        this.loreRenderer = new LoreRenderer(plugin);
    }

    /**
     * Check if an item is a valid pickaxe.
     */
    public boolean isPickaxe(ItemStack item) {
        return item != null && VALID_PICKAXES.contains(item.getType());
    }

    /**
     * Get all enchants on a pickaxe.
     * Uses caching for performance.
     */
    public Map<String, Integer> getEnchants(ItemStack item) {
        if (!isPickaxe(item)) {
            return Collections.emptyMap();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Collections.emptyMap();
        }

        // Check cache
        int itemHash = System.identityHashCode(item);
        CachedEnchantData cached = enchantCache.get(itemHash);
        int ttl = plugin.getConfigManager().getCacheTtlMs();

        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < ttl) {
            return cached.enchants;
        }

        // Parse from PDC
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String json = pdc.get(enchantsKey, PersistentDataType.STRING);

        Map<String, Integer> enchants;
        if (json == null || json.isEmpty()) {
            enchants = new HashMap<>();
        } else {
            try {
                enchants = gson.fromJson(json, ENCHANT_MAP_TYPE);
                if (enchants == null) enchants = new HashMap<>();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse enchant data: " + e.getMessage());
                enchants = new HashMap<>();
            }
        }

        // Cache result
        enchantCache.put(itemHash, new CachedEnchantData(enchants, System.currentTimeMillis()));

        return enchants;
    }

    /**
     * Get the level of a specific enchant on a pickaxe.
     */
    public int getEnchantLevel(ItemStack item, String enchantId) {
        return getEnchants(item).getOrDefault(enchantId, 0);
    }

    /**
     * Check if a pickaxe has a specific enchant.
     */
    public boolean hasEnchant(ItemStack item, String enchantId) {
        return getEnchantLevel(item, enchantId) > 0;
    }

    /**
     * Set an enchant on a pickaxe.
     * Returns the modified ItemStack.
     */
    public ItemStack setEnchant(ItemStack item, String enchantId, int level) {
        if (!isPickaxe(item)) {
            return item;
        }

        Map<String, Integer> enchants = new HashMap<>(getEnchants(item));

        if (level <= 0) {
            enchants.remove(enchantId);
        } else {
            enchants.put(enchantId, level);
        }

        return saveEnchants(item, enchants);
    }

    /**
     * Add levels to an enchant (upgrade).
     */
    public ItemStack addEnchantLevels(ItemStack item, String enchantId, int levelsToAdd, int maxLevel) {
        int currentLevel = getEnchantLevel(item, enchantId);
        int newLevel = Math.min(currentLevel + levelsToAdd, maxLevel);
        return setEnchant(item, enchantId, newLevel);
    }

    /**
     * Remove an enchant from a pickaxe.
     */
    public ItemStack removeEnchant(ItemStack item, String enchantId) {
        return setEnchant(item, enchantId, 0);
    }

    /**
     * Clear all custom enchants from a pickaxe.
     */
    public ItemStack clearEnchants(ItemStack item) {
        if (!isPickaxe(item)) {
            return item;
        }
        return saveEnchants(item, new HashMap<>());
    }

    /**
     * Save enchants to PDC and update lore.
     */
    private ItemStack saveEnchants(ItemStack item, Map<String, Integer> enchants) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Save to PDC
        if (enchants.isEmpty()) {
            pdc.remove(enchantsKey);
        } else {
            String json = gson.toJson(enchants);
            pdc.set(enchantsKey, PersistentDataType.STRING, json);
        }

        // Update lore
        loreRenderer.updateLore(meta, enchants);

        item.setItemMeta(meta);

        // Invalidate cache
        enchantCache.remove(System.identityHashCode(item));

        return item;
    }

    /**
     * Set metadata on a pickaxe (for tracking source, roll time, etc.).
     */
    public ItemStack setMeta(ItemStack item, Map<String, Object> metaData) {
        if (!isPickaxe(item)) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String json = gson.toJson(metaData);
        pdc.set(metaKey, PersistentDataType.STRING, json);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get metadata from a pickaxe.
     */
    public Map<String, Object> getMeta(ItemStack item) {
        if (!isPickaxe(item)) {
            return Collections.emptyMap();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Collections.emptyMap();
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String json = pdc.get(metaKey, PersistentDataType.STRING);

        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> result = gson.fromJson(json, type);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Rebuild lore from PDC data (useful after reloading configs).
     */
    public ItemStack rebuildLore(ItemStack item) {
        if (!isPickaxe(item)) {
            return item;
        }

        Map<String, Integer> enchants = getEnchants(item);
        return saveEnchants(item, enchants);
    }

    public LoreRenderer getLoreRenderer() {
        return loreRenderer;
    }

    /**
     * Clear expired cache entries.
     */
    public void cleanupCache() {
        long now = System.currentTimeMillis();
        int ttl = plugin.getConfigManager().getCacheTtlMs();
        enchantCache.entrySet().removeIf(entry ->
                (now - entry.getValue().timestamp) > ttl * 2);
    }

    private static class CachedEnchantData {
        final Map<String, Integer> enchants;
        final long timestamp;

        CachedEnchantData(Map<String, Integer> enchants, long timestamp) {
            this.enchants = enchants;
            this.timestamp = timestamp;
        }
    }
}
