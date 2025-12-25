package com.luminary.groups;

import com.luminary.groups.command.GroupCommand;
import com.luminary.groups.config.ConfigManager;
import com.luminary.groups.group.GroupManager;
import com.luminary.groups.hook.RanksHook;
import com.luminary.groups.listener.ChatListener;
import com.luminary.groups.listener.PlayerListener;
import com.luminary.groups.permission.PermissionManager;
import com.luminary.groups.placeholder.GroupPlaceholders;
import com.luminary.groups.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryGroups extends JavaPlugin {

    private static LuminaryGroups instance;

    private ConfigManager configManager;
    private GroupManager groupManager;
    private PlayerDataManager playerDataManager;
    private PermissionManager permissionManager;
    private RanksHook ranksHook;

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // Load groups
        this.groupManager = new GroupManager(this);
        this.groupManager.loadGroups();

        // Load player data
        this.playerDataManager = new PlayerDataManager(this);

        // Initialize permission manager
        this.permissionManager = new PermissionManager(this);

        // Hook into LuminaryRanks
        this.ranksHook = new RanksHook(this);
        this.ranksHook.hook();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register commands
        GroupCommand groupCommand = new GroupCommand(this);
        getCommand("group").setExecutor(groupCommand);
        getCommand("group").setTabCompleter(groupCommand);

        // Register PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GroupPlaceholders(this).register();
            getLogger().info("Registered PlaceholderAPI placeholders!");
        }

        getLogger().info("LuminaryGroups has been enabled!");
        getLogger().info("Loaded " + groupManager.getGroupCount() + " groups.");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (groupManager != null) {
            groupManager.saveGroups();
        }

        // Clean up permissions
        if (permissionManager != null) {
            permissionManager.removeAllPermissions();
        }

        getLogger().info("LuminaryGroups has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        groupManager.loadGroups();
        permissionManager.refreshAllPlayers();
        getLogger().info("LuminaryGroups configuration reloaded!");
    }

    public static LuminaryGroups getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public RanksHook getRanksHook() {
        return ranksHook;
    }
}
