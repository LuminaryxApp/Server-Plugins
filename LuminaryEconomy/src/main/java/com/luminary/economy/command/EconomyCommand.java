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

import java.util.*;

/**
 * Main economy admin command.
 */
public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final LuminaryEconomy plugin;

    public EconomyCommand(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no args and player, show balances
        if (args.length == 0) {
            if (sender instanceof Player player) {
                showBalances(player, player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "bal", "balance" -> handleBalance(sender, args);
            case "give", "add" -> handleGive(sender, args);
            case "take", "remove" -> handleTake(sender, args);
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            case "top", "leaderboard" -> handleTop(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ LuminaryEconomy Help ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&e/bal [player] &7- View balances"));
        sender.sendMessage(TextUtil.colorize("&e/tokens, /beacons, /gems &7- View specific currency"));
        sender.sendMessage(TextUtil.colorize("&e/pay <player> <amount> [currency] &7- Pay another player"));
        sender.sendMessage(TextUtil.colorize("&e/scoreboard &7- Toggle scoreboard"));

        if (sender.hasPermission("luminaryeconomy.admin")) {
            sender.sendMessage(TextUtil.colorize("&6&lAdmin Commands:"));
            sender.sendMessage(TextUtil.colorize("&e/eco give <player> <currency> <amount> &7- Give currency"));
            sender.sendMessage(TextUtil.colorize("&e/eco take <player> <currency> <amount> &7- Take currency"));
            sender.sendMessage(TextUtil.colorize("&e/eco set <player> <currency> <amount> &7- Set balance"));
            sender.sendMessage(TextUtil.colorize("&e/eco reset <player> [currency] &7- Reset balance"));
            sender.sendMessage(TextUtil.colorize("&e/eco top [currency] &7- View leaderboard"));
            sender.sendMessage(TextUtil.colorize("&e/eco reload &7- Reload configuration"));
        }
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleBalance(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(TextUtil.colorize("&cUsage: /eco balance <player>"));
            return;
        }

        showBalances(sender, target);
    }

    private void showBalances(CommandSender sender, Player target) {
        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(TextUtil.colorize("&cNo data found for that player."));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ " + target.getName() + "'s Balances ━━━━━"));
        for (Currency currency : plugin.getCurrencyManager().getAllCurrencies()) {
            double balance = data.getBalance(currency);
            sender.sendMessage(TextUtil.colorize(currency.getColor() + currency.getDisplayName() +
                    "&7: &f" + currency.format(balance)));
        }
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryeconomy.admin.give")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /eco give <player> <currency> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
            return;
        }

        Currency currency = plugin.getCurrencyManager().findCurrency(args[2]);
        if (currency == null) {
            sender.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[2]));
            return;
        }

        double amount;
        try {
            amount = TextUtil.parseAmount(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[3]));
            return;
        }

        double newBalance = plugin.getAPI().addBalance(target, currency.getId(), amount);

        sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("admin.gave",
                "{amount}", currency.format(amount),
                "{currency}", currency.getDisplayName(),
                "{player}", target.getName(),
                "{new_balance}", currency.format(newBalance))));

        target.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("received",
                "{amount}", currency.format(amount),
                "{currency}", currency.getDisplayName())));
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryeconomy.admin.take")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /eco take <player> <currency> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
            return;
        }

        Currency currency = plugin.getCurrencyManager().findCurrency(args[2]);
        if (currency == null) {
            sender.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[2]));
            return;
        }

        double amount;
        try {
            amount = TextUtil.parseAmount(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[3]));
            return;
        }

        boolean success = plugin.getAPI().removeBalance(target, currency.getId(), amount);
        if (!success) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have enough " + currency.getDisplayName()));
            return;
        }

        double newBalance = plugin.getAPI().getBalance(target, currency.getId());

        sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("admin.took",
                "{amount}", currency.format(amount),
                "{currency}", currency.getDisplayName(),
                "{player}", target.getName(),
                "{new_balance}", currency.format(newBalance))));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryeconomy.admin.set")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /eco set <player> <currency> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
            return;
        }

        Currency currency = plugin.getCurrencyManager().findCurrency(args[2]);
        if (currency == null) {
            sender.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[2]));
            return;
        }

        double amount;
        try {
            amount = TextUtil.parseAmount(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[3]));
            return;
        }

        plugin.getAPI().setBalance(target, currency.getId(), amount);

        sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("admin.set",
                "{amount}", currency.format(amount),
                "{currency}", currency.getDisplayName(),
                "{player}", target.getName())));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryeconomy.admin.reset")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /eco reset <player> [currency]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(TextUtil.colorize("&cNo data found for that player."));
            return;
        }

        if (args.length >= 3) {
            Currency currency = plugin.getCurrencyManager().findCurrency(args[2]);
            if (currency == null) {
                sender.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[2]));
                return;
            }
            data.resetBalance(currency.getId());
            sender.sendMessage(TextUtil.colorize("&aReset " + target.getName() + "'s " +
                    currency.getDisplayName() + " balance."));
        } else {
            data.resetBalances();
            sender.sendMessage(TextUtil.colorize("&aReset all balances for " + target.getName()));
        }
    }

    private void handleTop(CommandSender sender, String[] args) {
        Currency currency;
        if (args.length >= 2) {
            currency = plugin.getCurrencyManager().findCurrency(args[1]);
            if (currency == null) {
                sender.sendMessage(TextUtil.colorize("&cCurrency not found: " + args[1]));
                return;
            }
        } else {
            currency = plugin.getCurrencyManager().getDefaultCurrency();
        }

        List<PlayerData> top = plugin.getDataManager().getTopPlayers(currency.getId(), 10);

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Top " + currency.getDisplayName() + " ━━━━━"));
        int rank = 1;
        for (PlayerData data : top) {
            String color = rank <= 3 ? "&e" : "&7";
            sender.sendMessage(TextUtil.colorize(color + "#" + rank + " &f" + data.getPlayerName() +
                    " &7- " + currency.formatColored(data.getBalance(currency))));
            rank++;
        }
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("luminaryeconomy.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        plugin.reload();
        sender.sendMessage(TextUtil.colorize("&aLuminaryEconomy configuration reloaded!"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "balance", "top"));
            if (sender.hasPermission("luminaryeconomy.admin")) {
                completions.addAll(Arrays.asList("give", "take", "set", "reset", "reload"));
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("balance") || sub.equals("give") || sub.equals("take") ||
                    sub.equals("set") || sub.equals("reset")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (sub.equals("top")) {
                completions.addAll(plugin.getCurrencyManager().getCurrencyIds());
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set") || sub.equals("reset")) {
                completions.addAll(plugin.getCurrencyManager().getCurrencyIds());
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                completions.addAll(Arrays.asList("100", "1000", "10000", "100000", "1M", "10M"));
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
