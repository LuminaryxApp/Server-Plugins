package com.luminary.groups.placeholder;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import com.luminary.groups.player.PlayerGroupData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for LuminaryGroups.
 */
public class GroupPlaceholders extends PlaceholderExpansion {

    private final LuminaryGroups plugin;

    public GroupPlaceholders(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "luminarygroups";
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
        if (player == null) {
            return "";
        }

        PlayerGroupData data = plugin.getPlayerDataManager().getPlayerData(player);
        Group effectiveGroup = plugin.getPlayerDataManager().getEffectiveGroup(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "prefix" -> {
                return plugin.getPlayerDataManager().getPrefix(player.getUniqueId());
            }
            case "suffix" -> {
                return plugin.getPlayerDataManager().getSuffix(player.getUniqueId());
            }
            case "namecolor" -> {
                return plugin.getPlayerDataManager().getNameColor(player.getUniqueId());
            }
            case "group" -> {
                return effectiveGroup != null ? effectiveGroup.getId() : "none";
            }
            case "group_display" -> {
                return effectiveGroup != null ? effectiveGroup.getDisplayName() : "None";
            }
            case "group_weight" -> {
                return effectiveGroup != null ? String.valueOf(effectiveGroup.getWeight()) : "0";
            }
            case "groups" -> {
                if (data != null) {
                    return String.join(", ", data.getGroups());
                }
                return "";
            }
            case "groups_count" -> {
                if (data != null) {
                    return String.valueOf(data.getGroups().size());
                }
                return "0";
            }
            case "primary" -> {
                if (data != null && data.getPrimaryGroup() != null) {
                    return data.getPrimaryGroup();
                }
                return "none";
            }
            case "chatcolor" -> {
                return effectiveGroup != null ? effectiveGroup.getChatColor() : "&f";
            }
            case "has_custom_prefix" -> {
                return String.valueOf(data != null && data.getCustomPrefix() != null);
            }
            case "has_custom_suffix" -> {
                return String.valueOf(data != null && data.getCustomSuffix() != null);
            }
            case "formatted_name" -> {
                String prefix = plugin.getPlayerDataManager().getPrefix(player.getUniqueId());
                String nameColor = plugin.getPlayerDataManager().getNameColor(player.getUniqueId());
                String suffix = plugin.getPlayerDataManager().getSuffix(player.getUniqueId());
                return prefix + nameColor + player.getName() + suffix;
            }
            case "rank_prefix" -> {
                if (plugin.getRanksHook().isHooked()) {
                    return plugin.getRanksHook().getRankPrefix(player);
                }
                return "";
            }
            case "full_display" -> {
                // Group prefix + rank prefix (if hooked) + name color + name + suffix
                StringBuilder display = new StringBuilder();
                String prefix = plugin.getPlayerDataManager().getPrefix(player.getUniqueId());
                if (!prefix.isEmpty()) {
                    display.append(prefix).append(" ");
                }
                if (plugin.getRanksHook().isHooked()) {
                    String rankDisplay = plugin.getRanksHook().getCombinedRankDisplay(player);
                    if (!rankDisplay.isEmpty()) {
                        display.append(rankDisplay).append(" ");
                    }
                }
                String nameColor = plugin.getPlayerDataManager().getNameColor(player.getUniqueId());
                display.append(nameColor).append(player.getName());
                String suffix = plugin.getPlayerDataManager().getSuffix(player.getUniqueId());
                if (!suffix.isEmpty()) {
                    display.append(" ").append(suffix);
                }
                return display.toString();
            }
        }

        // Check for group-specific placeholders
        if (params.startsWith("has_group_")) {
            String groupId = params.substring("has_group_".length());
            if (data != null) {
                return String.valueOf(data.hasGroup(groupId));
            }
            return "false";
        }

        return null;
    }
}
