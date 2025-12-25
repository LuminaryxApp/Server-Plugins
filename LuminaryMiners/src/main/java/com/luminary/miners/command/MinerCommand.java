package com.luminary.miners.command;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.gui.MinerGUI;
import com.luminary.miners.miner.MinerType;
import com.luminary.miners.miner.PlayerMiner;
import com.luminary.miners.miner.ResourceType;
import com.luminary.miners.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Player command for interacting with miners.
 */
public class MinerCommand implements CommandExecutor, TabCompleter {

    private final LuminaryMiners plugin;

    public MinerCommand(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("luminaryminers.use")) {
            player.sendMessage(colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length == 0) {
            // Open main GUI
            new MinerGUI(plugin, player).openMainMenu();
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(player);
                break;

            case "list":
                listMiners(player);
                break;

            case "shop":
                new MinerGUI(plugin, player).openShop();
                break;

            case "collect":
                collectAll(player);
                break;

            case "stats":
                showStats(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /miners help for help.");
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== LuminaryMiners Help ===");
        player.sendMessage(ChatColor.YELLOW + "/miners" + ChatColor.GRAY + " - Open miner menu");
        player.sendMessage(ChatColor.YELLOW + "/miners list" + ChatColor.GRAY + " - List your miners");
        player.sendMessage(ChatColor.YELLOW + "/miners shop" + ChatColor.GRAY + " - Open miner shop");
        player.sendMessage(ChatColor.YELLOW + "/miners collect" + ChatColor.GRAY + " - Collect from all miners");
        player.sendMessage(ChatColor.YELLOW + "/miners stats" + ChatColor.GRAY + " - View production stats");
    }

    private void listMiners(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (data.getMinerCount() == 0) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any miners yet! Use /miners shop to purchase one.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Your Miners (" + data.getMinerCount() + ") ===");

        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) continue;

            String status = miner.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive";
            double stored = miner.getStoredResources();
            double maxStorage = type.getStorageCapacity(miner.getTier());

            player.sendMessage(ChatColor.YELLOW + "- " + colorize(type.getDisplayName()) +
                ChatColor.GRAY + " (Tier " + miner.getTier() + ") - " + status +
                ChatColor.GRAY + " - " + formatNumber(stored) + "/" + formatNumber(maxStorage));
        }
    }

    private void collectAll(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        double tokensCollected = 0;
        double beaconsCollected = 0;
        double gemsCollected = 0;

        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) continue;

            double collected = miner.collectResources();
            if (collected > 0) {
                ResourceType resource = type.getResourceType();
                switch (resource) {
                    case TOKENS:
                        tokensCollected += collected;
                        break;
                    case BEACONS:
                        beaconsCollected += collected;
                        break;
                    case GEMS:
                        gemsCollected += collected;
                        break;
                }
            }
        }

        // Add to economy
        if (tokensCollected > 0) {
            plugin.getEconomyHook().addBalance(player, "tokens", tokensCollected);
        }
        if (beaconsCollected > 0) {
            plugin.getEconomyHook().addBalance(player, "beacons", beaconsCollected);
        }
        if (gemsCollected > 0) {
            plugin.getEconomyHook().addBalance(player, "gems", gemsCollected);
        }

        // Send message
        if (tokensCollected > 0 || beaconsCollected > 0 || gemsCollected > 0) {
            StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Collected: ");
            if (tokensCollected > 0) msg.append(ChatColor.GOLD).append(formatNumber(tokensCollected)).append(" tokens ");
            if (beaconsCollected > 0) msg.append(ChatColor.AQUA).append(formatNumber(beaconsCollected)).append(" beacons ");
            if (gemsCollected > 0) msg.append(ChatColor.LIGHT_PURPLE).append(formatNumber(gemsCollected)).append(" gems");
            player.sendMessage(msg.toString().trim());
        } else {
            player.sendMessage(ChatColor.YELLOW + "Nothing to collect! Your miners are still working.");
        }

        plugin.getPlayerDataManager().savePlayerData(player);
    }

    private void showStats(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        double tokensPerHour = 0;
        double beaconsPerHour = 0;
        double gemsPerHour = 0;

        for (PlayerMiner miner : data.getMiners()) {
            if (!miner.isActive()) continue;

            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) continue;

            double production = type.getProductionPerHour(miner.getTier());
            switch (type.getResourceType()) {
                case TOKENS:
                    tokensPerHour += production;
                    break;
                case BEACONS:
                    beaconsPerHour += production;
                    break;
                case GEMS:
                    gemsPerHour += production;
                    break;
            }
        }

        player.sendMessage(ChatColor.GOLD + "=== Your Production Stats ===");
        player.sendMessage(ChatColor.YELLOW + "Active Miners: " + ChatColor.WHITE + data.getMinerCount());
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Production per Hour:");
        player.sendMessage(ChatColor.GOLD + "  Tokens: " + ChatColor.WHITE + formatNumber(tokensPerHour));
        player.sendMessage(ChatColor.AQUA + "  Beacons: " + ChatColor.WHITE + formatNumber(beaconsPerHour));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  Gems: " + ChatColor.WHITE + formatNumber(gemsPerHour));
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Production per Day:");
        player.sendMessage(ChatColor.GOLD + "  Tokens: " + ChatColor.WHITE + formatNumber(tokensPerHour * 24));
        player.sendMessage(ChatColor.AQUA + "  Beacons: " + ChatColor.WHITE + formatNumber(beaconsPerHour * 24));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  Gems: " + ChatColor.WHITE + formatNumber(gemsPerHour * 24));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "list", "shop", "collect", "stats");
            String input = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String formatNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000);
        } else {
            return String.format("%.0f", number);
        }
    }
}
