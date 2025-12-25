package com.luminary.economy.scoreboard;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.data.PlayerData;
import com.luminary.economy.util.TextUtil;
import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.rank.Prestige;
import com.luminary.ranks.rank.Rebirth;
import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Manages the scoreboard sidebar display for players.
 */
public class ScoreboardManager {

    private final LuminaryEconomy plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private BukkitTask updateTask;

    private String title;
    private List<String> lines;
    private LuminaryRanks ranksPlugin;
    private LuminaryGroups groupsPlugin;
    private boolean hooksInitialized = false;

    public ScoreboardManager(LuminaryEconomy plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void ensureHooksInitialized() {
        if (hooksInitialized) {
            return;
        }

        // Hook LuminaryRanks
        if (ranksPlugin == null) {
            Plugin ranks = Bukkit.getPluginManager().getPlugin("LuminaryRanks");
            if (ranks != null && ranks.isEnabled()) {
                ranksPlugin = (LuminaryRanks) ranks;
                plugin.getLogger().info("Hooked into LuminaryRanks for scoreboard!");
            }
        }

        // Hook LuminaryGroups
        if (groupsPlugin == null) {
            Plugin groups = Bukkit.getPluginManager().getPlugin("LuminaryGroups");
            if (groups != null && groups.isEnabled()) {
                groupsPlugin = (LuminaryGroups) groups;
                plugin.getLogger().info("Hooked into LuminaryGroups for scoreboard!");
            }
        }

        // Mark as initialized once both are checked
        if (ranksPlugin != null && groupsPlugin != null) {
            hooksInitialized = true;
        }
    }

    public void loadConfig() {
        var config = plugin.getConfigManager().getScoreboardConfig();
        this.title = config.getString("scoreboard.title", "&6&lLuminary Prison");
        this.lines = config.getStringList("scoreboard.lines");

        if (lines.isEmpty()) {
            // Default lines
            lines = Arrays.asList(
                    "&7&m----------------",
                    "&fPlayer: &a{player}",
                    "&fRank: &e{rank}",
                    "",
                    "&6&lCurrencies",
                    "&f{tokens_icon} Tokens: &e{tokens}",
                    "&f{beacons_icon} Beacons: &b{beacons}",
                    "&f{gems_icon} Gems: &d{gems}",
                    "",
                    "&7&m----------------",
                    "&eLuminaryPrison.com"
            );
        }
    }

    public void reloadConfig() {
        loadConfig();
        // Force update all scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    public void startUpdateTask() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) {
            return;
        }

        int interval = plugin.getConfigManager().getScoreboardUpdateInterval();
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
                if (data != null && data.isScoreboardEnabled()) {
                    updateScoreboard(player);
                }
            }
        }, 20L, interval);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        // Remove all scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
        }
        playerScoreboards.clear();
    }

    /**
     * Create or get the scoreboard for a player.
     */
    public void createScoreboard(Player player) {
        if (!plugin.getConfigManager().isScoreboardEnabled()) {
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data != null && !data.isScoreboardEnabled()) {
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("luminary", Criteria.DUMMY,
                TextUtil.colorize(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);

        updateScoreboard(player);
    }

    /**
     * Update the scoreboard for a player.
     */
    public void updateScoreboard(Player player) {
        // Ensure plugin hooks are initialized (lazy init for load order)
        ensureHooksInitialized();

        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            createScoreboard(player);
            return;
        }

        Objective objective = scoreboard.getObjective("luminary");
        if (objective == null) {
            createScoreboard(player);
            return;
        }

        // Update title
        objective.displayName(TextUtil.colorize(replacePlaceholders(title, player)));

        // Clear old entries
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Add lines (reverse order for proper display)
        int score = lines.size();
        Set<String> usedEntries = new HashSet<>();

        for (String line : lines) {
            String parsed = replacePlaceholders(line, player);

            // Convert color codes for scoreboard
            String colorized = TextUtil.colorizeString(parsed);

            // Ensure unique entries
            while (usedEntries.contains(colorized)) {
                colorized = colorized + " ";
            }
            usedEntries.add(colorized);

            // Truncate if too long (scoreboard entry limit is 40 chars)
            if (colorized.length() > 40) {
                colorized = colorized.substring(0, 40);
            }

            objective.getScore(colorized).setScore(score);
            score--;
        }
    }

    /**
     * Remove the scoreboard from a player.
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Toggle scoreboard for a player.
     */
    public void toggleScoreboard(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        boolean newState = !data.isScoreboardEnabled();
        data.setScoreboardEnabled(newState);

        if (newState) {
            createScoreboard(player);
        } else {
            removeScoreboard(player);
        }
    }

    /**
     * Replace placeholders in a line.
     */
    private String replacePlaceholders(String line, Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            data = plugin.getDataManager().loadPlayer(player.getUniqueId(), player.getName());
        }

        // Player placeholders
        line = line.replace("{player}", player.getName());
        line = line.replace("{displayname}", player.getDisplayName());
        line = line.replace("{world}", player.getWorld().getName());
        line = line.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        line = line.replace("{max_players}", String.valueOf(Bukkit.getMaxPlayers()));

        // Time placeholders
        line = line.replace("{time}", getFormattedTime());
        line = line.replace("{date}", getFormattedDate());

        // Currency placeholders
        for (Currency currency : plugin.getCurrencyManager().getAllCurrencies()) {
            String id = currency.getId();
            double balance = data.getBalance(currency);

            line = line.replace("{" + id + "}", currency.format(balance));
            line = line.replace("{" + id + "_raw}", String.valueOf((long) balance));
            line = line.replace("{" + id + "_formatted}", formatCompact(balance));
            line = line.replace("{" + id + "_icon}", getIconChar(currency));
        }

        // Rank placeholders (integrated with LuminaryRanks)
        line = line.replace("{rank}", getRank(player));
        line = line.replace("{prefix}", getPrefix(player));
        line = line.replace("{prestige}", getPrestige(player));
        line = line.replace("{rebirth}", getRebirth(player));

        // Group placeholders (integrated with LuminaryGroups)
        line = line.replace("{group}", getGroup(player));
        line = line.replace("{group_prefix}", getGroupPrefix(player));
        line = line.replace("{group_suffix}", getGroupSuffix(player));

        // Empty line placeholder
        if (line.trim().isEmpty() || line.equals("&r")) {
            line = " ";
        }

        return line;
    }

    private String getFormattedTime() {
        return String.format("%tH:%tM", System.currentTimeMillis(), System.currentTimeMillis());
    }

    private String getFormattedDate() {
        return String.format("%tD", System.currentTimeMillis());
    }

    private String formatCompact(double value) {
        if (value >= 1_000_000_000_000L) {
            return String.format("%.1fT", value / 1_000_000_000_000.0);
        } else if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000.0);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000.0);
        }
        return String.format("%.0f", value);
    }

    private String getIconChar(Currency currency) {
        // Map materials to unicode characters for display
        return switch (currency.getIcon()) {
            case GOLD_INGOT, GOLD_NUGGET -> "\u2B50"; // Star
            case BEACON -> "\u272A"; // Circled star
            case DIAMOND, EMERALD -> "\u2666"; // Diamond
            case NETHER_STAR -> "\u2605"; // Black star
            default -> "\u25CF"; // Bullet
        };
    }

    private String getRank(Player player) {
        // Try LuminaryRanks first
        if (ranksPlugin != null) {
            try {
                PlayerRankData rankData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (rankData != null) {
                    Rank rank = ranksPlugin.getRankManager().getRank(rankData.getCurrentRank());
                    if (rank != null) {
                        return rank.getDisplayName();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback to permission-based
        if (player.isOp()) return "&c&lOwner";
        if (player.hasPermission("group.admin")) return "&4Admin";
        if (player.hasPermission("group.mod")) return "&9Mod";
        if (player.hasPermission("group.vip")) return "&6VIP";
        return "&7Member";
    }

    private String getPrefix(Player player) {
        // Try LuminaryRanks first
        if (ranksPlugin != null) {
            try {
                PlayerRankData rankData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (rankData != null) {
                    Rank rank = ranksPlugin.getRankManager().getRank(rankData.getCurrentRank());
                    if (rank != null) {
                        return rank.getPrefix();
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private String getPrestige(Player player) {
        if (ranksPlugin != null) {
            try {
                PlayerRankData rankData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (rankData != null) {
                    int prestigeLevel = rankData.getPrestigeLevel();
                    if (prestigeLevel > 0) {
                        Prestige prestige = ranksPlugin.getRankManager().getPrestige(prestigeLevel);
                        if (prestige != null) {
                            return prestige.getDisplayName();
                        }
                        return "&5P" + prestigeLevel;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "&7None";
    }

    private String getRebirth(Player player) {
        if (ranksPlugin != null) {
            try {
                PlayerRankData rankData = ranksPlugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (rankData != null) {
                    int rebirthLevel = rankData.getRebirthLevel();
                    if (rebirthLevel > 0) {
                        Rebirth rebirth = ranksPlugin.getRankManager().getRebirth(rebirthLevel);
                        if (rebirth != null) {
                            return rebirth.getDisplayName();
                        }
                        return "&c\u2605" + rebirthLevel;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "&7None";
    }

    private String getGroup(Player player) {
        if (groupsPlugin != null) {
            try {
                Group group = groupsPlugin.getPlayerDataManager().getEffectiveGroup(player.getUniqueId());
                if (group != null) {
                    return group.getDisplayName();
                }
            } catch (Exception ignored) {
            }
        }
        return "&7Default";
    }

    private String getGroupPrefix(Player player) {
        if (groupsPlugin != null) {
            try {
                return groupsPlugin.getPlayerDataManager().getPrefix(player.getUniqueId());
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private String getGroupSuffix(Player player) {
        if (groupsPlugin != null) {
            try {
                return groupsPlugin.getPlayerDataManager().getSuffix(player.getUniqueId());
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    /**
     * Check if a player has a scoreboard.
     */
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }
}
