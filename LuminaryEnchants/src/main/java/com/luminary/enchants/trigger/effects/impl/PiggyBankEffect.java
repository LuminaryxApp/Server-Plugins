package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Piggy Bank: Stores tokens internally and pays out in chunks with bonus.
 */
public class PiggyBankEffect extends AbstractEnchantEffect {

    // Track stored tokens per player
    private final Map<UUID, Long> storedTokens = new ConcurrentHashMap<>();

    public PiggyBankEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "piggy_bank";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double storePercent = definition.getParamDouble("storePercent", 0.1);
        long payoutThreshold = definition.getParamLong("payoutThreshold", 10000);
        double payoutBonusPercent = definition.getParamDouble("payoutBonusPercent", 0.15);

        // Base tokens earned from block (simulated - would integrate with token rewards)
        long baseTokens = definition.getParamLong("baseTokensPerBlock", 10) * level;

        // Calculate how much to store
        long toStore = (long) (baseTokens * storePercent);

        if (toStore <= 0) return;

        UUID playerId = player.getUniqueId();
        long currentStored = storedTokens.getOrDefault(playerId, 0L);
        long newStored = currentStored + toStore;

        // Check if threshold reached
        if (newStored >= payoutThreshold) {
            // Payout with bonus
            long bonus = (long) (newStored * payoutBonusPercent);
            long payout = newStored + bonus;

            if (getTokenEconomy().isAvailable()) {
                depositTokens(player, payout, "Piggy Bank payout");
                player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                        .createDisplayComponent("&6Piggy Bank paid out " +
                                TextUtil.formatNumber(payout) + " tokens! (+" +
                                TextUtil.formatNumber(bonus) + " bonus)"));
            }

            storedTokens.put(playerId, 0L);
        } else {
            storedTokens.put(playerId, newStored);
        }
    }

    /**
     * Get current stored amount for a player.
     */
    public long getStoredAmount(UUID playerId) {
        return storedTokens.getOrDefault(playerId, 0L);
    }

    /**
     * Clear stored tokens for a player (on logout or manual clear).
     */
    public void clearPlayer(UUID playerId) {
        storedTokens.remove(playerId);
    }
}
