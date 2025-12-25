package com.luminary.crates.key;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages crate keys - creation, validation, and consumption.
 */
public class KeyManager {

    private final LuminaryCrates plugin;
    private final NamespacedKey crateKeyId;
    private final NamespacedKey keyUniqueId;

    public KeyManager(LuminaryCrates plugin) {
        this.plugin = plugin;
        this.crateKeyId = new NamespacedKey(plugin, "crate_key");
        this.keyUniqueId = new NamespacedKey(plugin, "key_uuid");
    }

    /**
     * Create a key item for a specific crate.
     */
    public ItemStack createKey(Crate crate, int amount) {
        ItemStack key = new ItemStack(crate.getKeyMaterial(), amount);
        ItemMeta meta = key.getItemMeta();

        if (meta != null) {
            // Set display name
            meta.displayName(TextUtil.colorize(crate.getKeyName()));

            // Set lore
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(TextUtil.colorize("&7Use this key on a"));
            lore.add(TextUtil.colorize(crate.getTier().getColor() + crate.getDisplayName() + " &7Crate"));
            lore.add(Component.empty());
            lore.add(TextUtil.colorize("&eRight-click a crate to use!"));
            meta.lore(lore);

            // Store crate ID in PDC
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(crateKeyId, PersistentDataType.STRING, crate.getId());
            pdc.set(keyUniqueId, PersistentDataType.STRING, UUID.randomUUID().toString());

            key.setItemMeta(meta);
        }

        return key;
    }

    /**
     * Check if an item is a valid crate key.
     */
    public boolean isKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(crateKeyId, PersistentDataType.STRING);
    }

    /**
     * Get the crate ID this key is for.
     */
    public String getKeyCrateId(ItemStack item) {
        if (!isKey(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(crateKeyId, PersistentDataType.STRING);
    }

    /**
     * Check if a key matches a specific crate.
     */
    public boolean isKeyForCrate(ItemStack item, Crate crate) {
        String keyCrateId = getKeyCrateId(item);
        return keyCrateId != null && keyCrateId.equals(crate.getId());
    }

    /**
     * Give keys to a player.
     */
    public void giveKeys(Player player, Crate crate, int amount) {
        ItemStack key = createKey(crate, amount);

        // Try to add to inventory
        var overflow = player.getInventory().addItem(key);

        // Drop any overflow
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    /**
     * Remove one key from a player's hand.
     */
    public void consumeKey(Player player, ItemStack keyItem) {
        if (keyItem.getAmount() > 1) {
            keyItem.setAmount(keyItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    /**
     * Count how many keys of a specific crate a player has.
     */
    public int countKeys(Player player, Crate crate) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKeyForCrate(item, crate)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Count how many keys of a specific crate a player has (by ID).
     */
    public int countKeys(Player player, String crateId) {
        Crate crate = plugin.getCrateRegistry().getCrate(crateId);
        if (crate == null) return 0;
        return countKeys(player, crate);
    }

    /**
     * Find a key for a specific crate in a player's inventory.
     * Returns the first matching key ItemStack, or null if not found.
     */
    public ItemStack findKeyInInventory(Player player, Crate crate) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKeyForCrate(item, crate)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Consume one key from any slot in the player's inventory.
     */
    public void consumeKeyFromInventory(Player player, Crate crate) {
        ItemStack keyItem = findKeyInInventory(player, crate);
        if (keyItem != null) {
            if (keyItem.getAmount() > 1) {
                keyItem.setAmount(keyItem.getAmount() - 1);
            } else {
                player.getInventory().remove(keyItem);
            }
        }
    }
}
