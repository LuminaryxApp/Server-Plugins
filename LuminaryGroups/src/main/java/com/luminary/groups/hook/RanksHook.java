package com.luminary.groups.hook;

import com.luminary.groups.LuminaryGroups;
import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.rank.Prestige;
import com.luminary.ranks.rank.Rebirth;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Hook into LuminaryRanks for combined prefix display.
 */
public class RanksHook {

    private final LuminaryGroups plugin;
    private LuminaryRanks ranksPlugin;
    private boolean hooked = false;

    public RanksHook(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        Plugin ranks = Bukkit.getPluginManager().getPlugin("LuminaryRanks");
        if (ranks == null || !ranks.isEnabled()) {
            plugin.getLogger().info("LuminaryRanks not found. Running standalone.");
            return;
        }

        try {
            ranksPlugin = (LuminaryRanks) ranks;
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
        if (!hooked || ranksPlugin == null) {
            return "";
        }

        try {
            PlayerRankData playerData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            String rankId = playerData.getCurrentRank();
            Rank rank = ranksPlugin.getRankManager().getRank(rankId);
            if (rank != null) {
                return rank.getPrefix();
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
        if (!hooked || ranksPlugin == null) {
            return "";
        }

        try {
            PlayerRankData playerData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            int prestigeLevel = playerData.getPrestigeLevel();
            if (prestigeLevel > 0) {
                Prestige prestige = ranksPlugin.getRankManager().getPrestige(prestigeLevel);
                if (prestige != null) {
                    return prestige.getPrefix();
                }
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
        if (!hooked || ranksPlugin == null) {
            return "";
        }

        try {
            PlayerRankData playerData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            int rebirthLevel = playerData.getRebirthLevel();
            if (rebirthLevel > 0) {
                Rebirth rebirth = ranksPlugin.getRankManager().getRebirth(rebirthLevel);
                if (rebirth != null) {
                    return rebirth.getPrefix();
                }
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
            display.append(rebirthPrefix).append(" ");
        }

        String prestigePrefix = getPrestigePrefix(player);
        if (!prestigePrefix.isEmpty()) {
            display.append(prestigePrefix).append(" ");
        }

        String rankPrefix = getRankPrefix(player);
        if (!rankPrefix.isEmpty()) {
            display.append(rankPrefix).append(" ");
        }

        return display.toString().trim();
    }
}
