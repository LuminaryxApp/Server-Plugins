package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Keyfinder: Chance to award keys (virtual counter) or key items.
 */
public class KeyfinderEffect extends AbstractEnchantEffect {

    public KeyfinderEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "keyfinder";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int keysBase = definition.getParamInt("keysBase", 1);
        int keysPerLevel = definition.getParamInt("keysPerLevel", 0);
        String keyType = definition.getParamString("keyType", "crate_basic");
        boolean convertToTokens = definition.getParamBoolean("convertToTokens", false);
        long tokensPerKey = definition.getParamLong("tokensPerKey", 100);

        // Calculate keys gained
        int keys = keysBase + (keysPerLevel * level);

        // Apply beacon key gain multiplier
        double keyMultiplier = getBeaconProvider().multiplier(
                player.getUniqueId(), BeaconEffectProvider.KEY_GAIN);
        keys = (int) Math.ceil(keys * keyMultiplier);

        if (keys <= 0) return;

        if (convertToTokens && getTokenEconomy().isAvailable()) {
            // Convert keys to tokens
            long tokens = keys * tokensPerKey;
            depositTokens(player, tokens, "Keyfinder enchant");
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&a+" + tokens + " tokens from Keyfinder!"));
        } else {
            // Award virtual keys (would integrate with crate plugin)
            // For now, send message - actual key giving would use external plugin hook
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&a+" + keys + " " + keyType + " key(s)!"));

            // Future: Hook into crate plugin to give actual keys
            // CratePlugin.giveKeys(player, keyType, keys);
        }
    }
}
