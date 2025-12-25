package com.luminary.miners.placeholder;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.miner.MinerType;
import com.luminary.miners.miner.PlayerMiner;
import com.luminary.miners.miner.ResourceType;
import com.luminary.miners.player.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for miner placeholders.
 */
public class MinerPlaceholders extends PlaceholderExpansion {

    private final LuminaryMiners plugin;

    public MinerPlaceholders(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "miners";
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
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // %miners_count%
        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(data.getMinerCount());
        }

        // %miners_production_tokens%
        if (params.equalsIgnoreCase("production_tokens")) {
            return formatNumber(getProductionPerHour(data, ResourceType.TOKENS));
        }

        // %miners_production_beacons%
        if (params.equalsIgnoreCase("production_beacons")) {
            return formatNumber(getProductionPerHour(data, ResourceType.BEACONS));
        }

        // %miners_production_gems%
        if (params.equalsIgnoreCase("production_gems")) {
            return formatNumber(getProductionPerHour(data, ResourceType.GEMS));
        }

        // %miners_stored_tokens%
        if (params.equalsIgnoreCase("stored_tokens")) {
            return formatNumber(getStoredResources(data, ResourceType.TOKENS));
        }

        // %miners_stored_beacons%
        if (params.equalsIgnoreCase("stored_beacons")) {
            return formatNumber(getStoredResources(data, ResourceType.BEACONS));
        }

        // %miners_stored_gems%
        if (params.equalsIgnoreCase("stored_gems")) {
            return formatNumber(getStoredResources(data, ResourceType.GEMS));
        }

        // %miners_total_stored%
        if (params.equalsIgnoreCase("total_stored")) {
            double total = 0;
            for (PlayerMiner miner : data.getMiners()) {
                total += miner.getStoredResources();
            }
            return formatNumber(total);
        }

        // %miners_active_count%
        if (params.equalsIgnoreCase("active_count")) {
            int count = 0;
            for (PlayerMiner miner : data.getMiners()) {
                if (miner.isActive()) count++;
            }
            return String.valueOf(count);
        }

        // %miners_type_count_<type>%
        if (params.startsWith("type_count_")) {
            String typeId = params.substring("type_count_".length());
            return String.valueOf(data.getMinerCountByType(typeId));
        }

        return null;
    }

    private double getProductionPerHour(PlayerData data, ResourceType resourceType) {
        double total = 0;
        for (PlayerMiner miner : data.getMiners()) {
            if (!miner.isActive()) continue;

            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null || type.getResourceType() != resourceType) continue;

            total += type.getProductionPerHour(miner.getTier());
        }
        return total;
    }

    private double getStoredResources(PlayerData data, ResourceType resourceType) {
        double total = 0;
        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null || type.getResourceType() != resourceType) continue;

            total += miner.getStoredResources();
        }
        return total;
    }

    private String formatNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000);
        } else {
            return String.format("%.0f", number);
        }
    }
}
