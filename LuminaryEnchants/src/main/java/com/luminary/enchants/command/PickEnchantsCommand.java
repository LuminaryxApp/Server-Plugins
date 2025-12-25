package com.luminary.enchants.command;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for /pickenchants.
 */
public class PickEnchantsCommand implements CommandExecutor, TabCompleter {

    private final LuminaryEnchants plugin;

    public PickEnchantsCommand(LuminaryEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            // Open GUI
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
                return true;
            }

            if (!player.hasPermission("luminaryenchants.use")) {
                player.sendMessage(TextUtil.colorize("&cYou don't have permission to use this!"));
                return true;
            }

            plugin.getMenuManager().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "apply" -> handleApply(sender, args);
            case "clear" -> handleClear(sender);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            default -> {
                sender.sendMessage(TextUtil.colorize("&cUnknown subcommand: " + subCommand));
                sendHelp(sender);
            }
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("luminaryenchants.reload")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to reload!"));
            return;
        }

        plugin.reload();
        sender.sendMessage(TextUtil.colorize("&aLuminaryEnchants configuration reloaded!"));
    }

    private void handleApply(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminaryenchants.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this!"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /pickenchants apply <enchant> <level>"));
            return;
        }

        String enchantId = args[1].toLowerCase();
        EnchantDefinition definition = plugin.getEnchantRegistry().getEnchant(enchantId);

        if (definition == null) {
            sender.sendMessage(TextUtil.colorize("&cUnknown enchant: " + enchantId));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.colorize("&cInvalid level: " + args[2]));
            return;
        }

        if (level < 0 || level > definition.getMaxLevel()) {
            sender.sendMessage(TextUtil.colorize("&cLevel must be between 0 and " + definition.getMaxLevel()));
            return;
        }

        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            sender.sendMessage(TextUtil.colorize("&cYou must hold a pickaxe!"));
            return;
        }

        ItemStack modified = plugin.getPickaxeDataManager().setEnchant(pickaxe, enchantId, level);
        player.getInventory().setItemInMainHand(modified);

        if (level > 0) {
            sender.sendMessage(TextUtil.colorize("&aApplied " + definition.getDisplayName() +
                    " level " + level + " to your pickaxe!"));
        } else {
            sender.sendMessage(TextUtil.colorize("&aRemoved " + definition.getDisplayName() +
                    " from your pickaxe!"));
        }
    }

    private void handleClear(CommandSender sender) {
        if (!sender.hasPermission("luminaryenchants.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this!"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command can only be used by players!"));
            return;
        }

        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            sender.sendMessage(TextUtil.colorize("&cYou must hold a pickaxe!"));
            return;
        }

        ItemStack cleared = plugin.getPickaxeDataManager().clearEnchants(pickaxe);
        player.getInventory().setItemInMainHand(cleared);

        sender.sendMessage(TextUtil.colorize("&aCleared all custom enchants from your pickaxe!"));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&5&l=== Available Enchants ==="));

        for (EnchantDefinition def : plugin.getEnchantRegistry().getAllEnchants()) {
            String rarityColor = switch (def.getRarity()) {
                case COMMON -> "&f";
                case UNCOMMON -> "&a";
                case RARE -> "&b";
                case EPIC -> "&5";
                case LEGENDARY -> "&6";
            };

            sender.sendMessage(TextUtil.colorize(rarityColor + def.getDisplayName() +
                    " &7(" + def.getId() + ") - Max Level: " + def.getMaxLevel()));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /pickenchants info <enchant>"));
            return;
        }

        String enchantId = args[1].toLowerCase();
        EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(enchantId);

        if (def == null) {
            sender.sendMessage(TextUtil.colorize("&cUnknown enchant: " + enchantId));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&5&l=== " + def.getDisplayName() + " ==="));
        sender.sendMessage(TextUtil.colorize("&7ID: &f" + def.getId()));
        sender.sendMessage(TextUtil.colorize("&7Rarity: &f" + def.getRarity()));
        sender.sendMessage(TextUtil.colorize("&7Max Level: &f" + def.getMaxLevel()));
        sender.sendMessage(TextUtil.colorize("&7Description: &f" + def.getDescription()));
        sender.sendMessage(TextUtil.colorize("&7Triggers: &f" + def.getTriggers()));
        sender.sendMessage(TextUtil.colorize("&7Cooldown: &f" + TextUtil.formatDuration(def.getCooldownMs())));
        sender.sendMessage(TextUtil.colorize("&7Base Proc Chance: &f" + TextUtil.formatPercent(def.getProcChanceBase())));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&5&l=== LuminaryEnchants Help ==="));
        sender.sendMessage(TextUtil.colorize("&7/pickenchants &f- Open the enchant menu"));
        sender.sendMessage(TextUtil.colorize("&7/pickenchants list &f- List all enchants"));
        sender.sendMessage(TextUtil.colorize("&7/pickenchants info <enchant> &f- View enchant details"));

        if (sender.hasPermission("luminaryenchants.reload")) {
            sender.sendMessage(TextUtil.colorize("&7/pickenchants reload &f- Reload configuration"));
        }

        if (sender.hasPermission("luminaryenchants.admin")) {
            sender.sendMessage(TextUtil.colorize("&7/pickenchants apply <enchant> <level> &f- Apply enchant"));
            sender.sendMessage(TextUtil.colorize("&7/pickenchants clear &f- Clear all enchants"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                  @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("list", "info"));

            if (sender.hasPermission("luminaryenchants.reload")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("luminaryenchants.admin")) {
                subCommands.addAll(Arrays.asList("apply", "clear"));
            }

            String partial = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());

        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("apply") || subCommand.equals("info")) {
                String partial = args[1].toLowerCase();
                completions = plugin.getEnchantRegistry().getEnchantIds().stream()
                        .filter(id -> id.startsWith(partial))
                        .collect(Collectors.toList());
            }

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("apply")) {
                EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(args[1].toLowerCase());
                if (def != null) {
                    // Suggest some level values
                    completions = Arrays.asList("1", "10", "50", "100", String.valueOf(def.getMaxLevel()));
                }
            }
        }

        return completions;
    }
}
