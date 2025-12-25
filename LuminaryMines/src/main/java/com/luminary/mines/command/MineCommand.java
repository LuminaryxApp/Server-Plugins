package com.luminary.mines.command;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.gui.MineGUI;
import com.luminary.mines.mine.Mine;
import com.luminary.mines.schematic.MineSchematic;
import com.luminary.mines.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Player mine command.
 */
public class MineCommand implements CommandExecutor, TabCompleter {

    private final LuminaryMines plugin;
    private final MineGUI gui;

    public MineCommand(LuminaryMines plugin) {
        this.plugin = plugin;
        this.gui = new MineGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (args.length == 0) {
            // Open mine menu or show help
            Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
            if (mine != null) {
                gui.openMainMenu(player, mine);
            } else {
                sendHelp(player);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(player);
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player);
            case "teleport", "tp", "go", "home" -> handleTeleport(player);
            case "reset" -> handleReset(player);
            case "menu", "gui" -> handleMenu(player);
            case "whitelist", "wl" -> handleWhitelist(player, args);
            case "info" -> handleInfo(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(TextUtil.colorize("&6&l━━━━━ Private Mines Help ━━━━━"));
        player.sendMessage(TextUtil.colorize("&e/mine &7- Open mine menu"));
        player.sendMessage(TextUtil.colorize("&e/mine create <schematic> &7- Create your mine"));
        player.sendMessage(TextUtil.colorize("&e/mine tp &7- Teleport to your mine"));
        player.sendMessage(TextUtil.colorize("&e/mine reset &7- Reset your mine"));
        player.sendMessage(TextUtil.colorize("&e/mine whitelist add/remove <player> &7- Manage access"));
        player.sendMessage(TextUtil.colorize("&e/mine info &7- View mine info"));
        player.sendMessage(TextUtil.colorize("&e/mine delete &7- Delete your mine"));
        player.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("luminarymines.create")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (plugin.getMineManager().hasMine(player.getUniqueId())) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.already-have")));
            return;
        }

        // Check schematic
        String schematicName;
        if (args.length >= 2) {
            schematicName = args[1].toLowerCase();
        } else {
            // Use default schematic
            Set<String> schematics = plugin.getSchematicManager().getSchematicNames();
            if (schematics.isEmpty()) {
                player.sendMessage(TextUtil.colorize("&cNo schematics available! Contact an admin."));
                return;
            }
            schematicName = schematics.iterator().next();
        }

        MineSchematic schematic = plugin.getSchematicManager().getSchematic(schematicName);
        if (schematic == null) {
            player.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
            player.sendMessage(TextUtil.colorize("&7Available: " +
                    String.join(", ", plugin.getSchematicManager().getSchematicNames())));
            return;
        }

        player.sendMessage(TextUtil.colorize("&eCreating your private mine..."));

        // Create the mine
        Mine mine = plugin.getMineManager().createMine(
                player.getUniqueId(),
                player.getName(),
                schematicName
        );

        if (mine == null) {
            player.sendMessage(TextUtil.colorize("&cFailed to create mine!"));
            return;
        }

        // Paste the schematic
        boolean pasted = plugin.getSchematicManager().pasteSchematic(mine, schematic);
        if (!pasted) {
            player.sendMessage(TextUtil.colorize("&cFailed to generate mine structure!"));
            plugin.getMineManager().deleteMine(player.getUniqueId());
            return;
        }

        // Fill the mine with blocks
        plugin.getResetTask().forceReset(mine);

        player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.created")));

        // Teleport player to their new mine
        org.bukkit.Location spawn = mine.getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            player.teleport(spawn);
            player.sendMessage(TextUtil.colorize("&aYou have been teleported to your new mine!"));
        }
    }

    private void handleDelete(Player player) {
        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        gui.openDeleteConfirmMenu(player, mine);
    }

    private void handleTeleport(Player player) {
        if (!player.hasPermission("luminarymines.teleport")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        org.bukkit.Location spawn = mine.getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) {
            player.sendMessage(TextUtil.colorize("&cCould not find mine location!"));
            return;
        }

        player.teleport(spawn);
        player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.teleported")));
    }

    private void handleReset(Player player) {
        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        player.sendMessage(TextUtil.colorize("&eResetting your mine..."));
        plugin.getResetTask().forceReset(mine);
    }

    private void handleMenu(Player player) {
        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        gui.openMainMenu(player, mine);
    }

    private void handleWhitelist(Player player, String[] args) {
        if (!player.hasPermission("luminarymines.whitelist")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        if (args.length < 2) {
            // Show whitelist
            gui.openWhitelistMenu(player, mine, 0);
            return;
        }

        String action = args[1].toLowerCase();

        if (args.length < 3) {
            player.sendMessage(TextUtil.colorize("&cUsage: /mine whitelist <add|remove> <player>"));
            return;
        }

        String targetName = args[2];
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(TextUtil.colorize("&cPlayer not found: " + targetName));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(TextUtil.colorize("&cYou cannot add yourself to the whitelist!"));
            return;
        }

        switch (action) {
            case "add" -> {
                int maxSize = plugin.getConfigManager().getMaxWhitelistSize();
                if (mine.getWhitelist().size() >= maxSize) {
                    player.sendMessage(TextUtil.colorize("&cWhitelist is full! Max: " + maxSize));
                    return;
                }
                if (mine.addToWhitelist(target.getUniqueId())) {
                    plugin.getMineManager().saveMine(mine);
                    player.sendMessage(TextUtil.colorize("&aAdded &f" + target.getName() + " &ato whitelist!"));
                } else {
                    player.sendMessage(TextUtil.colorize("&cPlayer is already whitelisted!"));
                }
            }
            case "remove" -> {
                if (mine.removeFromWhitelist(target.getUniqueId())) {
                    plugin.getMineManager().saveMine(mine);
                    player.sendMessage(TextUtil.colorize("&aRemoved &f" + target.getName() + " &afrom whitelist!"));
                } else {
                    player.sendMessage(TextUtil.colorize("&cPlayer is not on whitelist!"));
                }
            }
            default -> player.sendMessage(TextUtil.colorize("&cUsage: /mine whitelist <add|remove> <player>"));
        }
    }

    private void handleInfo(Player player) {
        Mine mine = plugin.getMineManager().getMine(player.getUniqueId());
        if (mine == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.no-mine")));
            return;
        }

        double minedPercent = plugin.getResetTask().getMinedPercentage(mine);
        long resetTime = mine.getTimeUntilReset() / 1000;

        player.sendMessage(TextUtil.colorize("&6&l━━━━━ Mine Info ━━━━━"));
        player.sendMessage(TextUtil.colorize("&eTier: &f" + mine.getTier()));
        player.sendMessage(TextUtil.colorize("&eSchematic: &f" + mine.getSchematicName()));
        player.sendMessage(TextUtil.colorize("&eMined: &f" + TextUtil.formatPercentage(minedPercent)));
        player.sendMessage(TextUtil.colorize("&eReset in: &f" + TextUtil.formatTime(resetTime)));
        player.sendMessage(TextUtil.colorize("&eAuto-reset: " + (mine.isAutoReset() ? "&aEnabled" : "&cDisabled")));
        player.sendMessage(TextUtil.colorize("&eWhitelist: &f" + mine.getWhitelist().size() + " players"));
        player.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "create", "delete", "tp", "reset", "menu", "whitelist", "info"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("create")) {
                completions.addAll(plugin.getSchematicManager().getSchematicNames());
            } else if (sub.equals("whitelist") || sub.equals("wl")) {
                completions.addAll(Arrays.asList("add", "remove"));
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("whitelist") || sub.equals("wl")) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (!p.equals(sender)) {
                        completions.add(p.getName());
                    }
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
