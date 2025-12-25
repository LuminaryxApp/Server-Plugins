package com.luminary.core;

import com.luminary.core.api.LuminaryAPI;
import com.luminary.core.command.*;
import com.luminary.core.config.ConfigManager;
import com.luminary.core.listener.ChatListener;
import com.luminary.core.listener.PlayerListener;
import com.luminary.core.moderation.ModerationManager;
import com.luminary.core.player.PlayerDataManager;
import com.luminary.core.staff.StaffManager;
import com.luminary.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryCore extends JavaPlugin {

    private static LuminaryCore instance;
    private static LuminaryAPI api;

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private ModerationManager moderationManager;
    private StaffManager staffManager;
    private boolean maintenanceMode = false;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config manager first
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // Initialize managers
        this.playerDataManager = new PlayerDataManager(this);
        this.moderationManager = new ModerationManager(this);
        this.staffManager = new StaffManager(this);

        // Load maintenance mode state
        this.maintenanceMode = configManager.getConfig().getBoolean("maintenance.enabled", false);

        // Initialize API
        api = new LuminaryAPI(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register commands
        registerCommands();

        getLogger().info("LuminaryCore has been enabled!");
        getLogger().info("Centralized config folder: " + configManager.getPluginConfigsFolder().getAbsolutePath());
    }

    @Override
    public void onDisable() {
        // Save all data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (moderationManager != null) {
            moderationManager.saveAll();
        }

        getLogger().info("LuminaryCore has been disabled!");
    }

    private void registerCommands() {
        // Moderation commands
        BanCommand banCommand = new BanCommand(this);
        getCommand("ban").setExecutor(banCommand);
        getCommand("ban").setTabCompleter(banCommand);

        UnbanCommand unbanCommand = new UnbanCommand(this);
        getCommand("unban").setExecutor(unbanCommand);
        getCommand("unban").setTabCompleter(unbanCommand);

        TempbanCommand tempbanCommand = new TempbanCommand(this);
        getCommand("tempban").setExecutor(tempbanCommand);
        getCommand("tempban").setTabCompleter(tempbanCommand);

        KickCommand kickCommand = new KickCommand(this);
        getCommand("kick").setExecutor(kickCommand);
        getCommand("kick").setTabCompleter(kickCommand);

        MuteCommand muteCommand = new MuteCommand(this);
        getCommand("mute").setExecutor(muteCommand);
        getCommand("mute").setTabCompleter(muteCommand);

        UnmuteCommand unmuteCommand = new UnmuteCommand(this);
        getCommand("unmute").setExecutor(unmuteCommand);
        getCommand("unmute").setTabCompleter(unmuteCommand);

        TempmuteCommand tempmuteCommand = new TempmuteCommand(this);
        getCommand("tempmute").setExecutor(tempmuteCommand);
        getCommand("tempmute").setTabCompleter(tempmuteCommand);

        WarnCommand warnCommand = new WarnCommand(this);
        getCommand("warn").setExecutor(warnCommand);
        getCommand("warn").setTabCompleter(warnCommand);

        FreezeCommand freezeCommand = new FreezeCommand(this);
        getCommand("freeze").setExecutor(freezeCommand);
        getCommand("freeze").setTabCompleter(freezeCommand);

        // Staff commands
        VanishCommand vanishCommand = new VanishCommand(this);
        getCommand("vanish").setExecutor(vanishCommand);
        getCommand("vanish").setTabCompleter(vanishCommand);

        StaffChatCommand staffChatCommand = new StaffChatCommand(this);
        getCommand("staffchat").setExecutor(staffChatCommand);
        getCommand("staffchat").setTabCompleter(staffChatCommand);

        // Admin commands
        MaintenanceCommand maintenanceCommand = new MaintenanceCommand(this);
        getCommand("maintenance").setExecutor(maintenanceCommand);
        getCommand("maintenance").setTabCompleter(maintenanceCommand);

        BroadcastCommand broadcastCommand = new BroadcastCommand(this);
        getCommand("broadcast").setExecutor(broadcastCommand);
        getCommand("broadcast").setTabCompleter(broadcastCommand);

        CoreCommand coreCommand = new CoreCommand(this);
        getCommand("luminarycore").setExecutor(coreCommand);
        getCommand("luminarycore").setTabCompleter(coreCommand);

        // Player info commands
        HistoryCommand historyCommand = new HistoryCommand(this);
        getCommand("history").setExecutor(historyCommand);
        getCommand("history").setTabCompleter(historyCommand);
    }

    public void reload() {
        configManager.loadAll();
        maintenanceMode = configManager.getConfig().getBoolean("maintenance.enabled", false);
        moderationManager.reload();
        getLogger().info("LuminaryCore configuration reloaded!");
    }

    // Getters
    public static LuminaryCore getInstance() {
        return instance;
    }

    public static LuminaryAPI getAPI() {
        return api;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }

    public StaffManager getStaffManager() {
        return staffManager;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean enabled) {
        this.maintenanceMode = enabled;
        configManager.getConfig().set("maintenance.enabled", enabled);
        configManager.saveConfig();
    }
}
