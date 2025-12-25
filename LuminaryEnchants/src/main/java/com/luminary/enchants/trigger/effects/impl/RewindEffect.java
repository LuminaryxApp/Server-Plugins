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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rewind: After X blocks mined, refund a chunk of durability.
 */
public class RewindEffect extends AbstractEnchantEffect {

    // Track blocks mined per player
    private final Map<UUID, Integer> blockCounter = new ConcurrentHashMap<>();

    public RewindEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "rewind";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int blocksPerCharge = definition.getParamInt("blocksPerCharge", 50);
        int durabilityRefund = definition.getParamInt("durabilityRefund", 10);

        // Scale with level
        int scaledRefund = durabilityRefund + (level / 5);
        // Lower blocks needed at higher levels
        int scaledBlocksNeeded = Math.max(10, blocksPerCharge - (level / 2));

        UUID playerId = player.getUniqueId();
        int currentCount = blockCounter.getOrDefault(playerId, 0) + 1;

        if (currentCount >= scaledBlocksNeeded) {
            // Refund durability
            ItemMeta meta = pickaxe.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int newDamage = Math.max(0, currentDamage - scaledRefund);
                damageable.setDamage(newDamage);
                pickaxe.setItemMeta(meta);
            }

            blockCounter.put(playerId, 0);

            // Visual effect
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                    player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.5f, 1.2f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&3\u21BA Rewind restored " + scaledRefund + " durability!"));
        } else {
            blockCounter.put(playerId, currentCount);
        }
    }

    public int getBlockCount(UUID playerId) {
        return blockCounter.getOrDefault(playerId, 0);
    }

    public void clearPlayer(UUID playerId) {
        blockCounter.remove(playerId);
    }
}
