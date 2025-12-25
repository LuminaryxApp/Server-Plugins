package com.luminary.groups.command;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import com.luminary.groups.player.PlayerGroupData;
import com.luminary.groups.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GroupCommand implements CommandExecutor, TabCompleter {

    private final LuminaryGroups plugin;

    public GroupCommand(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "setprefix" -> handleSetPrefix(sender, args);
            case "setsuffix" -> handleSetSuffix(sender, args);
            case "setnamecolor" -> handleSetNameColor(sender, args);
            case "setchatcolor" -> handleSetChatColor(sender, args);
            case "setweight" -> handleSetWeight(sender, args);
            case "setdisplayname" -> handleSetDisplayName(sender, args);
            case "setdefault" -> handleSetDefault(sender, args);
            case "addperm" -> handleAddPerm(sender, args);
            case "removeperm" -> handleRemovePerm(sender, args);
            case "inherit" -> handleInherit(sender, args);
            case "uninherit" -> handleUninherit(sender, args);
            case "player" -> handlePlayer(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.toComponent(message));
    }

    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "&6&lLuminaryGroups Commands");
        sendMessage(sender, "&e/group create <name> &7- Create a new group");
        sendMessage(sender, "&e/group delete <name> &7- Delete a group");
        sendMessage(sender, "&e/group list &7- List all groups");
        sendMessage(sender, "&e/group info <group> &7- View group info");
        sendMessage(sender, "&e/group setprefix <group> <prefix> &7- Set group prefix");
        sendMessage(sender, "&e/group setsuffix <group> <suffix> &7- Set group suffix");
        sendMessage(sender, "&e/group setnamecolor <group> <color> &7- Set name color");
        sendMessage(sender, "&e/group setchatcolor <group> <color> &7- Set chat color");
        sendMessage(sender, "&e/group setweight <group> <weight> &7- Set group weight");
        sendMessage(sender, "&e/group setdisplayname <group> <name> &7- Set display name");
        sendMessage(sender, "&e/group setdefault <group> &7- Set as default group");
        sendMessage(sender, "&e/group addperm <group> <perm> &7- Add permission");
        sendMessage(sender, "&e/group removeperm <group> <perm> &7- Remove permission");
        sendMessage(sender, "&e/group inherit <group> <parent> &7- Add inheritance");
        sendMessage(sender, "&e/group uninherit <group> <parent> &7- Remove inheritance");
        sendMessage(sender, "&e/group player ... &7- Manage player groups");
        sendMessage(sender, "&e/group reload &7- Reload configuration");
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /group create <name>");
            return;
        }

        String groupId = args[1].toLowerCase();
        if (plugin.getGroupManager().groupExists(groupId)) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-exists", "{group}", groupId));
            return;
        }

        Group group = plugin.getGroupManager().createGroup(groupId);
        if (group != null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-created", "{group}", groupId));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /group delete <name>");
            return;
        }

        String groupId = args[1].toLowerCase();
        if (plugin.getGroupManager().deleteGroup(groupId)) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-deleted", "{group}", groupId));
        } else {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", groupId));
        }
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        sendMessage(sender, "&6&lGroups:");
        for (Group group : plugin.getGroupManager().getGroupsSortedByWeight()) {
            String defaultTag = group.isDefault() ? " &a(default)" : "";
            sendMessage(sender, String.format("&7- &f%s &7[Weight: %d]%s &7Prefix: %s",
                    group.getDisplayName(), group.getWeight(), defaultTag, group.getPrefix()));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /group info <group>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        sendMessage(sender, "&6&lGroup: &f" + group.getDisplayName());
        sendMessage(sender, "&7ID: &f" + group.getId());
        sendMessage(sender, "&7Prefix: " + group.getPrefix() + " &7(raw: &f" + group.getPrefix().replace("&", "&&") + "&7)");
        sendMessage(sender, "&7Suffix: " + group.getSuffix() + " &7(raw: &f" + group.getSuffix().replace("&", "&&") + "&7)");
        sendMessage(sender, "&7Name Color: " + group.getNameColor() + "Example &7(raw: &f" + group.getNameColor().replace("&", "&&") + "&7)");
        sendMessage(sender, "&7Chat Color: " + group.getChatColor() + "Example &7(raw: &f" + group.getChatColor().replace("&", "&&") + "&7)");
        sendMessage(sender, "&7Weight: &f" + group.getWeight());
        sendMessage(sender, "&7Default: &f" + group.isDefault());
        sendMessage(sender, "&7Inherits: &f" + String.join(", ", group.getInheritedGroups()));
        sendMessage(sender, "&7Permissions (" + group.getPermissions().size() + "):");
        for (String perm : group.getPermissions()) {
            sendMessage(sender, "  &7- &f" + perm);
        }
    }

    private void handleSetPrefix(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setprefix <group> <prefix>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        String prefix = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        group.setPrefix(prefix);
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-prefix-set",
                "{group}", group.getId(), "{prefix}", prefix));
    }

    private void handleSetSuffix(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setsuffix <group> <suffix>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        String suffix = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        group.setSuffix(suffix);
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-suffix-set",
                "{group}", group.getId(), "{suffix}", suffix));
    }

    private void handleSetNameColor(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setnamecolor <group> <color>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        group.setNameColor(args[2]);
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-namecolor-set",
                "{group}", group.getId(), "{color}", args[2]));
    }

    private void handleSetChatColor(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setchatcolor <group> <color>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        group.setChatColor(args[2]);
        plugin.getGroupManager().saveGroups();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-chatcolor-set",
                "{group}", group.getId(), "{color}", args[2]));
    }

    private void handleSetWeight(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setweight <group> <weight>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        try {
            int weight = Integer.parseInt(args[2]);
            group.setWeight(weight);
            plugin.getGroupManager().saveGroups();
            plugin.getPermissionManager().refreshAllPlayers();

            sendMessage(sender, plugin.getConfigManager().getMessage("group-weight-set",
                    "{group}", group.getId(), "{weight}", String.valueOf(weight)));
        } catch (NumberFormatException e) {
            sendMessage(sender, "&cWeight must be a number!");
        }
    }

    private void handleSetDisplayName(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group setdisplayname <group> <name>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        group.setDisplayName(displayName);
        plugin.getGroupManager().saveGroups();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-displayname-set",
                "{group}", group.getId(), "{name}", displayName));
    }

    private void handleSetDefault(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /group setdefault <group>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        // Clear default from all other groups
        for (Group g : plugin.getGroupManager().getAllGroups()) {
            g.setDefault(false);
        }

        group.setDefault(true);
        plugin.getGroupManager().saveGroups();

        sendMessage(sender, plugin.getConfigManager().getMessage("group-default-set", "{group}", group.getId()));
    }

    private void handleAddPerm(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group addperm <group> <permission>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        String permission = args[2];
        group.addPermission(permission);
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("permission-added",
                "{permission}", permission, "{group}", group.getId()));
    }

    private void handleRemovePerm(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group removeperm <group> <permission>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        String permission = args[2];
        group.removePermission(permission);
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("permission-removed",
                "{permission}", permission, "{group}", group.getId()));
    }

    private void handleInherit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group inherit <group> <parent>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        Group parent = plugin.getGroupManager().getGroup(args[2]);

        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        if (parent == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[2]));
            return;
        }

        group.addInheritance(parent.getId());
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("inheritance-added",
                "{group}", group.getId(), "{parent}", parent.getId()));
    }

    private void handleUninherit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /group uninherit <group> <parent>");
            return;
        }

        Group group = plugin.getGroupManager().getGroup(args[1]);
        if (group == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[1]));
            return;
        }

        group.removeInheritance(args[2].toLowerCase());
        plugin.getGroupManager().saveGroups();
        plugin.getPermissionManager().refreshAllPlayers();

        sendMessage(sender, plugin.getConfigManager().getMessage("inheritance-removed",
                "{group}", group.getId(), "{parent}", args[2]));
    }

    private void handlePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&6Player Commands:");
            sendMessage(sender, "&e/group player info <player> &7- View player groups");
            sendMessage(sender, "&e/group player setgroup <player> <group> &7- Set primary group");
            sendMessage(sender, "&e/group player addgroup <player> <group> &7- Add player to group");
            sendMessage(sender, "&e/group player removegroup <player> <group> &7- Remove from group");
            sendMessage(sender, "&e/group player setprefix <player> <prefix> &7- Set custom prefix");
            sendMessage(sender, "&e/group player setsuffix <player> <suffix> &7- Set custom suffix");
            sendMessage(sender, "&e/group player setnamecolor <player> <color> &7- Set custom name color");
            sendMessage(sender, "&e/group player clear <player> &7- Clear customizations");
            return;
        }

        String action = args[1].toLowerCase();
        String playerName = args[2];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-not-found", "{player}", playerName));
            return;
        }

        PlayerGroupData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) {
            sendMessage(sender, "&cPlayer data not loaded!");
            return;
        }

        switch (action) {
            case "info" -> {
                sendMessage(sender, "&6&lPlayer: &f" + target.getName());
                sendMessage(sender, "&7Groups: &f" + String.join(", ", data.getGroups()));
                sendMessage(sender, "&7Primary: &f" + (data.getPrimaryGroup() != null ? data.getPrimaryGroup() : "none"));
                sendMessage(sender, "&7Custom Prefix: &f" + (data.getCustomPrefix() != null ? data.getCustomPrefix() : "none"));
                sendMessage(sender, "&7Custom Suffix: &f" + (data.getCustomSuffix() != null ? data.getCustomSuffix() : "none"));
                sendMessage(sender, "&7Custom Name Color: &f" + (data.getCustomNameColor() != null ? data.getCustomNameColor() : "none"));
            }
            case "setgroup", "addgroup" -> {
                if (args.length < 4) {
                    sendMessage(sender, "&cUsage: /group player " + action + " <player> <group>");
                    return;
                }
                Group group = plugin.getGroupManager().getGroup(args[3]);
                if (group == null) {
                    sendMessage(sender, plugin.getConfigManager().getMessage("group-not-found", "{group}", args[3]));
                    return;
                }
                data.addGroup(group.getId());
                if (action.equals("setgroup")) {
                    data.setPrimaryGroup(group.getId());
                }
                plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                plugin.getPermissionManager().refreshPlayer(target);
                sendMessage(sender, plugin.getConfigManager().getMessage("player-group-added",
                        "{player}", target.getName(), "{group}", group.getId()));
            }
            case "removegroup" -> {
                if (args.length < 4) {
                    sendMessage(sender, "&cUsage: /group player removegroup <player> <group>");
                    return;
                }
                if (data.removeGroup(args[3])) {
                    plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                    plugin.getPermissionManager().refreshPlayer(target);
                    sendMessage(sender, plugin.getConfigManager().getMessage("player-group-removed",
                            "{player}", target.getName(), "{group}", args[3]));
                } else {
                    sendMessage(sender, "&cPlayer is not in that group!");
                }
            }
            case "setprefix" -> {
                if (args.length < 4) {
                    sendMessage(sender, "&cUsage: /group player setprefix <player> <prefix>");
                    return;
                }
                String prefix = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                data.setCustomPrefix(prefix);
                plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                sendMessage(sender, "&aSet custom prefix for " + target.getName() + " to: " + prefix);
            }
            case "setsuffix" -> {
                if (args.length < 4) {
                    sendMessage(sender, "&cUsage: /group player setsuffix <player> <suffix>");
                    return;
                }
                String suffix = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                data.setCustomSuffix(suffix);
                plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                sendMessage(sender, "&aSet custom suffix for " + target.getName() + " to: " + suffix);
            }
            case "setnamecolor" -> {
                if (args.length < 4) {
                    sendMessage(sender, "&cUsage: /group player setnamecolor <player> <color>");
                    return;
                }
                data.setCustomNameColor(args[3]);
                plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                sendMessage(sender, "&aSet custom name color for " + target.getName() + " to: " + args[3]);
            }
            case "clear" -> {
                data.clearCustomizations();
                plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
                sendMessage(sender, "&aCleared customizations for " + target.getName());
            }
            default -> sendMessage(sender, "&cUnknown player action: " + action);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        plugin.reload();
        sendMessage(sender, plugin.getConfigManager().getMessage("config-reloaded"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("luminarygroups.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "list", "info", "setprefix", "setsuffix",
                    "setnamecolor", "setchatcolor", "setweight", "setdisplayname", "setdefault",
                    "addperm", "removeperm", "inherit", "uninherit", "player", "reload"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "delete", "info", "setprefix", "setsuffix", "setnamecolor", "setchatcolor",
                     "setweight", "setdisplayname", "setdefault", "addperm", "removeperm", "inherit", "uninherit" -> {
                    for (Group group : plugin.getGroupManager().getAllGroups()) {
                        completions.add(group.getId());
                    }
                }
                case "player" -> completions.addAll(Arrays.asList("info", "setgroup", "addgroup",
                        "removegroup", "setprefix", "setsuffix", "setnamecolor", "clear"));
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("inherit") || sub.equals("uninherit")) {
                for (Group group : plugin.getGroupManager().getAllGroups()) {
                    if (!group.getId().equalsIgnoreCase(args[1])) {
                        completions.add(group.getId());
                    }
                }
            } else if (sub.equals("player")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("player")) {
                String action = args[1].toLowerCase();
                if (action.equals("setgroup") || action.equals("addgroup") || action.equals("removegroup")) {
                    for (Group group : plugin.getGroupManager().getAllGroups()) {
                        completions.add(group.getId());
                    }
                }
            }
        }

        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());
    }
}
