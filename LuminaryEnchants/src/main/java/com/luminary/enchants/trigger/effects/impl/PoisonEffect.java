package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Poison: Corruption effect that increases ore yields but reduces stone yields.
 */
public class PoisonEffect extends AbstractEnchantEffect {

    // Track active poison effects per player
    private final Map<UUID, PoisonBuff> activeBuffs = new ConcurrentHashMap<>();

    public PoisonEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "poison";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double oreBonusMultiplier = definition.getParamDouble("oreBonusMultiplier", 1.5);
        double stonePenaltyMultiplier = definition.getParamDouble("stonePenaltyMultiplier", 0.5);
        int durationTicks = definition.getParamInt("durationTicks", 200);

        // Scale with level
        double scaledOreBonus = oreBonusMultiplier + (level * 0.01);
        int scaledDuration = durationTicks + (level * 2);

        UUID playerId = player.getUniqueId();
        long expiresAt = System.currentTimeMillis() + (scaledDuration * 50L);

        activeBuffs.put(playerId, new PoisonBuff(expiresAt, scaledOreBonus, stonePenaltyMultiplier));

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_WITCH,
                player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 0.5f, 0.8f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&5\u2620 Poison active! Ores +" +
                        String.format("%.0f", (scaledOreBonus - 1) * 100) + "%, Stone -" +
                        String.format("%.0f", (1 - stonePenaltyMultiplier) * 100) + "%"));
    }

    /**
     * Get ore bonus multiplier for a player (called by drop handlers).
     */
    public double getOreMultiplier(UUID playerId) {
        PoisonBuff buff = activeBuffs.get(playerId);
        if (buff != null && buff.isActive()) {
            return buff.oreBonus;
        }
        return 1.0;
    }

    /**
     * Get stone penalty multiplier for a player.
     */
    public double getStoneMultiplier(UUID playerId) {
        PoisonBuff buff = activeBuffs.get(playerId);
        if (buff != null && buff.isActive()) {
            return buff.stonePenalty;
        }
        return 1.0;
    }

    public boolean hasActiveBuff(UUID playerId) {
        PoisonBuff buff = activeBuffs.get(playerId);
        return buff != null && buff.isActive();
    }

    private static class PoisonBuff {
        final long expiresAt;
        final double oreBonus;
        final double stonePenalty;

        PoisonBuff(long expiresAt, double oreBonus, double stonePenalty) {
            this.expiresAt = expiresAt;
            this.oreBonus = oreBonus;
            this.stonePenalty = stonePenalty;
        }

        boolean isActive() {
            return System.currentTimeMillis() < expiresAt;
        }
    }
}
