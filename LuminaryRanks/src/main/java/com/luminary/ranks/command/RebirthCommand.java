package com.luminary.ranks.command;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Rebirth;
import com.luminary.ranks.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RebirthCommand implements CommandExecutor {

    private final LuminaryRanks plugin;

    public RebirthCommand(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        if (!player.hasPermission("luminaryranks.rebirth")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        rebirth(player);
        return true;
    }

    private void rebirth(Player player) {
        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                player.getUniqueId(), player.getName());

        // Check if at max prestige
        if (!plugin.getRankManager().isMaxPrestige(data.getPrestigeLevel())) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rebirth.not-max-prestige",
                            "{required}", String.valueOf(plugin.getRankManager().getMaxPrestige()),
                            "{current}", String.valueOf(data.getPrestigeLevel()))));
            return;
        }

        // Check if at max rank (must be at Z to prestige)
        if (!plugin.getRankManager().isMaxRank(data.getCurrentRank())) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rebirth.not-max-rank")));
            return;
        }

        // Check if at max rebirth
        if (plugin.getRankManager().isMaxRebirth(data.getRebirthLevel())) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rebirth.max-rebirth")));
            return;
        }

        // Get next rebirth
        Rebirth nextRebirth = plugin.getRankManager().getNextRebirth(data.getRebirthLevel());
        if (nextRebirth == null) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rebirth.max-rebirth")));
            return;
        }

        // Check balance
        long cost = nextRebirth.getCost();
        if (!plugin.getEconomyHook().hasBalance(player, cost)) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("rebirth.insufficient-funds",
                            "{cost}", TextUtil.formatNumber(cost),
                            "{balance}", TextUtil.formatNumber(plugin.getEconomyHook().getBalance(player)))));
            return;
        }

        // Withdraw and rebirth
        if (!plugin.getEconomyHook().withdraw(player, cost)) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("error.transaction-failed")));
            return;
        }

        int oldRebirth = data.getRebirthLevel();
        data.rebirth(cost);
        plugin.getPlayerDataManager().savePlayer(player.getUniqueId());

        // Execute rebirth commands
        for (String cmd : nextRebirth.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    cmd.replace("{player}", player.getName())
                       .replace("{rebirth}", String.valueOf(nextRebirth.getLevel())));
        }

        // Send success message
        player.sendMessage(TextUtil.colorize(
                plugin.getConfigManager().getMessage("rebirth.success",
                        "{old_rebirth}", String.valueOf(oldRebirth),
                        "{new_rebirth}", String.valueOf(nextRebirth.getLevel()),
                        "{multiplier}", TextUtil.formatMultiplier(nextRebirth.getPermanentMultiplier()),
                        "{cost}", TextUtil.formatNumber(cost))));

        // Broadcast if enabled
        if (plugin.getConfigManager().isBroadcastRebirthEnabled()) {
            String broadcast = plugin.getConfigManager().getRawMessage("rebirth.broadcast");
            broadcast = broadcast.replace("{player}", player.getName())
                                 .replace("{rebirth}", nextRebirth.getDisplayName());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(TextUtil.colorize(broadcast));
            }
        }
    }
}
