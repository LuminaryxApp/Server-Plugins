package com.luminary.miners.command;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.miner.*;
import com.luminary.miners.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin commands for managing miners.
 */
public class MinerAdminCommand implements CommandExecutor, TabCompleter {

    private final LuminaryMiners plugin;

    public MinerAdminCommand(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminaryminers.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;

            case "reload":
                plugin.reload();
                sender.sendMessage(ChatColor.GREEN + "LuminaryMiners configuration reloaded!");
                break;

            case "list":
                listMinerTypes(sender);
                break;

            case "give":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin give <player> <minertype>");
                    return true;
                }
                giveMiner(sender, args[1], args[2]);
                break;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin remove <player> [minertype]");
                    return true;
                }
                removeMiner(sender, args[1], args.length > 2 ? args[2] : null);
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin info <minertype>");
                    return true;
                }
                showMinerInfo(sender, args[1]);
                break;

            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin create <id>");
                    return true;
                }
                createMinerType(sender, args[1]);
                break;

            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin delete <minertype>");
                    return true;
                }
                deleteMinerType(sender, args[1]);
                break;

            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin set <minertype> <property> <value>");
                    return true;
                }
                setMinerProperty(sender, args[1], args[2], args[3]);
                break;

            case "player":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mineradmin player <player>");
                    return true;
                }
                showPlayerInfo(sender, args[1]);
                break;

            case "forcecycle":
                plugin.getProductionTask().forceCycle();
                sender.sendMessage(ChatColor.GREEN + "Forced production cycle for all players.");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /mineradmin help for help.");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LuminaryMiners Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin reload" + ChatColor.GRAY + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin list" + ChatColor.GRAY + " - List all miner types");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin info <type>" + ChatColor.GRAY + " - Show miner type info");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin give <player> <type>" + ChatColor.GRAY + " - Give a miner");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin remove <player> [type]" + ChatColor.GRAY + " - Remove miners");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin player <player>" + ChatColor.GRAY + " - View player's miners");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin create <id>" + ChatColor.GRAY + " - Create new miner type");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin delete <type>" + ChatColor.GRAY + " - Delete miner type");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin set <type> <prop> <val>" + ChatColor.GRAY + " - Modify miner type");
        sender.sendMessage(ChatColor.YELLOW + "/mineradmin forcecycle" + ChatColor.GRAY + " - Force production cycle");
    }

    private void listMinerTypes(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Miner Types ===");
        for (MinerType type : plugin.getMinerManager().getAllMinerTypes()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + type.getId() + ChatColor.GRAY + " (" +
                colorize(type.getDisplayName()) + ChatColor.GRAY + ") - " +
                type.getCategory().getDisplayName() + " / " + type.getResourceType().getDisplayName());
        }
    }

    private void giveMiner(CommandSender sender, String playerName, String typeId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        MinerType type = plugin.getMinerManager().getMinerType(typeId);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown miner type: " + typeId);
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        PlayerMiner miner = new PlayerMiner(type.getId());
        data.addMiner(miner);
        plugin.getPlayerDataManager().savePlayerData(target);

        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + colorize(type.getDisplayName()));
        target.sendMessage(ChatColor.GREEN + "You received a " + colorize(type.getDisplayName()) + ChatColor.GREEN + "!");
    }

    private void removeMiner(CommandSender sender, String playerName, String typeId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);

        if (typeId != null) {
            // Remove one miner of type
            List<PlayerMiner> miners = data.getMinersByType(typeId);
            if (miners.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Player doesn't have any " + typeId + " miners.");
                return;
            }
            data.removeMiner(miners.get(0).getMinerId());
            sender.sendMessage(ChatColor.GREEN + "Removed one " + typeId + " miner from " + target.getName());
        } else {
            // Remove all miners
            int count = data.getMinerCount();
            for (PlayerMiner miner : new ArrayList<>(data.getMiners())) {
                data.removeMiner(miner.getMinerId());
            }
            sender.sendMessage(ChatColor.GREEN + "Removed " + count + " miners from " + target.getName());
        }

        plugin.getPlayerDataManager().savePlayerData(target);
    }

    private void showMinerInfo(CommandSender sender, String typeId) {
        MinerType type = plugin.getMinerManager().getMinerType(typeId);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown miner type: " + typeId);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== " + colorize(type.getDisplayName()) + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + type.getId());
        sender.sendMessage(ChatColor.YELLOW + "Category: " + ChatColor.WHITE + type.getCategory().getDisplayName());
        sender.sendMessage(ChatColor.YELLOW + "Resource: " + ChatColor.WHITE + type.getResourceType().getDisplayName());
        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + type.getDescription());
        sender.sendMessage(ChatColor.YELLOW + "Icon: " + ChatColor.WHITE + type.getIcon().name());
        sender.sendMessage(ChatColor.YELLOW + "Base Production/Hour: " + ChatColor.WHITE + formatNumber(type.getBaseProduction()));
        sender.sendMessage(ChatColor.YELLOW + "Base Storage: " + ChatColor.WHITE + formatNumber(type.getBaseStorage()));
        sender.sendMessage(ChatColor.YELLOW + "Purchase Cost: " + ChatColor.WHITE + formatNumber(type.getPurchaseCost()) + " " + type.getPurchaseCurrency());
        sender.sendMessage(ChatColor.YELLOW + "Max Tier: " + ChatColor.WHITE + type.getMaxTier());

        sender.sendMessage(ChatColor.YELLOW + "Tiers:");
        for (int i = 1; i <= type.getMaxTier(); i++) {
            MinerTier tier = type.getTier(i);
            if (tier != null) {
                sender.sendMessage(ChatColor.GRAY + "  " + i + ": " + formatNumber(type.getProductionPerHour(i)) +
                    "/hr, " + formatNumber(type.getStorageCapacity(i)) + " storage" +
                    ", upgrade: " + formatNumber(tier.getUpgradeCost()) + " " + tier.getUpgradeCurrency());
            }
        }
    }

    private void createMinerType(CommandSender sender, String id) {
        if (plugin.getMinerManager().minerTypeExists(id)) {
            sender.sendMessage(ChatColor.RED + "Miner type already exists: " + id);
            return;
        }

        MinerType type = plugin.getMinerManager().createMinerType(id);
        if (type != null) {
            sender.sendMessage(ChatColor.GREEN + "Created new miner type: " + id);
            sender.sendMessage(ChatColor.GRAY + "Use /mineradmin set " + id + " <property> <value> to configure it.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to create miner type.");
        }
    }

    private void deleteMinerType(CommandSender sender, String typeId) {
        if (!plugin.getMinerManager().minerTypeExists(typeId)) {
            sender.sendMessage(ChatColor.RED + "Miner type not found: " + typeId);
            return;
        }

        if (plugin.getMinerManager().deleteMinerType(typeId)) {
            sender.sendMessage(ChatColor.GREEN + "Deleted miner type: " + typeId);
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete miner type.");
        }
    }

    private void setMinerProperty(CommandSender sender, String typeId, String property, String value) {
        MinerType type = plugin.getMinerManager().getMinerType(typeId);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown miner type: " + typeId);
            return;
        }

        try {
            switch (property.toLowerCase()) {
                case "displayname":
                case "display-name":
                case "name":
                    type.setDisplayName(value);
                    break;

                case "category":
                    type.setCategory(MinerCategory.fromString(value));
                    break;

                case "resource":
                case "resourcetype":
                case "resource-type":
                    type.setResourceType(ResourceType.fromString(value));
                    break;

                case "production":
                case "baseproduction":
                case "base-production":
                    type.setBaseProduction(Double.parseDouble(value));
                    break;

                case "storage":
                case "basestorage":
                case "base-storage":
                    type.setBaseStorage(Double.parseDouble(value));
                    break;

                case "cost":
                case "purchasecost":
                case "purchase-cost":
                    type.setPurchaseCost(Double.parseDouble(value));
                    break;

                case "currency":
                case "purchasecurrency":
                case "purchase-currency":
                    type.setPurchaseCurrency(value);
                    break;

                case "icon":
                    type.setIcon(Material.valueOf(value.toUpperCase()));
                    break;

                case "description":
                    type.setDescription(value);
                    break;

                case "maxtier":
                case "max-tier":
                    type.setMaxTier(Integer.parseInt(value));
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown property: " + property);
                    sender.sendMessage(ChatColor.GRAY + "Valid: displayname, category, resource, production, storage, cost, currency, icon, description, maxtier");
                    return;
            }

            plugin.getMinerManager().saveMiners();
            sender.sendMessage(ChatColor.GREEN + "Set " + property + " to " + value + " for " + typeId);

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Invalid value: " + e.getMessage());
        }
    }

    private void showPlayerInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);

        sender.sendMessage(ChatColor.GOLD + "=== " + target.getName() + "'s Miners ===");
        sender.sendMessage(ChatColor.YELLOW + "Total Miners: " + ChatColor.WHITE + data.getMinerCount());

        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            String typeName = type != null ? colorize(type.getDisplayName()) : miner.getTypeId();
            double maxStorage = type != null ? type.getStorageCapacity(miner.getTier()) : 0;

            sender.sendMessage(ChatColor.GRAY + "- " + typeName +
                ChatColor.GRAY + " T" + miner.getTier() +
                " | " + (miner.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive") +
                ChatColor.GRAY + " | " + formatNumber(miner.getStoredResources()) + "/" + formatNumber(maxStorage));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("luminaryminers.admin")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                "help", "reload", "list", "give", "remove", "info", "create", "delete", "set", "player", "forcecycle"
            );
            String input = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            if (sub.equals("give") || sub.equals("remove") || sub.equals("player")) {
                // Player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            } else if (sub.equals("info") || sub.equals("delete") || sub.equals("set")) {
                // Miner types
                for (MinerType type : plugin.getMinerManager().getAllMinerTypes()) {
                    if (type.getId().startsWith(input)) {
                        completions.add(type.getId());
                    }
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String input = args[2].toLowerCase();

            if (sub.equals("give")) {
                // Miner types
                for (MinerType type : plugin.getMinerManager().getAllMinerTypes()) {
                    if (type.getId().startsWith(input)) {
                        completions.add(type.getId());
                    }
                }
            } else if (sub.equals("remove")) {
                // Miner types
                for (MinerType type : plugin.getMinerManager().getAllMinerTypes()) {
                    if (type.getId().startsWith(input)) {
                        completions.add(type.getId());
                    }
                }
            } else if (sub.equals("set")) {
                // Properties
                List<String> props = Arrays.asList(
                    "displayname", "category", "resource", "production", "storage", "cost", "currency", "icon", "description", "maxtier"
                );
                for (String prop : props) {
                    if (prop.startsWith(input)) {
                        completions.add(prop);
                    }
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
