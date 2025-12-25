package com.luminary.economy.command;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.data.PlayerData;
import com.luminary.economy.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to toggle the scoreboard sidebar.
 */
public class ScoreboardCommand implements CommandExecutor {

    private final LuminaryEconomy plugin;

    public ScoreboardCommand(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (!player.hasPermission("luminaryeconomy.scoreboard")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        plugin.getScoreboardManager().toggleScoreboard(player);

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            if (data.isScoreboardEnabled()) {
                player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("scoreboard.enabled")));
            } else {
                player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("scoreboard.disabled")));
            }
        }

        return true;
    }
}
