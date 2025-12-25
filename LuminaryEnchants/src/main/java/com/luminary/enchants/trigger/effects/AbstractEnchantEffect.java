package com.luminary.enchants.trigger.effects;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.api.MineRegionProvider;
import com.luminary.enchants.api.TokenEconomy;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Base class for enchant effects with common utilities.
 */
public abstract class AbstractEnchantEffect implements EnchantEffect {

    protected final LuminaryEnchants plugin;

    protected AbstractEnchantEffect(LuminaryEnchants plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the token economy instance.
     */
    protected TokenEconomy getTokenEconomy() {
        return plugin.getHookManager().getTokenEconomy();
    }

    /**
     * Get the beacon provider instance.
     */
    protected BeaconEffectProvider getBeaconProvider() {
        return plugin.getHookManager().getBeaconProvider();
    }

    /**
     * Get the mine region provider instance.
     */
    protected MineRegionProvider getMineRegionProvider() {
        return plugin.getHookManager().getMineRegionProvider();
    }

    /**
     * Deposit tokens to a player with beacon multiplier.
     */
    protected void depositTokens(Player player, long baseAmount, String reason) {
        if (!getTokenEconomy().isAvailable()) return;

        double multiplier = getBeaconProvider().multiplier(
                player.getUniqueId(), BeaconEffectProvider.TOKEN_GAIN);
        long finalAmount = (long) (baseAmount * multiplier);

        getTokenEconomy().deposit(player.getUniqueId(), finalAmount, reason);
    }

    /**
     * Break a block safely with drops going to player's inventory or ground.
     */
    protected void breakBlock(Player player, Block block, ItemStack pickaxe,
                              DropMode dropMode, boolean applyDurability) {
        if (block.getType().isAir()) return;

        Collection<ItemStack> drops = block.getDrops(pickaxe, player);

        // Break the block
        block.setType(Material.AIR);

        // Handle drops
        switch (dropMode) {
            case INVENTORY -> addDropsToInventory(player, drops);
            case NORMAL -> dropItems(block.getLocation(), drops);
            case AUTOSELL_HOOK -> {
                // Future: hook into autosell plugin
                addDropsToInventory(player, drops);
            }
        }

        // Apply durability damage
        if (applyDurability && pickaxe.getType().getMaxDurability() > 0) {
            // This is handled by the game normally when player breaks blocks
            // For extra breaks, we may want to apply minimal durability
        }
    }

    /**
     * Break multiple blocks in an AOE pattern.
     */
    protected int breakBlocksAOE(Player player, Block center, ItemStack pickaxe,
                                  int radius, int maxBlocks, DropMode dropMode) {
        List<Block> blocks = getBlocksInRadius(center, radius, maxBlocks);
        int broken = 0;

        for (Block block : blocks) {
            if (!block.getType().isAir() && canBreak(player, block)) {
                breakBlock(player, block, pickaxe, dropMode, false);
                broken++;
            }
        }

        return broken;
    }

    /**
     * Break blocks in a line (for Sonic, Laser).
     */
    protected int breakBlocksLine(Player player, Block start, ItemStack pickaxe,
                                   Vector direction, int range, int maxBlocks,
                                   DropMode dropMode, Set<Material> skipMaterials) {
        int broken = 0;
        Location loc = start.getLocation();

        for (int i = 0; i < range && broken < maxBlocks; i++) {
            loc = loc.add(direction);
            Block block = loc.getBlock();

            if (block.getType().isAir()) continue;

            if (skipMaterials != null && skipMaterials.contains(block.getType())) {
                // Ghostrider: skip protected blocks
                continue;
            }

            if (!canBreak(player, block)) break;

            breakBlock(player, block, pickaxe, dropMode, false);
            broken++;
        }

        return broken;
    }

    /**
     * Break blocks in a vertical column (for Tower).
     */
    protected int breakBlocksColumn(Player player, Block start, ItemStack pickaxe,
                                     int height, int maxBlocks, DropMode dropMode) {
        int broken = 0;
        Block current = start;

        for (int y = 1; y <= height && broken < maxBlocks; y++) {
            Block above = current.getRelative(0, y, 0);
            if (above.getType().isAir()) continue;
            if (!canBreak(player, above)) break;

            breakBlock(player, above, pickaxe, dropMode, false);
            broken++;
        }

        return broken;
    }

    /**
     * Get blocks within a radius.
     */
    protected List<Block> getBlocksInRadius(Block center, int radius, int maxBlocks) {
        List<Block> blocks = new ArrayList<>();

        for (int x = -radius; x <= radius && blocks.size() < maxBlocks; x++) {
            for (int y = -radius; y <= radius && blocks.size() < maxBlocks; y++) {
                for (int z = -radius; z <= radius && blocks.size() < maxBlocks; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block block = center.getRelative(x, y, z);
                    if (!block.getType().isAir()) {
                        blocks.add(block);
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * Check if player can break a block (with mine region protection).
     */
    protected boolean canBreak(Player player, Block block) {
        // Basic checks
        if (block.getType().isAir()) return false;
        if (block.getType().getHardness() < 0) return false; // Unbreakable

        // Check mine region protection
        MineRegionProvider mineProvider = getMineRegionProvider();
        if (mineProvider != null && mineProvider.isAvailable()) {
            // Only allow breaking within mine interior regions
            if (!mineProvider.canPlayerBreakAt(player, block.getLocation())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add items to player inventory, dropping overflow.
     */
    protected void addDropsToInventory(Player player, Collection<ItemStack> drops) {
        for (ItemStack drop : drops) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(drop);
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    /**
     * Drop items at a location.
     */
    protected void dropItems(Location location, Collection<ItemStack> drops) {
        for (ItemStack drop : drops) {
            location.getWorld().dropItemNaturally(location, drop);
        }
    }

    /**
     * Get player's facing direction as a block vector.
     */
    protected Vector getFacingDirection(Player player) {
        return player.getLocation().getDirection().normalize();
    }

    /**
     * Get player's facing direction snapped to axis.
     */
    protected Vector getFacingAxis(Player player) {
        Vector dir = player.getLocation().getDirection();
        double absX = Math.abs(dir.getX());
        double absY = Math.abs(dir.getY());
        double absZ = Math.abs(dir.getZ());

        if (absX > absY && absX > absZ) {
            return new Vector(dir.getX() > 0 ? 1 : -1, 0, 0);
        } else if (absY > absZ) {
            return new Vector(0, dir.getY() > 0 ? 1 : -1, 0);
        } else {
            return new Vector(0, 0, dir.getZ() > 0 ? 1 : -1);
        }
    }

    public enum DropMode {
        NORMAL,
        INVENTORY,
        AUTOSELL_HOOK
    }
}
