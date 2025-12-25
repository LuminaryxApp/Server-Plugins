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
 * Flash: Short burst that instantly breaks a small cluster with very low cap but high frequency.
 */
public class FlashEffect extends AbstractEnchantEffect {

    public FlashEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "flash";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int radius = definition.getParamInt("radius", 1);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 8),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );

        // Flash is intentionally small and fast
        int broken = breakBlocksAOE(player, block, pickaxe, radius, maxBlocks, DropMode.INVENTORY);

        if (broken > 0) {
            // Quick flash effect
            player.getWorld().spawnParticle(Particle.FLASH,
                    block.getLocation().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,
                    block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
            player.playSound(block.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3f, 2.0f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&e\u26A1 Flash! " + broken + " blocks!"));
        }
    }
}
