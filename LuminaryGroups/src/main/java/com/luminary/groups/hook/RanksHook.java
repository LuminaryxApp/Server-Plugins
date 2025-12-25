package com.luminary.groups.hook;

import com.luminary.groups.LuminaryGroups;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Hook into LuminaryRanks for combined prefix display.
 */
public class RanksHook {

    private final LuminaryGroups plugin;
    private Plugin ranksPlugin;
    private Object rankManager;
    private Object playerDataManager;
    private Method getPlayerDataMethod;
    private Method getEffectiveRankMethod;
    private Method getRankPrefixMethod;
    private Method getPrestigeMethod;
    private Method getRebirthMethod;
    private Method getPrestigePrefixMethod;
    private Method getRebirthPrefixMethod;

    private boolean hooked = false;

    public RanksHook(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        ranksPlugin = Bukkit.getPluginManager().getPlugin("LuminaryRanks");
        if (ranksPlugin == null || !ranksPlugin.isEnabled()) {
            plugin.getLogger().info("LuminaryRanks not found. Running standalone.");
            return;
        }

        try {
            // Get managers via reflection
            Method getRankManager = ranksPlugin.getClass().getMethod("getRankManager");
            rankManager = getRankManager.invoke(ranksPlugin);

            Method getPlayerDataManagerMethod = ranksPlugin.getClass().getMethod("getPlayerDataManager");
            playerDataManager = getPlayerDataManagerMethod.invoke(ranksPlugin);

            // Get methods for player data
            getPlayerDataMethod = playerDataManager.getClass().getMethod("getPlayerData", UUID.class);

            // Get methods for rank info
            Class<?> playerDataClass = Class.forName("com.luminary.ranks.player.PlayerRankData");
            getEffectiveRankMethod = playerDataClass.getMethod("getEffectiveRank");
            getPrestigeMethod = playerDataClass.getMethod("getPrestige");
            getRebirthMethod = playerDataClass.getMethod("getRebirth");

            // Get methods for rank/prestige/rebirth objects
            Class<?> rankClass = Class.forName("com.luminary.ranks.rank.Rank");
            getRankPrefixMethod = rankClass.getMethod("getPrefix");

            Class<?> prestigeClass = Class.forName("com.luminary.ranks.rank.Prestige");
            getPrestigePrefixMethod = prestigeClass.getMethod("getPrefix");

            Class<?> rebirthClass = Class.forName("com.luminary.ranks.rank.Rebirth");
            getRebirthPrefixMethod = rebirthClass.getMethod("getPrefix");

            hooked = true;
            plugin.getLogger().info("Successfully hooked into LuminaryRanks!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuminaryRanks: " + e.getMessage());
            hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    /**
     * Get the player's rank prefix from LuminaryRanks.
     */
    public String getRankPrefix(Player player) {
        if (!hooked) {
            return "";
        }

        try {
            Object playerData = getPlayerDataMethod.invoke(playerDataManager, player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            Object rank = getEffectiveRankMethod.invoke(playerData);
            if (rank != null) {
                return (String) getRankPrefixMethod.invoke(rank);
            }
        } catch (Exception e) {
            // Silently fail
        }

        return "";
    }

    /**
     * Get the player's prestige prefix from LuminaryRanks.
     */
    public String getPrestigePrefix(Player player) {
        if (!hooked) {
            return "";
        }

        try {
            Object playerData = getPlayerDataMethod.invoke(playerDataManager, player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            Object prestige = getPrestigeMethod.invoke(playerData);
            if (prestige != null) {
                return (String) getPrestigePrefixMethod.invoke(prestige);
            }
        } catch (Exception e) {
            // Silently fail
        }

        return "";
    }

    /**
     * Get the player's rebirth prefix from LuminaryRanks.
     */
    public String getRebirthPrefix(Player player) {
        if (!hooked) {
            return "";
        }

        try {
            Object playerData = getPlayerDataMethod.invoke(playerDataManager, player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            Object rebirth = getRebirthMethod.invoke(playerData);
            if (rebirth != null) {
                return (String) getRebirthPrefixMethod.invoke(rebirth);
            }
        } catch (Exception e) {
            // Silently fail
        }

        return "";
    }

    /**
     * Get the combined rank display (rebirth + prestige + rank).
     */
    public String getCombinedRankDisplay(Player player) {
        if (!hooked) {
            return "";
        }

        StringBuilder display = new StringBuilder();

        String rebirthPrefix = getRebirthPrefix(player);
        if (!rebirthPrefix.isEmpty()) {
            display.append(rebirthPrefix);
        }

        String prestigePrefix = getPrestigePrefix(player);
        if (!prestigePrefix.isEmpty()) {
            display.append(prestigePrefix);
        }

        String rankPrefix = getRankPrefix(player);
        if (!rankPrefix.isEmpty()) {
            display.append(rankPrefix);
        }

        return display.toString();
    }
}
