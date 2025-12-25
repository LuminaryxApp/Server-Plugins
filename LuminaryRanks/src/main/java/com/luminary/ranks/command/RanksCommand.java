package com.luminary.ranks.command;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.gui.RanksGUI;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RanksCommand implements CommandExecutor, TabCompleter {

    private final LuminaryRanks plugin;
    private final RanksGUI gui;

    public RanksCommand(LuminaryRanks plugin) {
        this.plugin = plugin;
        this.gui = new RanksGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open GUI for players
            if (sender instanceof Player player) {
                gui.openMainMenu(player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "info" -> handleInfo(sender, args);
            case "list" -> handleList(sender);
            case "admin" -> handleAdmin(sender, args);
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            case "reload" -> handleReload(sender);
            default -> {
                if (sender instanceof Player player) {
                    gui.openMainMenu(player);
                } else {
                    sendHelp(sender);
                }
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Ranks Help ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&e/ranks &7- Open ranks menu"));
        sender.sendMessage(TextUtil.colorize("&e/ranks info [player] &7- View rank info"));
        sender.sendMessage(TextUtil.colorize("&e/ranks list &7- List all ranks"));
        sender.sendMessage(TextUtil.colorize("&e/rankup [max] &7- Rank up (or max ranks)"));
        sender.sendMessage(TextUtil.colorize("&e/prestige &7- Prestige (resets rank)"));
        sender.sendMessage(TextUtil.colorize("&e/rebirth &7- Rebirth (resets everything)"));

        if (sender.hasPermission("luminaryranks.admin")) {
            sender.sendMessage(TextUtil.colorize("&6&lAdmin Commands:"));
            sender.sendMessage(TextUtil.colorize("&e/ranks set <player> <rank/prestige/rebirth> <value>"));
            sender.sendMessage(TextUtil.colorize("&e/ranks reset <player> &7- Reset player progress"));
            sender.sendMessage(TextUtil.colorize("&e/ranks reload &7- Reload configuration"));
        }
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(TextUtil.colorize("&cUsage: /ranks info <player>"));
            return;
        }

        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                target.getUniqueId(), target.getName());

        Rank currentRank = plugin.getRankManager().getRank(data.getCurrentRank());
        Rank nextRank = plugin.getRankManager().getNextRank(data.getCurrentRank());

        double multiplier = plugin.getRankManager().getMultiplier(
                data.getPrestigeLevel(), data.getRebirthLevel());

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ " + target.getName() + "'s Rank ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&eRank: " + (currentRank != null ? currentRank.getDisplayName() : "&7None")));
        sender.sendMessage(TextUtil.colorize("&ePrestige: &d" + data.getPrestigeLevel() + "&7/" + plugin.getRankManager().getMaxPrestige()));
        sender.sendMessage(TextUtil.colorize("&eRebirth: &c" + data.getRebirthLevel() + "&7/" + plugin.getRankManager().getMaxRebirth()));
        sender.sendMessage(TextUtil.colorize("&eMultiplier: &a" + TextUtil.formatMultiplier(multiplier)));

        if (nextRank != null) {
            long cost = nextRank.getCost();
            long balance = plugin.getEconomyHook().getBalance(target);
            String canAfford = balance >= cost ? "&a(Can afford)" : "&c(Cannot afford)";
            sender.sendMessage(TextUtil.colorize("&eNext Rank: " + nextRank.getDisplayName() +
                    " &7- &f" + TextUtil.formatNumber(cost) + " " + canAfford));
        } else {
            sender.sendMessage(TextUtil.colorize("&eNext Rank: &7Max rank reached!"));
        }

        sender.sendMessage(TextUtil.colorize("&7Total Rankups: &f" + data.getTotalRankups()));
        sender.sendMessage(TextUtil.colorize("&7Tokens Spent: &f" + TextUtil.formatNumber(data.getTokensSpent())));
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ All Ranks ━━━━━"));

        List<Rank> ranks = plugin.getRankManager().getAllRanks();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ranks.size(); i++) {
            Rank rank = ranks.get(i);
            sb.append(rank.getDisplayName());
            if (i < ranks.size() - 1) {
                sb.append(" &8→ ");
            }
            if ((i + 1) % 13 == 0) {
                sender.sendMessage(TextUtil.colorize(sb.toString()));
                sb = new StringBuilder();
            }
        }

        if (sb.length() > 0) {
            sender.sendMessage(TextUtil.colorize(sb.toString()));
        }

        sender.sendMessage(TextUtil.colorize("&7Total: &f" + ranks.size() + " ranks, " +
                plugin.getRankManager().getMaxPrestige() + " prestiges, " +
                plugin.getRankManager().getMaxRebirth() + " rebirths"));
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryranks.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        sendHelp(sender);
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryranks.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /ranks set <player> <rank/prestige/rebirth> <value>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("player-not-found", "{player}", args[1])));
            return;
        }

        String type = args[2].toLowerCase();
        String value = args[3];

        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                target.getUniqueId(), target.getName() != null ? target.getName() : args[1]);

        switch (type) {
            case "rank" -> {
                Rank rank = plugin.getRankManager().getRank(value);
                if (rank == null) {
                    sender.sendMessage(TextUtil.colorize("&cInvalid rank: " + value));
                    return;
                }
                data.setCurrentRank(rank.getId());
                sender.sendMessage(TextUtil.colorize("&aSet " + args[1] + "'s rank to " + rank.getDisplayName()));
            }
            case "prestige" -> {
                try {
                    int level = Integer.parseInt(value);
                    if (level < 0 || level > plugin.getRankManager().getMaxPrestige()) {
                        sender.sendMessage(TextUtil.colorize("&cInvalid prestige level: " + value));
                        return;
                    }
                    data.setPrestigeLevel(level);
                    sender.sendMessage(TextUtil.colorize("&aSet " + args[1] + "'s prestige to " + level));
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtil.colorize("&cInvalid number: " + value));
                }
            }
            case "rebirth" -> {
                try {
                    int level = Integer.parseInt(value);
                    if (level < 0 || level > plugin.getRankManager().getMaxRebirth()) {
                        sender.sendMessage(TextUtil.colorize("&cInvalid rebirth level: " + value));
                        return;
                    }
                    data.setRebirthLevel(level);
                    sender.sendMessage(TextUtil.colorize("&aSet " + args[1] + "'s rebirth to " + level));
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtil.colorize("&cInvalid number: " + value));
                }
            }
            default -> sender.sendMessage(TextUtil.colorize("&cInvalid type. Use: rank, prestige, or rebirth"));
        }

        plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryranks.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /ranks reset <player>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                target.getUniqueId(), target.getName() != null ? target.getName() : args[1]);

        data.setCurrentRank("A");
        data.setPrestigeLevel(0);
        data.setRebirthLevel(0);
        plugin.getPlayerDataManager().savePlayer(target.getUniqueId());

        sender.sendMessage(TextUtil.colorize("&aReset " + args[1] + "'s rank progress!"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("luminaryranks.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        plugin.reload();
        sender.sendMessage(TextUtil.colorize("&aLuminaryRanks configuration reloaded!"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "info", "list"));
            if (sender.hasPermission("luminaryranks.admin")) {
                completions.addAll(Arrays.asList("set", "reset", "reload"));
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("info") || sub.equals("set") || sub.equals("reset")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("rank", "prestige", "rebirth"));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            String type = args[2].toLowerCase();
            if (type.equals("rank")) {
                for (Rank rank : plugin.getRankManager().getAllRanks()) {
                    completions.add(rank.getId());
                }
            } else if (type.equals("prestige")) {
                for (int i = 0; i <= plugin.getRankManager().getMaxPrestige(); i++) {
                    completions.add(String.valueOf(i));
                }
            } else if (type.equals("rebirth")) {
                for (int i = 0; i <= plugin.getRankManager().getMaxRebirth(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
