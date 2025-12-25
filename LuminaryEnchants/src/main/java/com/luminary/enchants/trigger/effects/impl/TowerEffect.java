package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Tower: Breaks blocks vertically upward from the broken block.
 */
public class TowerEffect extends AbstractEnchantEffect {

    public TowerEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "tower";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int height = definition.getParamInt("height", 5);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 10),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );
        boolean requiresSneak = definition.getParamBoolean("requiresSneak", false);

        // Check sneak requirement
        if (requiresSneak && !player.isSneaking()) {
            return;
        }

        // Scale height with level (optional)
        int scaledHeight = height + (level / 10);

        // Break blocks upward
        int broken = breakBlocksColumn(player, block, pickaxe, scaledHeight, maxBlocks, DropMode.INVENTORY);

        if (broken > 0) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&f\u2B06 Tower mined " + broken + " blocks upward!"));
        }
    }
}
