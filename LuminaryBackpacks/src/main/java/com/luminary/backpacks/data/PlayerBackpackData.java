package com.luminary.backpacks.data;

import com.luminary.backpacks.LuminaryBackpacks;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerBackpackData {

    private final UUID uuid;
    private int tier;
    private ItemStack[] contents;
    private boolean autoPickup;
    private boolean autoSell;
    private boolean dirty;

    public PlayerBackpackData(UUID uuid) {
        this.uuid = uuid;
        this.tier = LuminaryBackpacks.getInstance().getConfigManager().getDefaultTier();
        this.contents = new ItemStack[getSize()];
        this.autoPickup = true;
        this.autoSell = false;
        this.dirty = false;
    }

    public PlayerBackpackData(UUID uuid, ConfigurationSection section) {
        this.uuid = uuid;
        this.tier = section.getInt("tier", LuminaryBackpacks.getInstance().getConfigManager().getDefaultTier());
        this.autoPickup = section.getBoolean("auto-pickup", true);
        this.autoSell = section.getBoolean("auto-sell", false);

        int size = getSize();
        this.contents = new ItemStack[size];

        ConfigurationSection itemsSection = section.getConfigurationSection("contents");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot >= 0 && slot < size) {
                        contents[slot] = itemsSection.getItemStack(key);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        this.dirty = false;
    }

    public void save(ConfigurationSection section) {
        section.set("tier", tier);
        section.set("auto-pickup", autoPickup);
        section.set("auto-sell", autoSell);

        // Clear old contents
        section.set("contents", null);

        // Save contents
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                section.set("contents." + i, contents[i]);
            }
        }

        dirty = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        if (this.tier != tier) {
            this.tier = tier;
            resizeContents();
            this.dirty = true;
        }
    }

    public int getSize() {
        return LuminaryBackpacks.getInstance().getConfigManager().getTierSize(tier);
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        int size = getSize();
        this.contents = new ItemStack[size];
        for (int i = 0; i < Math.min(contents.length, size); i++) {
            this.contents[i] = contents[i];
        }
        this.dirty = true;
    }

    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < contents.length) {
            return contents[slot];
        }
        return null;
    }

    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < contents.length) {
            contents[slot] = item;
            this.dirty = true;
        }
    }

    public boolean isAutoPickupEnabled() {
        return autoPickup;
    }

    public void setAutoPickup(boolean autoPickup) {
        if (this.autoPickup != autoPickup) {
            this.autoPickup = autoPickup;
            this.dirty = true;
        }
    }

    public boolean isAutoSellEnabled() {
        return autoSell;
    }

    public void setAutoSell(boolean autoSell) {
        if (this.autoSell != autoSell) {
            this.autoSell = autoSell;
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Resize contents array when tier changes.
     */
    private void resizeContents() {
        int newSize = getSize();
        ItemStack[] newContents = new ItemStack[newSize];

        for (int i = 0; i < Math.min(contents.length, newSize); i++) {
            newContents[i] = contents[i];
        }

        this.contents = newContents;
    }

    /**
     * Check if backpack has any empty slots.
     */
    public boolean hasSpace() {
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if backpack has space for a specific item.
     */
    public boolean hasSpaceFor(ItemStack item) {
        int remaining = item.getAmount();

        for (ItemStack content : contents) {
            if (content == null || content.getType() == Material.AIR) {
                return true;
            }
            if (content.isSimilar(item)) {
                int canAdd = content.getMaxStackSize() - content.getAmount();
                remaining -= canAdd;
                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Add an item to the backpack.
     * Returns the amount that couldn't be added.
     */
    public int addItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }

        int remaining = item.getAmount();

        // First, try to stack with existing items
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack content = contents[i];
            if (content != null && content.isSimilar(item)) {
                int canAdd = content.getMaxStackSize() - content.getAmount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, remaining);
                    content.setAmount(content.getAmount() + toAdd);
                    remaining -= toAdd;
                    this.dirty = true;
                }
            }
        }

        // Then, try to find empty slots
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            if (contents[i] == null || contents[i].getType() == Material.AIR) {
                int toAdd = Math.min(item.getMaxStackSize(), remaining);
                ItemStack newItem = item.clone();
                newItem.setAmount(toAdd);
                contents[i] = newItem;
                remaining -= toAdd;
                this.dirty = true;
            }
        }

        return remaining;
    }

    /**
     * Clear all contents.
     */
    public void clear() {
        contents = new ItemStack[getSize()];
        this.dirty = true;
    }

    /**
     * Get total item count.
     */
    public int getTotalItems() {
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Get used slot count.
     */
    public int getUsedSlots() {
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                count++;
            }
        }
        return count;
    }
}
