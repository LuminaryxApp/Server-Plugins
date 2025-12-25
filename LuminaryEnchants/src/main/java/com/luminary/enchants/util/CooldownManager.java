package com.luminary.enchants.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cooldowns for enchant procs and other actions.
 */
public class CooldownManager {

    // Map: playerId -> enchantId -> lastProcTime
    private final Map<UUID, Map<String, Long>> enchantCooldowns = new ConcurrentHashMap<>();

    // Map: playerId -> lastProcTime (global proc limit)
    private final Map<UUID, Long> globalCooldowns = new ConcurrentHashMap<>();

    // Map: playerId -> enchantId -> procCount in current second
    private final Map<UUID, Map<String, ProcCounter>> procCounters = new ConcurrentHashMap<>();

    /**
     * Check if an enchant is on cooldown for a player.
     */
    public boolean isOnCooldown(UUID playerId, String enchantId, long cooldownMs) {
        if (cooldownMs <= 0) return false;

        Map<String, Long> playerCooldowns = enchantCooldowns.get(playerId);
        if (playerCooldowns == null) return false;

        Long lastProc = playerCooldowns.get(enchantId);
        if (lastProc == null) return false;

        return (System.currentTimeMillis() - lastProc) < cooldownMs;
    }

    /**
     * Get the remaining cooldown time in milliseconds.
     */
    public long getRemainingCooldown(UUID playerId, String enchantId, long cooldownMs) {
        if (cooldownMs <= 0) return 0;

        Map<String, Long> playerCooldowns = enchantCooldowns.get(playerId);
        if (playerCooldowns == null) return 0;

        Long lastProc = playerCooldowns.get(enchantId);
        if (lastProc == null) return 0;

        long elapsed = System.currentTimeMillis() - lastProc;
        return Math.max(0, cooldownMs - elapsed);
    }

    /**
     * Record a proc for cooldown tracking.
     */
    public void recordProc(UUID playerId, String enchantId) {
        enchantCooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(enchantId, System.currentTimeMillis());
    }

    /**
     * Reduce cooldown for an enchant (used by Pause enchant).
     */
    public void reduceCooldown(UUID playerId, String enchantId, long reductionMs) {
        Map<String, Long> playerCooldowns = enchantCooldowns.get(playerId);
        if (playerCooldowns == null) return;

        Long lastProc = playerCooldowns.get(enchantId);
        if (lastProc == null) return;

        // Move the last proc time forward (reducing remaining cooldown)
        playerCooldowns.put(enchantId, lastProc - reductionMs);
    }

    /**
     * Check if a player has exceeded the max procs per second for an enchant.
     */
    public boolean hasExceededProcLimit(UUID playerId, String enchantId, int maxProcsPerSecond) {
        if (maxProcsPerSecond <= 0) return false;

        Map<String, ProcCounter> playerCounters = procCounters.get(playerId);
        if (playerCounters == null) return false;

        ProcCounter counter = playerCounters.get(enchantId);
        if (counter == null) return false;

        long now = System.currentTimeMillis();
        if (now - counter.windowStart >= 1000) {
            // Reset counter for new second
            counter.count = 0;
            counter.windowStart = now;
            return false;
        }

        return counter.count >= maxProcsPerSecond;
    }

    /**
     * Increment the proc counter for rate limiting.
     */
    public void incrementProcCounter(UUID playerId, String enchantId) {
        Map<String, ProcCounter> playerCounters = procCounters.computeIfAbsent(playerId,
                k -> new ConcurrentHashMap<>());

        ProcCounter counter = playerCounters.computeIfAbsent(enchantId, k -> new ProcCounter());
        long now = System.currentTimeMillis();

        if (now - counter.windowStart >= 1000) {
            counter.count = 1;
            counter.windowStart = now;
        } else {
            counter.count++;
        }
    }

    /**
     * Clear all cooldowns for a player (on logout).
     */
    public void clearPlayer(UUID playerId) {
        enchantCooldowns.remove(playerId);
        globalCooldowns.remove(playerId);
        procCounters.remove(playerId);
    }

    /**
     * Clear all cooldown data.
     */
    public void clearAll() {
        enchantCooldowns.clear();
        globalCooldowns.clear();
        procCounters.clear();
    }

    private static class ProcCounter {
        int count = 0;
        long windowStart = System.currentTimeMillis();
    }
}
