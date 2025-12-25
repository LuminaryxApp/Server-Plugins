package com.luminary.enchants.trigger;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for pickaxe-related events and triggers enchant processing.
 */
public class PickaxeListener implements Listener {

    private final LuminaryEnchants plugin;

    public PickaxeListener(LuminaryEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!plugin.getPickaxeDataManager().isPickaxe(mainHand)) {
            return;
        }

        // Process enchants
        plugin.getProcEngine().processBlockBreak(
                player,
                mainHand,
                event.getBlock(),
                ProcEngine.ProcContext.BLOCK_BREAK
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Only main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        // Check if holding pickaxe
        if (!plugin.getPickaxeDataManager().isPickaxe(mainHand)) {
            return;
        }

        // Check permission
        if (!player.hasPermission("luminaryenchants.use")) {
            return;
        }

        // If sneaking, handle interact trigger for enchants like Laser
        if (player.isSneaking()) {
            plugin.getProcEngine().processInteract(player, mainHand, event.getClickedBlock());
            return;
        }

        // Open enchant menu on regular right-click
        event.setCancelled(true);
        plugin.getMenuManager().openMainMenu(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup player data
        plugin.getProcEngine().handlePlayerQuit(event.getPlayer().getUniqueId());
        plugin.getMenuManager().handlePlayerQuit(event.getPlayer().getUniqueId());
    }
}
