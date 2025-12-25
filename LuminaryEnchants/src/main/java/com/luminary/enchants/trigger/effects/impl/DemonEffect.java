package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

/**
 * Demon: High-risk high-reward - chance for huge burst drops, but small chance to burn durability.
 */
public class DemonEffect extends AbstractEnchantEffect {

    public DemonEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "demon";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double dropMultiplier = definition.getParamDouble("dropMultiplier", 3.0);
        int durabilityDamage = definition.getParamInt("durabilityDamageOnBackfire", 50);
        double backfireChance = definition.getParamDouble("backfireChance", 0.1);

        // Scale multiplier with level
        double scaledMultiplier = dropMultiplier + (level * 0.02);

        // Check for backfire first
        if (WeightedRandom.roll(backfireChance)) {
            // Backfire! Damage the pickaxe
            ItemMeta meta = pickaxe.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int maxDurability = pickaxe.getType().getMaxDurability();

                int newDamage = currentDamage + durabilityDamage;
                if (newDamage >= maxDurability) {
                    // Would break the pickaxe - set to nearly broken
                    newDamage = maxDurability - 1;
                }
                damageable.setDamage(newDamage);
                pickaxe.setItemMeta(meta);
            }

            player.getWorld().spawnParticle(Particle.LAVA,
                    player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 0.8f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&4\uD83D\uDD25 Demon backfired! Pickaxe damaged!"));
            return;
        }

        // Success! Multiply drops
        Collection<ItemStack> drops = block.getDrops(pickaxe, player);

        int extraDrops = (int) Math.ceil(scaledMultiplier) - 1;
        for (int i = 0; i < extraDrops; i++) {
            addDropsToInventory(player, drops);
        }

        // Visual effect
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                block.getLocation().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.05);
        player.playSound(block.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.2f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&4\uD83D\uDC7F Demon granted " +
                        String.format("%.0f", scaledMultiplier) + "x drops!"));
    }
}
