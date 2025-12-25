package com.luminary.ranks.command;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankupCommand implements CommandExecutor {

    private final LuminaryRanks plugin;

    public RankupCommand(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        if (!player.hasPermission("luminaryranks.rankup")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Handle max rankup
        if (args.length > 0 && args[0].equalsIgnoreCase("max")) {
            rankUpMax(player);
            return true;
        }

        rankUp(player);
        return true;
    }

    private void rankUp(Player player) {
        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                player.getUniqueId(), player.getName());

        Rank currentRank = plugin.getRankManager().getRank(data.getCurrentRank());
        Rank nextRank = plugin.getRankManager().getNextRank(data.getCurrentRank());

        // Check if at max rank
        if (nextRank == null) {
            if (plugin.getRankManager().isMaxPrestige(data.getPrestigeLevel())) {
                // At max prestige too
                if (plugin.getRankManager().isMaxRebirth(data.getRebirthLevel())) {
                    player.sendMessage(TextUtil.colorize(
                            plugin.getConfigManager().getMessage("rankup.max-everything")));
                } else {
                    player.sendMessage(TextUtil.colorize(
                            plugin.getConfigManager().getMessage("rankup.max-rank-can-rebirth")));
                }
            } else {
                player.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("rankup.max-rank-can-prestige")));
            }
            return;
        }

        // Check balance
        long cost = nextRank.getCost();
        if (!plugin.getEconomyHook().hasBalance(player, cost)) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rankup.insufficient-funds",
                            "{cost}", TextUtil.formatNumber(cost),
                            "{balance}", TextUtil.formatNumber(plugin.getEconomyHook().getBalance(player)))));
            return;
        }

        // Withdraw and rank up
        if (!plugin.getEconomyHook().withdraw(player, cost)) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("error.transaction-failed")));
            return;
        }

        data.rankUp(nextRank.getId(), cost);
        plugin.getPlayerDataManager().savePlayer(player.getUniqueId());

        // Execute rank commands
        for (String cmd : nextRank.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    cmd.replace("{player}", player.getName())
                       .replace("{rank}", nextRank.getId()));
        }

        // Send success message
        player.sendMessage(TextUtil.colorize(
                plugin.getConfigManager().getMessage("rankup.success",
                        "{old_rank}", currentRank != null ? currentRank.getDisplayName() : "None",
                        "{new_rank}", nextRank.getDisplayName(),
                        "{cost}", TextUtil.formatNumber(cost))));

        // Broadcast if enabled
        if (plugin.getConfigManager().isBroadcastEnabled()) {
            String broadcast = plugin.getConfigManager().getRawMessage("rankup.broadcast");
            broadcast = broadcast.replace("{player}", player.getName())
                                 .replace("{rank}", nextRank.getDisplayName());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(TextUtil.colorize(broadcast));
            }
        }
    }

    private void rankUpMax(Player player) {
        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                player.getUniqueId(), player.getName());

        int rankupsCompleted = 0;
        long totalCost = 0;

        while (true) {
            Rank nextRank = plugin.getRankManager().getNextRank(data.getCurrentRank());
            if (nextRank == null) break;

            long cost = nextRank.getCost();
            if (!plugin.getEconomyHook().hasBalance(player, cost)) break;

            if (!plugin.getEconomyHook().withdraw(player, cost)) break;

            data.rankUp(nextRank.getId(), cost);
            totalCost += cost;
            rankupsCompleted++;

            // Execute commands
            for (String cmd : nextRank.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("{player}", player.getName())
                           .replace("{rank}", nextRank.getId()));
            }
        }

        if (rankupsCompleted == 0) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rankup.cannot-afford-next")));
            return;
        }

        plugin.getPlayerDataManager().savePlayer(player.getUniqueId());

        Rank currentRank = plugin.getRankManager().getRank(data.getCurrentRank());
        player.sendMessage(TextUtil.colorize(
                plugin.getConfigManager().getMessage("rankup.max-success",
                        "{count}", String.valueOf(rankupsCompleted),
                        "{rank}", currentRank.getDisplayName(),
                        "{cost}", TextUtil.formatNumber(totalCost))));
    }
}
