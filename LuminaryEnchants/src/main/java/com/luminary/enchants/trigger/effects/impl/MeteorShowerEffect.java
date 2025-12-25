package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Meteor Shower: Multiple small bursts around the player over 1-2 seconds.
 */
public class MeteorShowerEffect extends AbstractEnchantEffect {

    public MeteorShowerEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "meteor_shower";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int meteors = definition.getParamInt("meteors", 5);
        int radius = definition.getParamInt("radius", 3);
        int maxBlocksPerMeteor = Math.min(
                definition.getParamInt("maxBlocksPerMeteor", 10),
                plugin.getConfigManager().getMaxBlocksPerProc() / meteors
        );
        int durationTicks = definition.getParamInt("durationTicks", 40);

        // Scale with level
        int scaledMeteors = meteors + (level / 20);

        // Calculate delay between meteors
        int delayBetween = durationTicks / scaledMeteors;

        Location playerLoc = player.getLocation();

        // Schedule meteor strikes
        for (int i = 0; i < scaledMeteors; i++) {
            final int meteorIndex = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if player is still online
                if (!player.isOnline()) return;

                // Random location within radius of the original block
                int offsetX = WeightedRandom.randomInt(-radius, radius);
                int offsetZ = WeightedRandom.randomInt(-radius, radius);
                Location strikeLocation = block.getLocation().add(offsetX, 0, offsetZ);

                // Find the highest block at this location
                Block targetBlock = strikeLocation.getWorld().getHighestBlockAt(strikeLocation);
                if (targetBlock.getY() < block.getY() - 5) {
                    targetBlock = strikeLocation.getBlock();
                }

                // Meteor particle trail from above
                Location meteorStart = targetBlock.getLocation().add(0.5, 10, 0.5);
                for (int y = 10; y > 0; y--) {
                    Location trailLoc = meteorStart.clone().add(0, -y, 0);
                    player.getWorld().spawnParticle(Particle.FLAME, trailLoc, 3, 0.1, 0.1, 0.1, 0.01);
                    player.getWorld().spawnParticle(Particle.LAVA, trailLoc, 1, 0.1, 0.1, 0.1, 0);
                }

                // Impact effect
                player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,
                        targetBlock.getLocation().add(0.5, 1, 0.5), 1, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.FLAME,
                        targetBlock.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                player.playSound(targetBlock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.3f);

                // Break blocks at impact
                breakBlocksAOE(player, targetBlock, pickaxe, 1, maxBlocksPerMeteor, DropMode.INVENTORY);

            }, (long) i * delayBetween);
        }

        // Initial message
        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&c\u2604 Meteor Shower! " + scaledMeteors + " incoming!"));
    }
}
