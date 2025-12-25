package com.luminary.backpacks.command;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackpackAdminCommand implements CommandExecutor, TabCompleter {

    private final LuminaryBackpacks plugin;

    public BackpackAdminCommand(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarybackpacks.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> showHelp(sender);
            case "settier" -> setTier(sender, args);
            case "reset" -> resetPlayer(sender, args);
            case "clear" -> clearBackpack(sender, args);
            case "view" -> viewBackpack(sender, args);
            case "reload" -> reloadConfig(sender);
            default -> showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sendMessage(sender, "&6&l━━━━━ Backpack Admin ━━━━━");
        sendMessage(sender, "&e/bpadmin settier <player> <tier> &7- Set player tier");
        sendMessage(sender, "&e/bpadmin reset <player> &7- Reset player backpack");
        sendMessage(sender, "&e/bpadmin clear <player> &7- Clear player backpack");
        sendMessage(sender, "&e/bpadmin view <player> &7- View player backpack");
        sendMessage(sender, "&e/bpadmin reload &7- Reload configuration");
        sendMessage(sender, "&6&l━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void setTier(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /bpadmin settier <player> <tier>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        int tier;
        try {
            tier = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sendMessage(sender, plugin.getConfigManager().getMessage("invalid-tier"));
            return;
        }

        int maxTier = plugin.getConfigManager().getMaxTier();
        if (tier < 1 || tier > maxTier) {
            sendMessage(sender, plugin.getConfigManager().getMessage("invalid-tier"));
            return;
        }

        plugin.getBackpackManager().setTier(target.getUniqueId(), tier);
        String tierName = plugin.getConfigManager().getTierName(tier);

        sendMessage(sender, plugin.getConfigManager().getMessage("tier-given",
                "{tier}", tierName, "{player}", target.getName()));
        sendMessage(target, plugin.getConfigManager().getMessage("tier-set",
                "{tier}", tierName));
    }

    private void resetPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /bpadmin reset <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        plugin.getPlayerDataManager().resetPlayer(target.getUniqueId());
        sendMessage(sender, plugin.getConfigManager().getMessage("tier-reset",
                "{player}", target.getName()));
    }

    private void clearBackpack(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /bpadmin clear <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        plugin.getBackpackManager().clearBackpack(target.getUniqueId());
        sendMessage(sender, "&aCleared " + target.getName() + "'s backpack!");
    }

    private void viewBackpack(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "&cThis command can only be used by players!");
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /bpadmin view <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sendMessage(sender, "&cCould not load player data!");
            return;
        }

        // Open the target's backpack for viewing
        int tier = data.getTier();
        String tierName = plugin.getConfigManager().getTierName(tier);
        String tierColor = plugin.getConfigManager().getTierColor(tier);

        String title = tierColor + target.getName() + "'s " + tierName;
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, data.getSize(),
                LegacyComponentSerializer.legacyAmpersand().deserialize(title));
        inv.setContents(data.getContents());

        player.openInventory(inv);
    }

    private void reloadConfig(CommandSender sender) {
        plugin.reload();
        sendMessage(sender, plugin.getConfigManager().getMessage("admin-reload"));
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("luminarybackpacks.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "settier", "reset", "clear", "view", "reload"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("settier") || sub.equals("reset") || sub.equals("clear") || sub.equals("view")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settier")) {
                int maxTier = plugin.getConfigManager().getMaxTier();
                for (int i = 1; i <= maxTier; i++) {
                    completions.add(String.valueOf(i));
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
