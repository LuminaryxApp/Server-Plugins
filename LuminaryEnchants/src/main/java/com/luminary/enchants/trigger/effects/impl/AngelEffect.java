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

/**
 * Angel: Protection/luck - reduces durability loss, increases token drops.
 */
public class AngelEffect extends AbstractEnchantEffect {

    public AngelEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "angel";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double durabilitySaveChance = definition.getParamDouble("durabilitySaveChance", 0.3);
        double tokenBonusPercent = definition.getParamDouble("tokenBonusPercent", 0.1);

        // Scale with level
        double scaledSaveChance = Math.min(durabilitySaveChance + (level * 0.005), 0.9);
        long tokenBonus = (long) (100 * (tokenBonusPercent + (level * 0.005)));

        boolean savedDurability = false;

        // Try to save durability
        if (WeightedRandom.roll(scaledSaveChance)) {
            ItemMeta meta = pickaxe.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                if (currentDamage > 0) {
                    // Restore 1 durability (negate the loss from this break)
                    damageable.setDamage(currentDamage - 1);
                    pickaxe.setItemMeta(meta);
                    savedDurability = true;
                }
            }
        }

        // Award bonus tokens
        if (getTokenEconomy().isAvailable() && tokenBonus > 0) {
            depositTokens(player, tokenBonus, "Angel enchant");
        }

        // Visual effect
        player.getWorld().spawnParticle(Particle.END_ROD,
                block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.02);
        player.playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.5f);

        StringBuilder message = new StringBuilder("&f\u2606 Angel: ");
        if (savedDurability) {
            message.append("Durability saved! ");
        }
        if (tokenBonus > 0) {
            message.append("+").append(tokenBonus).append(" tokens!");
        }

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent(message.toString()));
    }
}
