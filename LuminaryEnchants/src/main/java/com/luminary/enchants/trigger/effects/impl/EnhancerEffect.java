package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Enhancer: Temporarily increases proc chance of other enchants.
 */
public class EnhancerEffect extends AbstractEnchantEffect {

    public EnhancerEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "enhancer";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int durationTicks = definition.getParamInt("durationTicks", 100);
        double procChanceBonus = definition.getParamDouble("procChanceBonus", 0.1);

        // Scale bonus with level
        double totalBonus = procChanceBonus * level;

        // Apply the buff (refreshes duration if already active)
        plugin.getProcEngine().applyEnhancerBuff(
                player.getUniqueId(),
                durationTicks,
                totalBonus
        );

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&d+Enhancer active! +" +
                        String.format("%.0f", totalBonus * 100) + "% proc chance"));
    }
}
