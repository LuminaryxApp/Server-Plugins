package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * Rainbow: Random bonus type each proc - tokens, keys, extra drops, or haste burst.
 */
public class RainbowEffect extends AbstractEnchantEffect {

    private static final Color[] RAINBOW_COLORS = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
            Color.AQUA, Color.BLUE, Color.PURPLE
    };

    public RainbowEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "rainbow";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        // Default weights
        double tokenWeight = definition.getParamDouble("tokenWeight", 1.0);
        double keyWeight = definition.getParamDouble("keyWeight", 1.0);
        double dropWeight = definition.getParamDouble("dropWeight", 1.0);
        double hasteWeight = definition.getParamDouble("hasteWeight", 1.0);

        // Bonus amounts
        long tokenBonus = definition.getParamLong("tokenBonus", 200) + (level * 5);
        int keyBonus = definition.getParamInt("keyBonus", 1);
        int hasteDuration = definition.getParamInt("hasteDuration", 100) + (level * 2);

        // Select random bonus type
        WeightedRandom<BonusType> selector = new WeightedRandom<>();
        selector.add(BonusType.TOKENS, tokenWeight);
        selector.add(BonusType.KEYS, keyWeight);
        selector.add(BonusType.DROPS, dropWeight);
        selector.add(BonusType.HASTE, hasteWeight);

        BonusType selected = selector.select();
        String message;
        Color particleColor;

        switch (selected) {
            case TOKENS -> {
                if (getTokenEconomy().isAvailable()) {
                    depositTokens(player, tokenBonus, "Rainbow enchant");
                }
                message = "&e+" + tokenBonus + " tokens!";
                particleColor = Color.YELLOW;
            }
            case KEYS -> {
                // Would integrate with crate plugin
                message = "&b+" + keyBonus + " keys!";
                particleColor = Color.AQUA;
            }
            case DROPS -> {
                // Double the drops from this block
                addDropsToInventory(player, block.getDrops(pickaxe, player));
                message = "&a+Extra drops!";
                particleColor = Color.GREEN;
            }
            case HASTE -> {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.FAST_DIGGING,
                        hasteDuration,
                        1 + (level / 50),
                        false, true, true
                ));
                message = "&d+Haste burst!";
                particleColor = Color.PURPLE;
            }
            default -> {
                message = "&f+Rainbow!";
                particleColor = Color.WHITE;
            }
        }

        // Rainbow particle effect
        for (int i = 0; i < RAINBOW_COLORS.length; i++) {
            Particle.DustOptions dust = new Particle.DustOptions(RAINBOW_COLORS[i], 1.0f);
            player.getWorld().spawnParticle(Particle.REDSTONE,
                    block.getLocation().add(0.5, 0.5 + (i * 0.2), 0.5),
                    3, 0.2, 0.1, 0.2, 0, dust);
        }
        player.playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.5f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&c\u2728 &6R&ea&ai&bn&db&5o&dw&f: " + message));
    }

    private enum BonusType {
        TOKENS, KEYS, DROPS, HASTE
    }
}
