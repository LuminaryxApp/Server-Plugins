package com.luminary.crates.listener;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.animation.AnimationManager;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.crate.CrateManager;
import com.luminary.crates.crate.CrateReward;
import com.luminary.crates.key.KeyManager;
import com.luminary.crates.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles crate interaction events.
 */
public class CrateListener implements Listener {

    private final LuminaryCrates plugin;
    private final KeyManager keyManager;
    private final CrateManager crateManager;
    private final AnimationManager animationManager;

    public CrateListener(LuminaryCrates plugin) {
        this.plugin = plugin;
        this.keyManager = plugin.getKeyManager();
        this.crateManager = plugin.getCrateManager();
        this.animationManager = plugin.getAnimationManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrateInteract(PlayerInteractEvent event) {
        // Only handle main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Check if this is a crate location
        CrateManager.CrateLocation crateLocation = crateManager.getCrateAt(block.getLocation());
        if (crateLocation == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        Crate crate = crateLocation.getCrate();

        // Check if player is already in animation
        if (animationManager.isAnimating(player)) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.already-opening")));
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            // Preview crate contents
            handlePreview(player, crate);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            // Try to open crate
            handleOpen(player, crate, crateLocation.getLocation());
        }
    }

    /**
     * Handle crate preview (left-click).
     */
    private void handlePreview(Player player, Crate crate) {
        plugin.getPreviewManager().openPreview(player, crate);
    }

    /**
     * Handle crate opening (right-click).
     */
    private void handleOpen(Player player, Crate crate, Location location) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Check if holding a valid key
        if (!keyManager.isKeyForCrate(heldItem, crate)) {
            // No key or wrong key
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.no-key")
                            .replace("{crate}", crate.getDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check permission
        String permission = "luminarycrates.open." + crate.getId();
        if (!player.hasPermission(permission) && !player.hasPermission("luminarycrates.open.*")) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.no-permission")));
            return;
        }

        // Consume the key
        keyManager.consumeKey(player, heldItem);

        // Roll for reward
        CrateReward reward = crate.rollReward();

        // Spawn particles at crate location
        spawnOpenParticles(location);

        // Play open sound
        player.playSound(location, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

        // Start animation
        animationManager.startAnimation(player, crate, reward);
    }

    /**
     * Spawn particles when opening a crate.
     */
    private void spawnOpenParticles(Location location) {
        Location center = location.clone().add(0.5, 1.0, 0.5);
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, center, 30, 0.3, 0.3, 0.3, 0.1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, center, 50, 0.5, 0.5, 0.5, 0.5);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrateBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if this is a crate location
        if (crateManager.isCrateLocation(block.getLocation())) {
            Player player = event.getPlayer();

            // Only allow breaking with permission
            if (!player.hasPermission("luminarycrates.admin")) {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("crate.cannot-break")));
                return;
            }

            // Remove the crate location
            crateManager.removeCrateLocation(block.getLocation());
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("admin.crate-removed")));
        }
    }
}
