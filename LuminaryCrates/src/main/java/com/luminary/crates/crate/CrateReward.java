package com.luminary.crates.crate;

import com.luminary.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a single reward in a crate.
 */
public class CrateReward {

    private final String id;
    private final String displayName;
    private final RewardType type;
    private final int weight;
    private final double chance;
    private final CrateTier rarity;

    // Item reward fields
    private final Material material;
    private final int amount;
    private final List<String> lore;
    private final boolean enchantGlow;

    // Command reward fields
    private final List<String> commands;

    // Token reward fields
    private final long tokens;

    // Key reward fields
    private final String keyCrateId;
    private final int keyAmount;

    public CrateReward(ConfigurationSection section) {
        this.id = section.getName();
        this.displayName = section.getString("display", id);
        this.type = RewardType.fromString(section.getString("type", "ITEM"));
        this.weight = section.getInt("weight", 10);
        this.rarity = CrateTier.fromString(section.getString("rarity", "COMMON"));

        int totalWeight = section.getParent() != null ?
                section.getParent().getKeys(false).stream()
                        .mapToInt(k -> section.getParent().getInt(k + ".weight", 10)).sum() : 100;
        this.chance = (double) weight / Math.max(totalWeight, 1);

        // Item fields
        String mat = section.getString("material", "DIAMOND");
        this.material = Material.matchMaterial(mat) != null ?
                Material.matchMaterial(mat) : Material.DIAMOND;
        this.amount = section.getInt("amount", 1);
        this.lore = section.getStringList("lore");
        this.enchantGlow = section.getBoolean("enchant-glow", false);

        // Command fields
        this.commands = section.getStringList("commands");

        // Token fields
        this.tokens = section.getLong("tokens", 0);

        // Key fields
        this.keyCrateId = section.getString("key-crate", null);
        this.keyAmount = section.getInt("key-amount", 1);
    }

    /**
     * Give the reward to a player.
     */
    public void give(Player player) {
        switch (type) {
            case ITEM -> giveItem(player);
            case COMMAND -> executeCommands(player);
            case TOKENS -> giveTokens(player);
            case KEY -> giveKey(player);
        }
    }

    private void giveItem(Player player) {
        ItemStack item = createDisplayItem();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    private void executeCommands(Player player) {
        for (String command : commands) {
            String parsed = command
                    .replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    private void giveTokens(Player player) {
        // Would integrate with LuminaryTokens or Vault
        // For now, run a command
        String command = "eco give " + player.getName() + " " + tokens;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void giveKey(Player player) {
        // Would use KeyManager from this plugin
        // This is handled in the animation completion
    }

    /**
     * Create the display item for GUI/animation.
     */
    public ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(TextUtil.colorize(rarity.getColor() + displayName));

            if (!lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(TextUtil.colorize(line));
                }
                loreComponents.add(Component.empty());
                loreComponents.add(TextUtil.colorize("&7Chance: &e" + String.format("%.2f", chance * 100) + "%"));
                loreComponents.add(TextUtil.colorize("&7Rarity: " + rarity.getColoredName()));
                meta.lore(loreComponents);
            } else {
                List<Component> defaultLore = new ArrayList<>();
                defaultLore.add(TextUtil.colorize("&7Chance: &e" + String.format("%.2f", chance * 100) + "%"));
                defaultLore.add(TextUtil.colorize("&7Rarity: " + rarity.getColoredName()));
                meta.lore(defaultLore);
            }

            if (enchantGlow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RewardType getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public double getChance() {
        return chance;
    }

    public CrateTier getRarity() {
        return rarity;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public long getTokens() {
        return tokens;
    }

    public String getKeyCrateId() {
        return keyCrateId;
    }

    public int getKeyAmount() {
        return keyAmount;
    }

    public enum RewardType {
        ITEM,
        COMMAND,
        TOKENS,
        KEY;

        public static RewardType fromString(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ITEM;
            }
        }
    }
}
