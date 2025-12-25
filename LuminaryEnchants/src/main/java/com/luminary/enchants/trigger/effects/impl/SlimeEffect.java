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
 * Slime: Chance to drop extra slime-related items for crafting boosts.
 */
public class SlimeEffect extends AbstractEnchantEffect {

    public SlimeEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "slime";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        String extraDropItemStr = definition.getParamString("extraDropItem", "SLIME_BALL");
        double extraDropChance = definition.getParamDouble("extraDropChance", 0.5);

        // Scale chance with level
        double scaledChance = Math.min(extraDropChance + (level * 0.005), 1.0);

        if (!WeightedRandom.roll(scaledChance)) {
            return;
        }

        // Get the item to drop
        Material dropMaterial;
        try {
            dropMaterial = Material.valueOf(extraDropItemStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            dropMaterial = Material.SLIME_BALL;
        }

        // Calculate quantity based on level
        int quantity = 1 + (level / 25);

        ItemStack drop = new ItemStack(dropMaterial, quantity);
        addDropsToInventory(player, Collections.singletonList(drop));

        // Visual effect
        player.getWorld().spawnParticle(Particle.SLIME,
                block.getLocation().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.05);
        player.playSound(block.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0.5f, 1.2f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&a\u2B24 Slime dropped " + quantity + "x " +
                        dropMaterial.name().toLowerCase().replace("_", " ") + "!"));
    }
}
