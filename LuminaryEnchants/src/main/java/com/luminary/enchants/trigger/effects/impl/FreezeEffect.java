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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Freeze: Grants mining speed buff (Haste) for a duration.
 * Would integrate with mine plugin to freeze regeneration if available.
 */
public class FreezeEffect extends AbstractEnchantEffect {

    public FreezeEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "freeze";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int durationTicks = definition.getParamInt("durationTicks", 200);
        int hasteAmplifier = definition.getParamInt("hasteAmplifierFallback", 1);

        // Scale with level
        int scaledAmplifier = Math.min(hasteAmplifier + (level / 20), 5); // Cap at Haste VI
        int scaledDuration = durationTicks + (level * 2);

        // Apply Haste effect
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FAST_DIGGING,
                scaledDuration,
                scaledAmplifier,
                false,
                true,
                true
        ));

        // Visual/sound effects
        player.getWorld().spawnParticle(Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&b\u2744 Freeze! Haste " + (scaledAmplifier + 1) +
                        " for " + TextUtil.formatTicks(scaledDuration)));

        // Future: If mine region API exists, call it to freeze regeneration
        // MineRegionAPI.freezeRegeneration(player.getLocation(), durationTicks);
    }
}
