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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Overload: Builds charge from mining; when full, releases a big AOE break + bonus payout.
 */
public class OverloadEffect extends AbstractEnchantEffect {

    // Track charge per player
    private final Map<UUID, Integer> playerCharge = new ConcurrentHashMap<>();

    public OverloadEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "overload";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int chargePerBlock = definition.getParamInt("chargePerBlock", 1);
        int chargeMax = definition.getParamInt("chargeMax", 100);
        int burstRadius = definition.getParamInt("burstRadius", 3);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 64),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );
        long bonusTokens = definition.getParamLong("bonusTokens", 1000);

        UUID playerId = player.getUniqueId();
        int currentCharge = playerCharge.getOrDefault(playerId, 0) + chargePerBlock;

        if (currentCharge >= chargeMax) {
            // OVERLOAD BURST!
            playerCharge.put(playerId, 0);

            // Big AOE break
            int broken = breakBlocksAOE(player, block, pickaxe, burstRadius, maxBlocks, DropMode.INVENTORY);

            // Bonus tokens
            if (getTokenEconomy().isAvailable()) {
                long scaledBonus = bonusTokens + (level * 10);
                depositTokens(player, scaledBonus, "Overload burst");
            }

            // Epic visual effect
            player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,
                    block.getLocation().add(0.5, 0.5, 0.5), 3, 1, 1, 1, 0);
            player.getWorld().spawnParticle(Particle.FLAME,
                    block.getLocation().add(0.5, 0.5, 0.5), 100, burstRadius, burstRadius, burstRadius, 0.1);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    block.getLocation().add(0.5, 0.5, 0.5), 50, burstRadius, burstRadius, burstRadius, 0.2);
            player.playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
            player.playSound(block.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&c\uD83D\uDCA5 OVERLOAD! " + broken +
                            " blocks + " + bonusTokens + " tokens!"));
        } else {
            playerCharge.put(playerId, currentCharge);

            // Show charge progress periodically
            if (currentCharge % 20 == 0 || currentCharge >= chargeMax - 10) {
                int percentage = (currentCharge * 100) / chargeMax;

                // Build charge bar
                StringBuilder bar = new StringBuilder("&8[");
                int filled = percentage / 10;
                for (int i = 0; i < 10; i++) {
                    if (i < filled) {
                        bar.append("&c\u2588");
                    } else {
                        bar.append("&7\u2588");
                    }
                }
                bar.append("&8] &c").append(percentage).append("%");

                player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                        .createDisplayComponent("&cOverload: " + bar));
            }
        }
    }

    public int getCharge(UUID playerId) {
        return playerCharge.getOrDefault(playerId, 0);
    }

    public void clearPlayer(UUID playerId) {
        playerCharge.remove(playerId);
    }
}
