package com.luminary.enchants.trigger;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.api.events.PickEnchantProcEvent;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.pickaxe.EnchantTrigger;
import com.luminary.enchants.trigger.effects.EnchantEffect;
import com.luminary.enchants.trigger.effects.EnchantEffectRegistry;
import com.luminary.enchants.util.CooldownManager;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Core engine for processing enchant procs.
 * Handles chance rolls, cooldowns, safety limits, and effect invocation.
 */
public class ProcEngine {

    private final LuminaryEnchants plugin;
    private final CooldownManager cooldownManager;
    private final EnchantEffectRegistry effectRegistry;

    // Track Enhancer buff state per player
    private final Map<UUID, EnhancerBuff> enhancerBuffs = new HashMap<>();

    // Track if we're in a secondary roll (for Second Hand)
    private final Set<UUID> inSecondaryRoll = new HashSet<>();

    public ProcEngine(LuminaryEnchants plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager();
        this.effectRegistry = new EnchantEffectRegistry(plugin);
    }

    /**
     * Process all applicable enchants for a block break event.
     */
    public void processBlockBreak(Player player, ItemStack pickaxe, Block block, ProcContext context) {
        if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            return;
        }

        Map<String, Integer> enchants = plugin.getPickaxeDataManager().getEnchants(pickaxe);
        if (enchants.isEmpty()) {
            return;
        }

        List<EnchantDefinition> triggeredEnchants = plugin.getEnchantRegistry()
                .getEnchantsByTrigger(EnchantTrigger.BLOCK_BREAK);

        // Process each enchant
        for (EnchantDefinition definition : triggeredEnchants) {
            int level = enchants.getOrDefault(definition.getId(), 0);
            if (level <= 0) continue;

            tryProc(player, pickaxe, block, definition, level, context);
        }
    }

    /**
     * Process interact trigger (for Laser toggle, etc.).
     */
    public void processInteract(Player player, ItemStack pickaxe, Block block) {
        if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            return;
        }

        Map<String, Integer> enchants = plugin.getPickaxeDataManager().getEnchants(pickaxe);
        if (enchants.isEmpty()) {
            return;
        }

        List<EnchantDefinition> triggeredEnchants = plugin.getEnchantRegistry()
                .getEnchantsByTrigger(EnchantTrigger.INTERACT);

        for (EnchantDefinition definition : triggeredEnchants) {
            int level = enchants.getOrDefault(definition.getId(), 0);
            if (level <= 0) continue;

            tryProc(player, pickaxe, block, definition, level, ProcContext.INTERACT);
        }
    }

    /**
     * Attempt to proc a specific enchant.
     */
    public boolean tryProc(Player player, ItemStack pickaxe, Block block,
                           EnchantDefinition definition, int level, ProcContext context) {
        UUID playerId = player.getUniqueId();

        // Anti-abuse checks
        if (!passesAntiAbuseChecks(player, definition)) {
            return false;
        }

        // Block filter check
        if (block != null && !definition.isBlockAllowed(block.getType())) {
            return false;
        }

        // Cooldown check
        if (cooldownManager.isOnCooldown(playerId, definition.getId(), definition.getCooldownMs())) {
            return false;
        }

        // Rate limit check
        if (cooldownManager.hasExceededProcLimit(playerId, definition.getId(),
                definition.getMaxProcsPerSecond())) {
            return false;
        }

        // Calculate proc chance with beacon multiplier
        double beaconMultiplier = plugin.getHookManager().getBeaconProvider()
                .multiplier(playerId, BeaconEffectProvider.PROC_CHANCE);

        // Add Enhancer buff if active
        EnhancerBuff buff = enhancerBuffs.get(playerId);
        if (buff != null && buff.isActive()) {
            beaconMultiplier += buff.bonus;
        }

        double procChance = definition.calculateProcChance(level, beaconMultiplier);

        // Roll for proc
        if (!WeightedRandom.roll(procChance)) {
            return false;
        }

        // Fire event
        PickEnchantProcEvent event = new PickEnchantProcEvent(
                player, definition.getId(), level, pickaxe, block,
                context == ProcContext.SECONDARY_ROLL ?
                        PickEnchantProcEvent.ProcContext.SECONDARY_ROLL :
                        PickEnchantProcEvent.ProcContext.BLOCK_BREAK
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Execute effect
        EnchantEffect effect = effectRegistry.getEffect(definition.getId());
        if (effect != null) {
            try {
                effect.execute(player, pickaxe, block, definition, level, context);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing enchant " + definition.getId() +
                        ": " + e.getMessage());
            }
        }

        // Record proc for cooldown and rate limiting
        cooldownManager.recordProc(playerId, definition.getId());
        cooldownManager.incrementProcCounter(playerId, definition.getId());

        return true;
    }

    /**
     * Handle secondary roll from Second Hand enchant.
     * Returns true if a proc occurred.
     */
    public boolean trySecondaryRoll(Player player, ItemStack pickaxe, Block block,
                                     Map<String, Integer> enchants) {
        UUID playerId = player.getUniqueId();

        // Prevent infinite recursion
        if (inSecondaryRoll.contains(playerId)) {
            return false;
        }

        inSecondaryRoll.add(playerId);
        try {
            // Pick a random enchant to re-roll (excluding Second Hand itself)
            List<Map.Entry<String, Integer>> eligible = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                if (!entry.getKey().equals("second_hand") && entry.getValue() > 0) {
                    EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(entry.getKey());
                    if (def != null && def.hasTrigger(EnchantTrigger.BLOCK_BREAK)) {
                        eligible.add(entry);
                    }
                }
            }

            if (eligible.isEmpty()) {
                return false;
            }

            // Pick random
            Map.Entry<String, Integer> selected = eligible.get(
                    WeightedRandom.randomInt(0, eligible.size() - 1));

            EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(selected.getKey());
            if (def == null) return false;

            return tryProc(player, pickaxe, block, def, selected.getValue(),
                    ProcContext.SECONDARY_ROLL);
        } finally {
            inSecondaryRoll.remove(playerId);
        }
    }

    /**
     * Check if player passes anti-abuse requirements.
     */
    private boolean passesAntiAbuseChecks(Player player, EnchantDefinition definition) {
        // Creative mode check
        if (definition.isIgnoreIfCreative() && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        // Permission check
        if (definition.isIgnoreIfNoPermission()) {
            String permission = "luminaryenchants.enchant." + definition.getId();
            if (!player.hasPermission(permission)) {
                return false;
            }
        }

        // Mine region check would go here (requires WorldGuard or custom integration)
        // For now, we skip this check

        return true;
    }

    /**
     * Apply Enhancer buff to a player.
     */
    public void applyEnhancerBuff(UUID playerId, int durationTicks, double bonus) {
        long expiresAt = System.currentTimeMillis() + (durationTicks * 50L);
        EnhancerBuff existing = enhancerBuffs.get(playerId);

        if (existing != null && existing.isActive()) {
            // Refresh duration, don't stack bonus
            existing.expiresAt = expiresAt;
        } else {
            enhancerBuffs.put(playerId, new EnhancerBuff(expiresAt, bonus));
        }
    }

    /**
     * Get cooldown manager for external access.
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Get effect registry.
     */
    public EnchantEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

    /**
     * Clean up player data on logout.
     */
    public void handlePlayerQuit(UUID playerId) {
        cooldownManager.clearPlayer(playerId);
        enhancerBuffs.remove(playerId);
        inSecondaryRoll.remove(playerId);
    }

    /**
     * Context for when a proc is happening.
     */
    public enum ProcContext {
        BLOCK_BREAK,
        INTERACT,
        SECONDARY_ROLL
    }

    private static class EnhancerBuff {
        long expiresAt;
        final double bonus;

        EnhancerBuff(long expiresAt, double bonus) {
            this.expiresAt = expiresAt;
            this.bonus = bonus;
        }

        boolean isActive() {
            return System.currentTimeMillis() < expiresAt;
        }
    }
}
