package com.luminary.ranks.placeholder;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankPlaceholders extends PlaceholderExpansion {

    private final LuminaryRanks plugin;

    public RankPlaceholders(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "luminaryranks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Luminary";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerRankData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return "";

        Rank rank = plugin.getRankManager().getRank(data.getCurrentRank());

        return switch (params.toLowerCase()) {
            // Rank placeholders
            case "rank" -> data.getCurrentRank();
            case "rank_display" -> rank != null ? rank.getDisplayName() : "None";
            case "rank_prefix" -> rank != null ? rank.getPrefix() : "";

            // Prestige placeholders
            case "prestige" -> String.valueOf(data.getPrestigeLevel());
            case "prestige_max" -> String.valueOf(plugin.getRankManager().getMaxPrestige());
            case "prestige_formatted" -> data.getPrestigeLevel() > 0 ? "&d[P" + data.getPrestigeLevel() + "]" : "";

            // Rebirth placeholders
            case "rebirth" -> String.valueOf(data.getRebirthLevel());
            case "rebirth_max" -> String.valueOf(plugin.getRankManager().getMaxRebirth());
            case "rebirth_formatted" -> data.getRebirthLevel() > 0 ? "&c[R" + data.getRebirthLevel() + "]" : "";

            // Combined prefix
            case "prefix" -> buildFullPrefix(data, rank);
            case "prefix_short" -> buildShortPrefix(data, rank);

            // Multiplier
            case "multiplier" -> TextUtil.formatMultiplier(
                    plugin.getRankManager().getMultiplier(data.getPrestigeLevel(), data.getRebirthLevel()));

            // Stats
            case "total_rankups" -> String.valueOf(data.getTotalRankups());
            case "total_prestiges" -> String.valueOf(data.getTotalPrestiges());
            case "total_rebirths" -> String.valueOf(data.getTotalRebirths());
            case "tokens_spent" -> TextUtil.formatNumber(data.getTokensSpent());

            // Next rank info
            case "next_rank" -> {
                Rank next = plugin.getRankManager().getNextRank(data.getCurrentRank());
                yield next != null ? next.getId() : "MAX";
            }
            case "next_rank_cost" -> {
                Rank next = plugin.getRankManager().getNextRank(data.getCurrentRank());
                yield next != null ? TextUtil.formatNumber(next.getCost()) : "0";
            }

            default -> null;
        };
    }

    private String buildFullPrefix(PlayerRankData data, Rank rank) {
        StringBuilder prefix = new StringBuilder();

        if (data.getRebirthLevel() > 0) {
            prefix.append("&c[R").append(data.getRebirthLevel()).append("] ");
        }
        if (data.getPrestigeLevel() > 0) {
            prefix.append("&d[P").append(data.getPrestigeLevel()).append("] ");
        }
        if (rank != null) {
            prefix.append(rank.getPrefix());
        }

        return prefix.toString().trim();
    }

    private String buildShortPrefix(PlayerRankData data, Rank rank) {
        StringBuilder prefix = new StringBuilder();

        if (data.getRebirthLevel() > 0) {
            prefix.append("&cR").append(data.getRebirthLevel());
        }
        if (data.getPrestigeLevel() > 0) {
            prefix.append("&dP").append(data.getPrestigeLevel());
        }
        if (rank != null) {
            prefix.append(rank.getId());
        }

        return prefix.toString();
    }
}
