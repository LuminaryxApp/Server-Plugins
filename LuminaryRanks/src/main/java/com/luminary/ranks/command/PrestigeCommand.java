package com.luminary.ranks.command;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Prestige;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCommand implements CommandExecutor {

    private final LuminaryRanks plugin;

    public PrestigeCommand(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        if (!player.hasPermission("luminaryranks.prestige")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        prestige(player);
        return true;
    }

    private void prestige(Player player) {
        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                player.getUniqueId(), player.getName());

        // Check if at max rank
        if (!plugin.getRankManager().isMaxRank(data.getCurrentRank())) {
            Rank lastRank = plugin.getRankManager().getLastRank();
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("prestige.not-max-rank",
                            "{required_rank}", lastRank != null ? lastRank.getDisplayName() : "Z")));
            return;
        }

        // Check if at max prestige
        if (plugin.getRankManager().isMaxPrestige(data.getPrestigeLevel())) {
            if (plugin.getRankManager().isMaxRebirth(data.getRebirthLevel())) {
                player.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("prestige.max-everything")));
            } else {
                player.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("prestige.max-can-rebirth")));
            }
            return;
        }

        // Get next prestige
        Prestige nextPrestige = plugin.getRankManager().getNextPrestige(data.getPrestigeLevel());
        if (nextPrestige == null) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("prestige.max-prestige")));
            return;
        }

        // Check balance
        long cost = nextPrestige.getCost();
        if (!plugin.getEconomyHook().hasBalance(player, cost)) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("prestige.insufficient-funds",
                            "{cost}", TextUtil.formatNumber(cost),
                            "{balance}", TextUtil.formatNumber(plugin.getEconomyHook().getBalance(player)))));
            return;
        }

        // Withdraw and prestige
        if (!plugin.getEconomyHook().withdraw(player, cost)) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("error.transaction-failed")));
            return;
        }

        int oldPrestige = data.getPrestigeLevel();
        data.prestige(cost);
        plugin.getPlayerDataManager().savePlayer(player.getUniqueId());

        // Execute prestige commands
        for (String cmd : nextPrestige.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    cmd.replace("{player}", player.getName())
                       .replace("{prestige}", String.valueOf(nextPrestige.getLevel())));
        }

        // Send success message
        player.sendMessage(TextUtil.colorize(
                plugin.getConfigManager().getMessage("prestige.success",
                        "{old_prestige}", String.valueOf(oldPrestige),
                        "{new_prestige}", String.valueOf(nextPrestige.getLevel()),
                        "{multiplier}", TextUtil.formatMultiplier(nextPrestige.getMultiplier()),
                        "{cost}", TextUtil.formatNumber(cost))));

        // Broadcast if enabled
        if (plugin.getConfigManager().isBroadcastPrestigeEnabled()) {
            String broadcast = plugin.getConfigManager().getRawMessage("prestige.broadcast");
            broadcast = broadcast.replace("{player}", player.getName())
                                 .replace("{prestige}", nextPrestige.getDisplayName());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(TextUtil.colorize(broadcast));
            }
        }
    }
}
