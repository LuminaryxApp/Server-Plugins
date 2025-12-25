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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mirror: Copies the drop outcome of the broken block (duplication).
 */
public class MirrorEffect extends AbstractEnchantEffect {

    // Track last mirror proc to prevent stacking
    private final Map<UUID, Long> lastMirrorProc = new ConcurrentHashMap<>();

    public MirrorEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "mirror";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        // Prevent stacking with Second Hand
        if (context == ProcEngine.ProcContext.SECONDARY_ROLL) {
            return;
        }

        double duplicateChance = definition.getParamDouble("duplicateChance", 0.3);
        long cooldownMs = definition.getParamLong("cooldownMs", 1000);

        // Scale chance with level
        double scaledChance = Math.min(duplicateChance + (level * 0.003), 0.8);

        UUID playerId = player.getUniqueId();

        // Check internal cooldown
        Long lastProc = lastMirrorProc.get(playerId);
        if (lastProc != null && (System.currentTimeMillis() - lastProc) < cooldownMs) {
            return;
        }

        if (!WeightedRandom.roll(scaledChance)) {
            return;
        }

        // Duplicate the drops
        Collection<ItemStack> drops = block.getDrops(pickaxe, player);
        if (drops.isEmpty()) {
            return;
        }

        addDropsToInventory(player, drops);

        lastMirrorProc.put(playerId, System.currentTimeMillis());

        // Visual effect
        player.getWorld().spawnParticle(Particle.END_ROD,
                block.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.05);
        // Create a reflection effect with second particle burst
        player.getWorld().spawnParticle(Particle.END_ROD,
                block.getLocation().add(0.5, 0.5, 0.5).add(0.3, 0.3, 0.3),
                20, 0.3, 0.3, 0.3, 0.05);
        player.playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.2f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&f\u2728 Mirror duplicated drops!"));
    }

    public void clearPlayer(UUID playerId) {
        lastMirrorProc.remove(playerId);
    }
}
