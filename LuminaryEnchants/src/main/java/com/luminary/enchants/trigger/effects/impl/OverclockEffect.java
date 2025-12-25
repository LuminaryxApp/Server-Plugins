package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Overclock: Stacking mining speed buff while continuously mining, decays when stopping.
 */
public class OverclockEffect extends AbstractEnchantEffect {

    // Track stacks and last activity per player
    private final Map<UUID, OverclockState> playerStates = new ConcurrentHashMap<>();

    public OverclockEffect(LuminaryEnchants plugin) {
        super(plugin);
        startDecayTask();
    }

    @Override
    public String getEnchantId() {
        return "overclock";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int maxStacks = definition.getParamInt("maxStacks", 10);
        int stackGainPerBreak = definition.getParamInt("stackGainPerBreak", 1);
        int hasteAmplifierBase = definition.getParamInt("hasteAmplifierBase", 0);

        UUID playerId = player.getUniqueId();
        OverclockState state = playerStates.computeIfAbsent(playerId, k -> new OverclockState());

        // Add stacks
        state.stacks = Math.min(state.stacks + stackGainPerBreak, maxStacks);
        state.lastActivity = System.currentTimeMillis();

        // Calculate haste level based on stacks
        int hasteLevel = hasteAmplifierBase + (state.stacks / 2);
        hasteLevel = Math.min(hasteLevel, 5); // Cap at Haste VI

        // Apply haste effect
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FAST_DIGGING,
                60, // 3 seconds, continuously refreshed
                hasteLevel,
                false, false, true
        ));

        // Visual feedback at certain thresholds
        if (state.stacks == maxStacks) {
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.3f, 2.0f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&e\u26A1 OVERCLOCK MAX! Haste " + (hasteLevel + 1)));
        } else if (state.stacks % 3 == 0) {
            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&e\u26A1 Overclock: " + state.stacks + "/" + maxStacks +
                            " (Haste " + (hasteLevel + 1) + ")"));
        }
    }

    private void startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            int decayDelayTicks = plugin.getConfigManager().getMainConfig()
                    .getInt("enchants.overclock.decayDelayTicks", 40) * 50; // Convert to ms

            playerStates.forEach((playerId, state) -> {
                if (now - state.lastActivity > decayDelayTicks && state.stacks > 0) {
                    state.stacks = Math.max(0, state.stacks - 1);

                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && state.stacks == 0) {
                        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                                .createDisplayComponent("&7Overclock faded..."));
                    }
                }
            });
        }, 20L, 20L); // Run every second
    }

    public int getStacks(UUID playerId) {
        OverclockState state = playerStates.get(playerId);
        return state != null ? state.stacks : 0;
    }

    public void clearPlayer(UUID playerId) {
        playerStates.remove(playerId);
    }

    private static class OverclockState {
        int stacks = 0;
        long lastActivity = System.currentTimeMillis();
    }
}
