package com.luminary.backpacks.listener;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class MiningListener implements Listener {

    private final LuminaryBackpacks plugin;

    public MiningListener(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Skip if in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if auto-pickup is enabled globally
        if (!plugin.getConfigManager().isAutoPickupEnabled()) {
            return;
        }

        // Check if player has permission
        if (!player.hasPermission("luminarybackpacks.autopickup")) {
            return;
        }

        // Check if player has auto-pickup enabled
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.isAutoPickupEnabled()) {
            return;
        }

        // Get block drops
        Collection<ItemStack> drops = event.getBlock().getDrops(player.getInventory().getItemInMainHand());
        if (drops.isEmpty()) {
            return;
        }

        // Check sellable-only setting
        boolean sellableOnly = plugin.getConfigManager().isSellableOnly();

        // Try to add drops to backpack
        boolean addedAny = false;
        for (ItemStack drop : drops) {
            if (drop == null || drop.getType() == Material.AIR) {
                continue;
            }

            // If sellable-only, skip items that can't be sold
            if (sellableOnly && !plugin.getConfigManager().isSellable(drop.getType())) {
                continue;
            }

            // Try to add to backpack
            int remaining = plugin.getBackpackManager().addItem(player, drop);

            if (remaining < drop.getAmount()) {
                addedAny = true;
            }

            // If some items couldn't be added, drop them naturally
            if (remaining > 0) {
                ItemStack leftover = drop.clone();
                leftover.setAmount(remaining);
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(), leftover);
            }
        }

        // If we added anything to backpack, don't drop items normally
        if (addedAny) {
            event.setDropItems(false);
        }

        // Notify if backpack is full
        if (!plugin.getBackpackManager().hasSpace(player)) {
            // Only notify occasionally to avoid spam
            if (System.currentTimeMillis() % 5000 < 100) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(plugin.getConfigManager().getMessage("backpack-full")));
            }
        }
    }
}
