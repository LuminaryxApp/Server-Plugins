package com.luminary.economy.scoreboard;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.data.PlayerData;
import com.luminary.economy.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    public ScoreboardManager(LuminaryEconomy plugin) {
        this.plugin = plugin;
        loadConfig();
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

            // Ensure unique entries
            while (usedEntries.contains(parsed)) {
                parsed = parsed + " ";
            }
            usedEntries.add(parsed);

            // Truncate if too long (scoreboard entry limit)
            if (parsed.length() > 40) {
                parsed = parsed.substring(0, 40);
            }

            objective.getScore(parsed).setScore(score);
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

        // Rank placeholder (would integrate with LuckPerms/Vault if available)
        line = line.replace("{rank}", getRank(player));
        line = line.replace("{prefix}", getPrefix(player));

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
        // Would integrate with LuckPerms/Vault
        // For now, return a default
        if (player.isOp()) return "&c&lOwner";
        if (player.hasPermission("group.admin")) return "&4Admin";
        if (player.hasPermission("group.mod")) return "&9Mod";
        if (player.hasPermission("group.vip")) return "&6VIP";
        return "&7Member";
    }

    private String getPrefix(Player player) {
        // Would integrate with LuckPerms/Vault
        return "";
    }

    /**
     * Check if a player has a scoreboard.
     */
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }
}
