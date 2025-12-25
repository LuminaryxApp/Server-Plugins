package com.luminary.mines;

import com.luminary.mines.command.MineAdminCommand;
import com.luminary.mines.command.MineCommand;
import com.luminary.mines.config.ConfigManager;
import com.luminary.mines.listener.MineListener;
import com.luminary.mines.listener.PlayerListener;
import com.luminary.mines.mine.MineManager;
import com.luminary.mines.mine.MineResetTask;
import com.luminary.mines.schematic.SchematicManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryMines extends JavaPlugin {

    private static LuminaryMines instance;

    private ConfigManager configManager;
    private MineManager mineManager;
    private SchematicManager schematicManager;
    private MineResetTask resetTask;

    @Override
    public void onEnable() {
        instance = this;

        // Check for WorldEdit
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().severe("WorldEdit not found! LuminaryMines requires WorldEdit.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.schematicManager = new SchematicManager(this);
        this.schematicManager.loadSchematics();

        this.mineManager = new MineManager(this);
        this.mineManager.getOrCreateMineWorld(); // Ensure mine world exists
        this.mineManager.loadMines();

        // Start reset task
        this.resetTask = new MineResetTask(this);
        this.resetTask.start();

        // Register listeners
        getServer().getPluginManager().registerEvents(new MineListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register commands
        MineCommand mineCommand = new MineCommand(this);
        getCommand("mine").setExecutor(mineCommand);
        getCommand("mine").setTabCompleter(mineCommand);

        MineAdminCommand adminCommand = new MineAdminCommand(this);
        getCommand("mineadmin").setExecutor(adminCommand);
        getCommand("mineadmin").setTabCompleter(adminCommand);

        getLogger().info("LuminaryMines has been enabled!");
        getLogger().info("Loaded " + schematicManager.getSchematicCount() + " schematics.");
        getLogger().info("Loaded " + mineManager.getMineCount() + " private mines.");

        // Check integrations
        if (getServer().getPluginManager().getPlugin("LuminaryEconomy") != null) {
            getLogger().info("LuminaryEconomy detected - upgrade costs enabled!");
        }
    }

    @Override
    public void onDisable() {
        // Stop reset task
        if (resetTask != null) {
            resetTask.stop();
        }

        // Save all mines
        if (mineManager != null) {
            mineManager.saveAll();
        }

        getLogger().info("LuminaryMines has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        schematicManager.loadSchematics();
        mineManager.loadMines();
        resetTask.reload();
        getLogger().info("LuminaryMines configuration reloaded!");
    }

    public static LuminaryMines getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public MineResetTask getResetTask() {
        return resetTask;
    }
}
