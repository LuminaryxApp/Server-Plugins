package com.luminary.economy.command;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.data.PlayerData;
import com.luminary.economy.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Currency-specific balance commands (/tokens, /beacons, /gems).
 */
public class BalanceCommand implements CommandExecutor, TabCompleter {

    private final LuminaryEconomy plugin;

    public BalanceCommand(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Determine which currency based on command name
        String currencyId = getCurrencyFromCommand(label);
        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);

        if (currency == null) {
            sender.sendMessage(TextUtil.colorize("&cCurrency not configured: " + currencyId));
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("player-not-found", "{player}", args[0])));
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(TextUtil.colorize("&cUsage: /" + label + " <player>"));
            return true;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(TextUtil.colorize("&cNo data found for that player."));
            return true;
        }

        double balance = data.getBalance(currency);

        if (target.equals(sender)) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("balance.self",
                    "{currency}", currency.getDisplayName(),
                    "{amount}", currency.format(balance),
                    "{color}", currency.getColor())));
        } else {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("balance.other",
                    "{player}", target.getName(),
                    "{currency}", currency.getDisplayName(),
                    "{amount}", currency.format(balance),
                    "{color}", currency.getColor())));
        }

        return true;
    }

    private String getCurrencyFromCommand(String command) {
        return switch (command.toLowerCase()) {
            case "tokens", "token" -> "tokens";
            case "beacons", "beacon" -> "beacons";
            case "gems", "gem" -> "gems";
            default -> command.toLowerCase();
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
            String prefix = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        }

        return completions;
    }
}
