package com.luminary.economy.command;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pay command for transferring currency between players.
 */
public class PayCommand implements CommandExecutor, TabCompleter {

    private final LuminaryEconomy plugin;

    public PayCommand(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (!plugin.getConfigManager().isPayEnabled()) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.disabled")));
            return true;
        }

        if (!player.hasPermission("luminaryeconomy.pay")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(TextUtil.colorize("&cUsage: /pay <player> <amount> [currency]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[0])));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.self")));
            return true;
        }

        double amount;
        try {
            amount = TextUtil.parseAmount(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[1]));
            return true;
        }

        // Get currency (default if not specified)
        Currency currency;
        if (args.length >= 3) {
            currency = plugin.getCurrencyManager().findCurrency(args[2]);
            if (currency == null) {
                player.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[2]));
                return true;
            }
        } else {
            currency = plugin.getCurrencyManager().getDefaultCurrency();
        }

        // Check if currency is payable
        if (!currency.isPayable()) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.not-payable",
                    "{currency}", currency.getDisplayName())));
            return true;
        }

        // Check minimum amount
        double minimum = plugin.getConfigManager().getPayMinimum();
        if (amount < minimum) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.minimum",
                    "{minimum}", currency.format(minimum))));
            return true;
        }

        // Check if player has enough
        if (!plugin.getAPI().hasBalance(player, currency.getId(), amount)) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.insufficient",
                    "{currency}", currency.getDisplayName())));
            return true;
        }

        // Calculate tax
        double taxPercent = plugin.getConfigManager().getPayTax();
        double tax = amount * (taxPercent / 100.0);
        double received = amount - tax;

        // Perform transfer
        plugin.getAPI().removeBalance(player, currency.getId(), amount);
        plugin.getAPI().addBalance(target, currency.getId(), received);

        // Send messages
        if (tax > 0) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.sent-with-tax",
                    "{amount}", currency.format(amount),
                    "{received}", currency.format(received),
                    "{tax}", currency.format(tax),
                    "{currency}", currency.getDisplayName(),
                    "{player}", target.getName())));
        } else {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.sent",
                    "{amount}", currency.format(amount),
                    "{currency}", currency.getDisplayName(),
                    "{player}", target.getName())));
        }

        target.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("pay.received",
                "{amount}", currency.format(received),
                "{currency}", currency.getDisplayName(),
                "{player}", player.getName())));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(sender)) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 2) {
            completions.addAll(Arrays.asList("100", "1000", "10000", "100000"));
        } else if (args.length == 3) {
            for (Currency currency : plugin.getCurrencyManager().getAllCurrencies()) {
                if (currency.isPayable()) {
                    completions.add(currency.getId());
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
