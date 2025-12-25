package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Second Hand: Chance to re-roll one random eligible enchant proc.
 * Never chains more than once per break.
 */
public class SecondHandEffect extends AbstractEnchantEffect {

    public SecondHandEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "second_hand";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        // Don't trigger on secondary rolls (prevent infinite recursion)
        if (context == ProcEngine.ProcContext.SECONDARY_ROLL) {
            return;
        }

        double extraRollChanceBase = definition.getParamDouble("extraRollChanceBase", 0.5);
        double extraRollChancePerLevel = definition.getParamDouble("extraRollChancePerLevel", 0.01);

        double rollChance = extraRollChanceBase + (extraRollChancePerLevel * level);
        rollChance = Math.min(rollChance, 1.0);

        if (!WeightedRandom.roll(rollChance)) {
            return;
        }

        // Get all enchants on the pickaxe
        Map<String, Integer> enchants = plugin.getPickaxeDataManager().getEnchants(pickaxe);

        // Try secondary roll
        boolean procced = plugin.getProcEngine().trySecondaryRoll(player, pickaxe, block, enchants);

        if (procced) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&d\u270B Second Hand triggered a bonus proc!"));
        }
    }
}
