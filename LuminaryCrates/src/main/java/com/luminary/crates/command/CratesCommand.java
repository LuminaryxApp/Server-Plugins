package com.luminary.crates.command;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Main command handler for LuminaryCrates.
 */
public class CratesCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCrates plugin;

    public CratesCommand(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open virtual crate menu by default
            openCrateMenu(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "list" -> listCrates(sender);
            case "open", "menu" -> openCrateMenu(sender);
            case "give", "givekey" -> giveKey(sender, args);
            case "set" -> setCrate(sender, args);
            case "remove" -> removeCrate(sender);
            case "preview" -> previewCrate(sender, args);
            case "reload" -> reloadPlugin(sender);
            case "info" -> crateInfo(sender, args);
            default -> {
                sender.sendMessage(TextUtil.colorize("&cUnknown subcommand. Use /crates help"));
            }
        }

        return true;
    }

    /**
     * Open the virtual crate menu.
     */
    private void openCrateMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        plugin.getCrateMenuManager().openCrateMenu(player);
    }

    /**
     * Send help message.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ LuminaryCrates Help ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&e/crates &7- Open the crate menu"));
        sender.sendMessage(TextUtil.colorize("&e/crates list &7- List all crates"));
        sender.sendMessage(TextUtil.colorize("&e/crates preview <crate> &7- Preview a crate"));

        if (sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize("&6&lAdmin Commands:"));
            sender.sendMessage(TextUtil.colorize("&e/crates give <player> <crate> [amount] &7- Give keys"));
            sender.sendMessage(TextUtil.colorize("&e/crates set <crate> &7- Set crate at looked block"));
            sender.sendMessage(TextUtil.colorize("&e/crates remove &7- Remove crate at looked block"));
            sender.sendMessage(TextUtil.colorize("&e/crates info <crate> &7- View crate details"));
            sender.sendMessage(TextUtil.colorize("&e/crates reload &7- Reload configuration"));
        }
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    /**
     * List all available crates.
     */
    private void listCrates(CommandSender sender) {
        Collection<Crate> crates = plugin.getCrateRegistry().getAllCrates();

        if (crates.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&cNo crates configured!"));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&6&lAvailable Crates:"));
        for (Crate crate : crates) {
            sender.sendMessage(TextUtil.colorize("&7- " + crate.getTier().getColor() +
                    crate.getDisplayName() + " &7(" + crate.getId() + ") - " +
                    crate.getRewards().size() + " rewards"));
        }
    }

    /**
     * Give keys to a player.
     */
    private void giveKey(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /crates give <player> <crate> [amount]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer not found: " + args[1]));
            return;
        }

        Crate crate = plugin.getCrateRegistry().getCrate(args[2].toLowerCase());
        if (crate == null) {
            sender.sendMessage(TextUtil.colorize("&cCrate not found: " + args[2]));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                amount = Math.max(1, Math.min(amount, 64));
            } catch (NumberFormatException e) {
                sender.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[3]));
                return;
            }
        }

        plugin.getKeyManager().giveKeys(target, crate, amount);

        sender.sendMessage(TextUtil.colorize("&aGave " + amount + "x " +
                crate.getDisplayName() + " &akey(s) to " + target.getName()));

        target.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("key.received")
                .replace("{amount}", String.valueOf(amount))
                .replace("{crate}", crate.getDisplayName())));
    }

    /**
     * Set a crate at the block the player is looking at.
     */
    private void setCrate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /crates set <crate>"));
            return;
        }

        Crate crate = plugin.getCrateRegistry().getCrate(args[1].toLowerCase());
        if (crate == null) {
            sender.sendMessage(TextUtil.colorize("&cCrate not found: " + args[1]));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            sender.sendMessage(TextUtil.colorize("&cYou must be looking at a block!"));
            return;
        }

        // Check if already a crate
        if (plugin.getCrateManager().isCrateLocation(targetBlock.getLocation())) {
            sender.sendMessage(TextUtil.colorize("&cThis location already has a crate! Remove it first."));
            return;
        }

        plugin.getCrateManager().addCrateLocation(targetBlock.getLocation(), crate);
        sender.sendMessage(TextUtil.colorize("&aSet " + crate.getDisplayName() +
                " &acrate at your target location!"));
    }

    /**
     * Remove a crate at the block the player is looking at.
     */
    private void removeCrate(CommandSender sender) {
        if (!sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            sender.sendMessage(TextUtil.colorize("&cYou must be looking at a block!"));
            return;
        }

        if (!plugin.getCrateManager().isCrateLocation(targetBlock.getLocation())) {
            sender.sendMessage(TextUtil.colorize("&cThis location is not a crate!"));
            return;
        }

        plugin.getCrateManager().removeCrateLocation(targetBlock.getLocation());
        sender.sendMessage(TextUtil.colorize("&aRemoved crate from this location!"));
    }

    /**
     * Preview a crate's contents.
     */
    private void previewCrate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /crates preview <crate>"));
            return;
        }

        Crate crate = plugin.getCrateRegistry().getCrate(args[1].toLowerCase());
        if (crate == null) {
            sender.sendMessage(TextUtil.colorize("&cCrate not found: " + args[1]));
            return;
        }

        plugin.getPreviewManager().openPreview(player, crate);
    }

    /**
     * Reload the plugin configuration.
     */
    private void reloadPlugin(CommandSender sender) {
        if (!sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        plugin.reload();
        sender.sendMessage(TextUtil.colorize("&aLuminaryCrates configuration reloaded!"));
    }

    /**
     * Show detailed crate information.
     */
    private void crateInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarycrates.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /crates info <crate>"));
            return;
        }

        Crate crate = plugin.getCrateRegistry().getCrate(args[1].toLowerCase());
        if (crate == null) {
            sender.sendMessage(TextUtil.colorize("&cCrate not found: " + args[1]));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&6&l━━━━━ Crate Info ━━━━━"));
        sender.sendMessage(TextUtil.colorize("&eID: &f" + crate.getId()));
        sender.sendMessage(TextUtil.colorize("&eName: " + crate.getTier().getColor() + crate.getDisplayName()));
        sender.sendMessage(TextUtil.colorize("&eTier: " + crate.getTier().getColoredName()));
        sender.sendMessage(TextUtil.colorize("&eKey Material: &f" + crate.getKeyMaterial().name()));
        sender.sendMessage(TextUtil.colorize("&eRewards: &f" + crate.getRewards().size()));
        sender.sendMessage(TextUtil.colorize("&eLocations: &f" +
                plugin.getCrateManager().getLocationsForCrate(crate.getId()).size()));
        sender.sendMessage(TextUtil.colorize("&6&l━━━━━━━━━━━━━━━━━━━━"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "list", "open", "preview"));
            if (sender.hasPermission("luminarycrates.admin")) {
                completions.addAll(Arrays.asList("give", "set", "remove", "info", "reload"));
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") && sender.hasPermission("luminarycrates.admin")) {
                // Player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (sub.equals("preview") || sub.equals("set") || sub.equals("info")) {
                // Crate IDs
                completions.addAll(plugin.getCrateRegistry().getCrateIds());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("luminarycrates.admin")) {
                // Crate IDs
                completions.addAll(plugin.getCrateRegistry().getCrateIds());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("luminarycrates.admin")) {
                // Amount suggestions
                completions.addAll(Arrays.asList("1", "5", "10", "32", "64"));
            }
        }

        // Filter by prefix
        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
