package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Storm Cloud: AOE break with mid-size radius, more frequent than Thor.
 */
public class StormCloudEffect extends AbstractEnchantEffect {

    public StormCloudEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "storm_cloud";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int radius = definition.getParamInt("radius", 1);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 27),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );

        // Visual effect - storm particles
        player.getWorld().spawnParticle(Particle.CLOUD,
                block.getLocation().add(0.5, 1.5, 0.5), 30, radius, 0.5, radius, 0.02);
        player.getWorld().spawnParticle(Particle.FALLING_WATER,
                block.getLocation().add(0.5, 1, 0.5), 20, radius, 0.3, radius, 0);
        player.playSound(block.getLocation(), Sound.WEATHER_RAIN, 0.5f, 0.8f);

        // Break blocks in AOE
        int broken = breakBlocksAOE(player, block, pickaxe, radius, maxBlocks, DropMode.INVENTORY);

        if (broken > 0) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&7\u2601 Storm Cloud washed away " + broken + " blocks!"));
        }
    }
}
