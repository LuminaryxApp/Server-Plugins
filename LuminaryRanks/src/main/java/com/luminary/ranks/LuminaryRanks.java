package com.luminary.ranks;

import com.luminary.ranks.command.PrestigeCommand;
import com.luminary.ranks.command.RankupCommand;
import com.luminary.ranks.command.RanksCommand;
import com.luminary.ranks.command.RebirthCommand;
import com.luminary.ranks.config.ConfigManager;
import com.luminary.ranks.data.PlayerDataManager;
import com.luminary.ranks.economy.EconomyHook;
import com.luminary.ranks.listener.PlayerListener;
import com.luminary.ranks.placeholder.RankPlaceholders;
import com.luminary.ranks.rank.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryRanks extends JavaPlugin {

    private static LuminaryRanks instance;

    private ConfigManager configManager;
    private RankManager rankManager;
    private PlayerDataManager playerDataManager;
    private EconomyHook economyHook;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // Initialize rank manager
        this.rankManager = new RankManager(this);
        this.rankManager.loadRanks();

        // Initialize player data
        this.playerDataManager = new PlayerDataManager(this);

        // Hook into economy
        this.economyHook = new EconomyHook(this);
        this.economyHook.hook();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register commands
        getCommand("rankup").setExecutor(new RankupCommand(this));
        getCommand("prestige").setExecutor(new PrestigeCommand(this));
        getCommand("rebirth").setExecutor(new RebirthCommand(this));

        RanksCommand ranksCommand = new RanksCommand(this);
        getCommand("ranks").setExecutor(ranksCommand);
        getCommand("ranks").setTabCompleter(ranksCommand);

        // Register PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RankPlaceholders(this).register();
            getLogger().info("Registered PlaceholderAPI placeholders!");
        }

        getLogger().info("LuminaryRanks has been enabled!");
        getLogger().info("Loaded " + rankManager.getRankCount() + " ranks, " +
                        rankManager.getMaxPrestige() + " prestiges, " +
                        rankManager.getMaxRebirth() + " rebirths");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        getLogger().info("LuminaryRanks has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        rankManager.loadRanks();
        getLogger().info("LuminaryRanks configuration reloaded!");
    }

    public static LuminaryRanks getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }
}
