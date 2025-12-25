package com.luminary.mines.mine;

import com.luminary.mines.LuminaryMines;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all private mines.
 */
public class MineManager {

    private final LuminaryMines plugin;
    private final Map<UUID, Mine> minesByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, Mine> minesById = new ConcurrentHashMap<>();

    // Grid position tracking for mine placement
    private int nextGridX = 0;
    private int nextGridZ = 0;
    private int gridSize = 10; // 10x10 grid before wrapping

    public MineManager(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    public void loadMines() {
        minesByPlayer.clear();
        minesById.clear();

        FileConfiguration config = plugin.getConfigManager().getMinesData();
        ConfigurationSection minesSection = config.getConfigurationSection("mines");

        if (minesSection == null) {
            return;
        }

        // Load grid position
        nextGridX = config.getInt("grid.next-x", 0);
        nextGridZ = config.getInt("grid.next-z", 0);

        for (String key : minesSection.getKeys(false)) {
            ConfigurationSection section = minesSection.getConfigurationSection(key);
            if (section != null) {
                try {
                    Mine mine = new Mine(section);
                    minesByPlayer.put(mine.getOwnerId(), mine);
                    minesById.put(mine.getMineId(), mine);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load mine '" + key + "': " + e.getMessage());
                }
            }
        }

        plugin.getLogger().info("Loaded " + minesById.size() + " private mines.");
    }

    public void saveAll() {
        FileConfiguration config = plugin.getConfigManager().getMinesData();

        // Clear existing data
        config.set("mines", null);

        // Save grid position
        config.set("grid.next-x", nextGridX);
        config.set("grid.next-z", nextGridZ);

        // Save all mines
        for (Mine mine : minesById.values()) {
            ConfigurationSection section = config.createSection("mines." + mine.getMineId().toString());
            mine.save(section);
        }

        plugin.getConfigManager().saveMinesData();
    }

    public void saveMine(Mine mine) {
        FileConfiguration config = plugin.getConfigManager().getMinesData();
        ConfigurationSection section = config.createSection("mines." + mine.getMineId().toString());
        mine.save(section);
        plugin.getConfigManager().saveMinesData();
    }

    /**
     * Create a new mine for a player.
     */
    public Mine createMine(UUID ownerId, String ownerName, String schematicName) {
        // Check if player already has a mine
        if (minesByPlayer.containsKey(ownerId)) {
            return null;
        }

        Mine mine = new Mine(ownerId, ownerName, schematicName);

        // Calculate grid position
        Location origin = calculateNextMineLocation();
        mine.setOrigin(origin);

        // Set default spawn (will be updated when schematic is pasted)
        mine.setSpawn(origin.clone().add(0.5, 1, 0.5));

        // Set default block composition based on tier 1
        Map<String, Integer> blocks = new LinkedHashMap<>();
        blocks.put("COBBLESTONE", 50);
        blocks.put("STONE", 30);
        blocks.put("COAL_ORE", 15);
        blocks.put("IRON_ORE", 5);
        mine.setBlockComposition(blocks);

        // Register mine
        minesByPlayer.put(ownerId, mine);
        minesById.put(mine.getMineId(), mine);

        // Advance grid position
        advanceGridPosition();

        // Save
        saveMine(mine);

        return mine;
    }

    /**
     * Calculate the next mine location on the grid.
     */
    private Location calculateNextMineLocation() {
        int spacing = plugin.getConfigManager().getMineSpacing();
        String worldName = plugin.getConfigManager().getMineWorld();

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            world = createMineWorld(worldName);
        }

        int x = nextGridX * spacing;
        int z = nextGridZ * spacing;
        int y = plugin.getConfigManager().getYLevel();

        return new Location(world, x, y, z);
    }

    /**
     * Create or get the mines world.
     */
    public World getOrCreateMineWorld() {
        String worldName = plugin.getConfigManager().getMineWorld();
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            world = createMineWorld(worldName);
        }
        return world;
    }

    /**
     * Create the void mine world.
     */
    private World createMineWorld(String worldName) {
        if (!plugin.getConfigManager().isAutoCreateWorld()) {
            plugin.getLogger().warning("Mine world '" + worldName + "' not found and auto-create is disabled!");
            return plugin.getServer().getWorlds().get(0);
        }

        plugin.getLogger().info("Creating mine world: " + worldName);

        String worldType = plugin.getConfigManager().getWorldType();
        WorldCreator creator = new WorldCreator(worldName);

        if (worldType.equalsIgnoreCase("VOID")) {
            creator.type(WorldType.FLAT);
            creator.generatorSettings("{\"layers\": [], \"biome\": \"the_void\"}");
            creator.generator(new VoidGenerator());
        } else {
            creator.type(WorldType.NORMAL);
        }

        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        World world = creator.createWorld();
        if (world != null) {
            world.setSpawnLocation(0, 64, 0);
            world.setKeepSpawnInMemory(false);
            plugin.getLogger().info("Mine world '" + worldName + "' created successfully!");
        }
        return world;
    }

    /**
     * Void chunk generator for empty worlds.
     */
    private static class VoidGenerator extends ChunkGenerator {
        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }

        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }
    }

    /**
     * Advance to the next grid position.
     */
    private void advanceGridPosition() {
        nextGridX++;
        if (nextGridX >= gridSize) {
            nextGridX = 0;
            nextGridZ++;
        }

        // Save grid position
        FileConfiguration config = plugin.getConfigManager().getMinesData();
        config.set("grid.next-x", nextGridX);
        config.set("grid.next-z", nextGridZ);
    }

    /**
     * Delete a mine.
     */
    public boolean deleteMine(UUID ownerId) {
        Mine mine = minesByPlayer.remove(ownerId);
        if (mine == null) return false;

        minesById.remove(mine.getMineId());

        // Remove from config
        FileConfiguration config = plugin.getConfigManager().getMinesData();
        config.set("mines." + mine.getMineId().toString(), null);
        plugin.getConfigManager().saveMinesData();

        return true;
    }

    /**
     * Get a mine by owner UUID.
     */
    public Mine getMine(UUID ownerId) {
        return minesByPlayer.get(ownerId);
    }

    /**
     * Get a mine by mine ID.
     */
    public Mine getMineById(UUID mineId) {
        return minesById.get(mineId);
    }

    /**
     * Check if a player has a mine.
     */
    public boolean hasMine(UUID ownerId) {
        return minesByPlayer.containsKey(ownerId);
    }

    /**
     * Get all mines.
     */
    public Collection<Mine> getAllMines() {
        return Collections.unmodifiableCollection(minesById.values());
    }

    /**
     * Get mine count.
     */
    public int getMineCount() {
        return minesById.size();
    }

    /**
     * Find which mine a location is in.
     */
    public Mine getMineAt(Location location) {
        for (Mine mine : minesById.values()) {
            if (mine.isInMine(location)) {
                return mine;
            }
        }
        return null;
    }

    /**
     * Get mines that need resetting.
     */
    public List<Mine> getMinesNeedingReset() {
        List<Mine> needsReset = new ArrayList<>();
        for (Mine mine : minesById.values()) {
            if (mine.needsReset()) {
                needsReset.add(mine);
            }
        }
        return needsReset;
    }
}
