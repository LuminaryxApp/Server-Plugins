package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Thor: Lightning-style mining burst - breaks an AOE around the block.
 */
public class ThorEffect extends AbstractEnchantEffect {

    public ThorEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "thor";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int radius = definition.getParamInt("radius", 2);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 64),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );
        String dropModeStr = definition.getParamString("dropMode", "INVENTORY");
        DropMode dropMode = DropMode.valueOf(dropModeStr.toUpperCase());

        // Visual effect - lightning strike
        Location loc = block.getLocation().add(0.5, 1, 0.5);
        player.getWorld().strikeLightningEffect(loc);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 50, 1, 1, 1, 0.1);
        player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);

        // Break blocks in AOE
        int broken = breakBlocksAOE(player, block, pickaxe, radius, maxBlocks, dropMode);

        if (broken > 0) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&e\u26A1 Thor struck " + broken + " blocks!"));
        }
    }
}
