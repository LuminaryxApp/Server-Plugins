package com.luminary.mines.command;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.mine.Mine;
import com.luminary.mines.schematic.MineSchematic;
import com.luminary.mines.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Admin mine command.
 */
public class MineAdminCommand implements CommandExecutor, TabCompleter {

    private final LuminaryMines plugin;

    public MineAdminCommand(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarymines.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "reset" -> handleReset(sender, args);
            case "resetall" -> handleResetAll(sender);
            case "tp", "teleport" -> handleTeleport(sender, args);
            case "list" -> handleList(sender, args);
            case "info" -> handleInfo(sender, args);
            case "settier" -> handleSetTier(sender, args);
            case "reload" -> handleReload(sender);
            case "schematics" -> handleSchematics(sender);
            case "setregion" -> handleSetRegion(sender, args);
            case "clearregion" -> handleClearRegion(sender, args);
            case "paste" -> handlePaste(sender, args);
            case "setspawn" -> handleSetSpawn(sender, args);
            case "clearspawn" -> handleClearSpawn(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Mine Admin Help ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin create <player> <schematic> &7- Create mine for player"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin delete <player> &7- Delete a player's mine"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin reset <player> &7- Reset a player's mine"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin resetall &7- Reset all mines"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin tp <player> &7- Teleport to a player's mine"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin list [page] &7- List all mines"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin info <player> &7- View mine info"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin settier <player> <tier> &7- Set mine tier"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin schematics &7- List available schematics"));
        sender.sendMessage(TextUtil.colorize("&6&lSchematic Setup:"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin paste <schematic> &7- Paste schematic at your location"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin setregion <schematic> &7- Set mine region (use WE selection)"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin clearregion <schematic> &7- Clear custom region"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin setspawn <schematic> &7- Set spawn point (stand at spawn)"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin clearspawn <schematic> &7- Clear custom spawn"));
        sender.sendMessage(TextUtil.colorize("&e/mineadmin reload &7- Reload configuration"));
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin create <player> <schematic>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(TextUtil.colorize("&cPlayer not found: " + args[1]));
            return;
        }

        if (plugin.getMineManager().hasMine(target.getUniqueId())) {
            sender.sendMessage(TextUtil.colorize("&cPlayer already has a mine!"));
            return;
        }

        String schematicName = args[2].toLowerCase();
        MineSchematic schematic = plugin.getSchematicManager().getSchematic(schematicName);
        if (schematic == null) {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&eCreating mine for " + target.getName() + "..."));

        String playerName = target.getName() != null ? target.getName() : args[1];
        Mine mine = plugin.getMineManager().createMine(target.getUniqueId(), playerName, schematicName);

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cFailed to create mine!"));
            return;
        }

        boolean pasted = plugin.getSchematicManager().pasteSchematic(mine, schematic);
        if (!pasted) {
            sender.sendMessage(TextUtil.colorize("&cFailed to paste schematic!"));
            plugin.getMineManager().deleteMine(target.getUniqueId());
            return;
        }

        plugin.getResetTask().forceReset(mine);

        sender.sendMessage(TextUtil.colorize("&aCreated mine for " + playerName + "!"));

        if (target.isOnline()) {
            ((Player) target).sendMessage(TextUtil.colorize("&aAn admin has created a private mine for you!"));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin delete <player>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Mine mine = plugin.getMineManager().getMine(target.getUniqueId());

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have a mine!"));
            return;
        }

        plugin.getMineManager().deleteMine(target.getUniqueId());
        sender.sendMessage(TextUtil.colorize("&aDeleted mine for " + args[1] + "!"));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin reset <player>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Mine mine = plugin.getMineManager().getMine(target.getUniqueId());

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have a mine!"));
            return;
        }

        plugin.getResetTask().forceReset(mine);
        sender.sendMessage(TextUtil.colorize("&aReset mine for " + args[1] + "!"));
    }

    private void handleResetAll(CommandSender sender) {
        Collection<Mine> mines = plugin.getMineManager().getAllMines();
        int count = 0;

        for (Mine mine : mines) {
            plugin.getResetTask().forceReset(mine);
            count++;
        }

        sender.sendMessage(TextUtil.colorize("&aReset " + count + " mines!"));
    }

    private void handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin tp <player>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Mine mine = plugin.getMineManager().getMine(target.getUniqueId());

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have a mine!"));
            return;
        }

        org.bukkit.Location spawn = mine.getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) {
            sender.sendMessage(TextUtil.colorize("&cCould not find mine location!"));
            return;
        }

        player.teleport(spawn);
        player.sendMessage(TextUtil.colorize("&aTeleported to " + args[1] + "'s mine!"));
    }

    private void handleList(CommandSender sender, String[] args) {
        Collection<Mine> mines = plugin.getMineManager().getAllMines();
        int page = 0;

        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException ignored) {
            }
        }

        int perPage = 10;
        int totalPages = (int) Math.ceil(mines.size() / (double) perPage);
        page = Math.max(0, Math.min(page, totalPages - 1));

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Mines (Page " + (page + 1) + "/" + totalPages + ") ━━━━━"));

        List<Mine> mineList = new ArrayList<>(mines);
        int start = page * perPage;
        int end = Math.min(start + perPage, mineList.size());

        for (int i = start; i < end; i++) {
            Mine mine = mineList.get(i);
            sender.sendMessage(TextUtil.colorize("&e" + mine.getOwnerName() +
                    " &7- Tier " + mine.getTier() +
                    " &7(" + mine.getSchematicName() + ")"));
        }

        sender.sendMessage(TextUtil.colorize("&7Total: " + mines.size() + " mines"));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin info <player>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Mine mine = plugin.getMineManager().getMine(target.getUniqueId());

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have a mine!"));
            return;
        }

        double minedPercent = plugin.getResetTask().getMinedPercentage(mine);
        long resetTime = mine.getTimeUntilReset() / 1000;

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Mine Info: " + mine.getOwnerName() + " ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&eID: &f" + mine.getMineId()));
        sender.sendMessage(TextUtil.colorize("&eTier: &f" + mine.getTier()));
        sender.sendMessage(TextUtil.colorize("&eSchematic: &f" + mine.getSchematicName()));
        sender.sendMessage(TextUtil.colorize("&eWorld: &f" + mine.getWorldName()));
        sender.sendMessage(TextUtil.colorize("&eMined: &f" + TextUtil.formatPercentage(minedPercent)));
        sender.sendMessage(TextUtil.colorize("&eReset in: &f" + TextUtil.formatTime(resetTime)));
        sender.sendMessage(TextUtil.colorize("&eWhitelist: &f" + mine.getWhitelist().size() + " players"));
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleSetTier(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin settier <player> <tier>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Mine mine = plugin.getMineManager().getMine(target.getUniqueId());

        if (mine == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer doesn't have a mine!"));
            return;
        }

        int tier;
        try {
            tier = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.colorize("&cInvalid tier: " + args[2]));
            return;
        }

        mine.setTier(tier);
        plugin.getMineManager().saveMine(mine);
        sender.sendMessage(TextUtil.colorize("&aSet " + args[1] + "'s mine tier to " + tier + "!"));
    }

    private void handleSchematics(CommandSender sender) {
        Set<String> schematics = plugin.getSchematicManager().getSchematicNames();

        sender.sendMessage(TextUtil.colorize("&6&lAvailable Schematics:"));
        if (schematics.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&7No schematics loaded!"));
            sender.sendMessage(TextUtil.colorize("&7Add .schem files to plugins/LuminaryMines/schematics/"));
        } else {
            for (String name : schematics) {
                MineSchematic schematic = plugin.getSchematicManager().getSchematic(name);
                sender.sendMessage(TextUtil.colorize("&e" + name + " &7(" +
                        schematic.getWidth() + "x" + schematic.getHeight() + "x" + schematic.getLength() + ")"));
            }
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(TextUtil.colorize("&aLuminaryMines configuration reloaded!"));
    }

    // Track paste locations for setregion command
    private final Map<UUID, PasteInfo> pasteLocations = new HashMap<>();

    private record PasteInfo(String schematic, org.bukkit.Location location) {}

    private void handlePaste(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin paste <schematic>"));
            return;
        }

        String schematicName = args[1].toLowerCase();
        MineSchematic schematic = plugin.getSchematicManager().getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
            return;
        }

        org.bukkit.Location pasteLocation = player.getLocation();

        // Paste the schematic at player's location
        boolean success = plugin.getSchematicManager().pasteSchematicAt(schematic, pasteLocation);

        if (success) {
            // Store paste location for setregion command
            pasteLocations.put(player.getUniqueId(), new PasteInfo(schematicName, pasteLocation));

            sender.sendMessage(TextUtil.colorize("&aSchematic pasted at your location!"));
            sender.sendMessage(TextUtil.colorize("&7To set the mine region:"));
            sender.sendMessage(TextUtil.colorize("&7 1. Use WorldEdit wand to select the interior"));
            sender.sendMessage(TextUtil.colorize("&7 2. Run &e/mineadmin setregion " + schematicName));
        } else {
            sender.sendMessage(TextUtil.colorize("&cFailed to paste schematic!"));
        }
    }

    private void handleSetRegion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin setregion <schematic>"));
            return;
        }

        String schematicName = args[1].toLowerCase();
        MineSchematic schematic = plugin.getSchematicManager().getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
            return;
        }

        // Get stored paste location
        PasteInfo pasteInfo = pasteLocations.get(player.getUniqueId());
        if (pasteInfo == null || !pasteInfo.schematic().equals(schematicName)) {
            sender.sendMessage(TextUtil.colorize("&cYou must first paste this schematic with &e/mineadmin paste " + schematicName));
            return;
        }

        boolean success = plugin.getSchematicManager().setSchematicRegion(player, schematicName, pasteInfo.location());

        if (success) {
            sender.sendMessage(TextUtil.colorize("&aMine region set for schematic: " + schematicName));
            sender.sendMessage(TextUtil.colorize("&7All new mines using this schematic will fill this region."));
            // Clear the paste info
            pasteLocations.remove(player.getUniqueId());
        } else {
            sender.sendMessage(TextUtil.colorize("&cFailed to set region! Make sure you have a WorldEdit selection."));
            sender.sendMessage(TextUtil.colorize("&7Use &e//wand &7and select the interior area."));
        }
    }

    private void handleClearRegion(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin clearregion <schematic>"));
            return;
        }

        String schematicName = args[1].toLowerCase();

        if (plugin.getSchematicManager().clearSchematicRegion(schematicName)) {
            sender.sendMessage(TextUtil.colorize("&aCleared custom region for: " + schematicName));
            sender.sendMessage(TextUtil.colorize("&7Will use default wall-inset calculation."));
        } else {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
        }
    }

    private void handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin setspawn <schematic>"));
            return;
        }

        String schematicName = args[1].toLowerCase();

        if (plugin.getSchematicManager().getSchematic(schematicName) == null) {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
            return;
        }

        // Get stored paste location
        PasteInfo pasteInfo = pasteLocations.get(player.getUniqueId());
        if (pasteInfo == null || !pasteInfo.schematic().equals(schematicName)) {
            sender.sendMessage(TextUtil.colorize("&cYou must first paste this schematic with &e/mineadmin paste " + schematicName));
            return;
        }

        boolean success = plugin.getSchematicManager().setSchematicSpawn(player, schematicName, pasteInfo.location());

        if (success) {
            sender.sendMessage(TextUtil.colorize("&aSpawn point set for schematic: " + schematicName));
            sender.sendMessage(TextUtil.colorize("&7Players will spawn here when creating a mine with this schematic."));
        } else {
            sender.sendMessage(TextUtil.colorize("&cFailed to set spawn point!"));
        }
    }

    private void handleClearSpawn(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /mineadmin clearspawn <schematic>"));
            return;
        }

        String schematicName = args[1].toLowerCase();

        if (plugin.getSchematicManager().clearSchematicSpawn(schematicName)) {
            sender.sendMessage(TextUtil.colorize("&aCleared custom spawn for: " + schematicName));
            sender.sendMessage(TextUtil.colorize("&7Will use default spawn (top center of mine)."));
        } else {
            sender.sendMessage(TextUtil.colorize("&cSchematic not found: " + schematicName));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("luminarymines.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "create", "delete", "reset", "resetall",
                    "tp", "list", "info", "settier", "schematics", "reload",
                    "paste", "setregion", "clearregion", "setspawn", "clearspawn"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("create") || sub.equals("delete") || sub.equals("reset") ||
                    sub.equals("tp") || sub.equals("info") || sub.equals("settier")) {
                // Add all players with mines
                for (Mine mine : plugin.getMineManager().getAllMines()) {
                    completions.add(mine.getOwnerName());
                }
                // Also add online players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!completions.contains(p.getName())) {
                        completions.add(p.getName());
                    }
                }
            } else if (sub.equals("paste") || sub.equals("setregion") || sub.equals("clearregion") ||
                       sub.equals("setspawn") || sub.equals("clearspawn")) {
                completions.addAll(plugin.getSchematicManager().getSchematicNames());
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("create")) {
                completions.addAll(plugin.getSchematicManager().getSchematicNames());
            } else if (sub.equals("settier")) {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
