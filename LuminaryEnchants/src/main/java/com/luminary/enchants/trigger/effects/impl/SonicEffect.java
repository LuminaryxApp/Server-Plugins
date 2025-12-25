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
import org.bukkit.util.Vector;

/**
 * Sonic: Shockwave that breaks blocks in a line/cone in the facing direction.
 */
public class SonicEffect extends AbstractEnchantEffect {

    public SonicEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "sonic";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int range = definition.getParamInt("range", 5);
        int width = definition.getParamInt("width", 1);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 15),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );

        // Scale with level
        int scaledRange = range + (level / 20);

        // Get facing direction
        Vector direction = getFacingAxis(player);

        // Break blocks in line
        int broken = breakBlocksLine(player, block, pickaxe, direction, scaledRange, maxBlocks,
                DropMode.INVENTORY, null);

        // Visual effect - sonic boom particles
        player.getWorld().spawnParticle(Particle.SONIC_BOOM,
                block.getLocation().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
        player.playSound(block.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.3f, 1.5f);

        if (broken > 0) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&9\u27A1 Sonic wave blasted " + broken + " blocks!"));
        }
    }
}
