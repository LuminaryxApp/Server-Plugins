package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StaffChatCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public StaffChatCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.staffchat")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // If no args, toggle staff chat mode for players
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessageUtil.send(sender, "&cUsage: /staffchat <message>");
                return true;
            }

            Player player = (Player) sender;
            plugin.getStaffManager().toggleStaffChat(player);
            return true;
        }

        // Send message to staff chat
        String message = String.join(" ", args);

        if (sender instanceof Player) {
            plugin.getStaffManager().sendStaffMessage((Player) sender, message);
        } else {
            plugin.getStaffManager().sendStaffMessage("Console", message);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
