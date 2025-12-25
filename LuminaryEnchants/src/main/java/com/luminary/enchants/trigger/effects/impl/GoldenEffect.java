package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * Golden: Converts drops into "golden" variants worth more tokens.
 */
public class GoldenEffect extends AbstractEnchantEffect {

    public GoldenEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "golden";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double convertChance = definition.getParamDouble("convertChance", 0.2);
        long bonusValue = definition.getParamLong("bonusValue", 500);

        // Scale with level
        double scaledChance = Math.min(convertChance + (level * 0.003), 0.8);
        long scaledBonus = bonusValue + (level * 5);

        if (!WeightedRandom.roll(scaledChance)) {
            return;
        }

        // Award bonus tokens (golden conversion)
        if (getTokenEconomy().isAvailable()) {
            depositTokens(player, scaledBonus, "Golden enchant");
        }

        // Give a gold nugget as visual representation
        ItemStack goldNugget = new ItemStack(Material.GOLD_NUGGET, 1);
        addDropsToInventory(player, Collections.singletonList(goldNugget));

        // Visual effect
        player.getWorld().spawnParticle(Particle.TOTEM,
                block.getLocation().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
        player.playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 2.0f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&6\u2B50 Golden conversion! +" + scaledBonus + " tokens!"));
    }
}
