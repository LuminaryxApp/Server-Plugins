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

import java.util.List;
import java.util.UUID;

/**
 * Pause: Briefly reduces cooldowns for a subset of enchants.
 */
public class PauseEffect extends AbstractEnchantEffect {

    public PauseEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "pause";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        long cooldownReductionMs = definition.getParamLong("cooldownReductionMs", 500);
        List<String> affectedEnchantIds = definition.getParamStringList("affectedEnchantIds");

        // Scale with level
        long scaledReduction = cooldownReductionMs + (level * 10);

        UUID playerId = player.getUniqueId();

        // If no specific enchants listed, affect all enchants on the pickaxe
        if (affectedEnchantIds.isEmpty()) {
            affectedEnchantIds = plugin.getPickaxeDataManager().getEnchants(pickaxe).keySet()
                    .stream().toList();
        }

        int reducedCount = 0;
        for (String enchantId : affectedEnchantIds) {
            if (!enchantId.equals("pause")) { // Don't affect itself
                plugin.getProcEngine().getCooldownManager()
                        .reduceCooldown(playerId, enchantId, scaledReduction);
                reducedCount++;
            }
        }

        if (reducedCount > 0) {
            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL,
                    player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.5f, 1.5f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&d\u23F8 Pause reduced " + reducedCount +
                            " cooldowns by " + (scaledReduction / 1000.0) + "s!"));
        }
    }
}
