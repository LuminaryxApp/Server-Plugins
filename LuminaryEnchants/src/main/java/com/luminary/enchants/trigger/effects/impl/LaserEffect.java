package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Laser: Mining beam mode - breaks blocks in a straight line.
 * Toggle with sneak-right-click.
 */
public class LaserEffect extends AbstractEnchantEffect {

    // Track laser mode state per player
    private final Map<UUID, Boolean> laserEnabled = new ConcurrentHashMap<>();

    public LaserEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "laser";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int range = definition.getParamInt("range", 10);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 20),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );
        boolean toggleMode = definition.getParamBoolean("toggleMode", true);

        UUID playerId = player.getUniqueId();

        // Handle toggle on interact (sneak + right-click)
        if (context == ProcEngine.ProcContext.INTERACT && player.isSneaking()) {
            if (toggleMode) {
                boolean current = laserEnabled.getOrDefault(playerId, false);
                laserEnabled.put(playerId, !current);

                if (!current) {
                    player.sendMessage(plugin.getPickaxeDataManager().getLoreRenderer()
                            .createDisplayComponent("&c\u2604 Laser mode &aENABLED"));
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
                } else {
                    player.sendMessage(plugin.getPickaxeDataManager().getLoreRenderer()
                            .createDisplayComponent("&c\u2604 Laser mode &cDISABLED"));
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.5f);
                }
                return;
            }
        }

        // Only fire laser beam on block break if enabled
        if (context == ProcEngine.ProcContext.BLOCK_BREAK) {
            if (toggleMode && !laserEnabled.getOrDefault(playerId, false)) {
                return;
            }

            // Scale range with level
            int scaledRange = range + (level / 10);

            // Get facing direction
            Vector direction = getFacingAxis(player);

            // Break blocks in line
            int broken = breakBlocksLine(player, block, pickaxe, direction, scaledRange, maxBlocks,
                    DropMode.INVENTORY, null);

            if (broken > 0) {
                // Laser beam particle effect
                Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.0f);
                for (int i = 1; i <= scaledRange; i++) {
                    player.getWorld().spawnParticle(Particle.REDSTONE,
                            block.getLocation().add(0.5, 0.5, 0.5)
                                    .add(direction.clone().multiply(i)),
                            3, 0.1, 0.1, 0.1, 0, dust);
                }
                player.playSound(block.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 0.5f, 2.0f);

                player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                        .createDisplayComponent("&c\u2604 Laser cut through " + broken + " blocks!"));
            }
        }
    }

    public boolean isLaserEnabled(UUID playerId) {
        return laserEnabled.getOrDefault(playerId, false);
    }

    public void setLaserEnabled(UUID playerId, boolean enabled) {
        laserEnabled.put(playerId, enabled);
    }
}
