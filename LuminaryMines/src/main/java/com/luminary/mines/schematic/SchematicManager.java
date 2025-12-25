package com.luminary.mines.schematic;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.mine.Mine;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Manages mine schematics using WorldEdit.
 */
public class SchematicManager {

    private final LuminaryMines plugin;
    private final Map<String, MineSchematic> schematics = new LinkedHashMap<>();
    private File regionsFile;
    private YamlConfiguration regionsConfig;

    public SchematicManager(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    public void loadSchematics() {
        schematics.clear();

        // Load regions config
        loadRegionsConfig();

        File folder = plugin.getConfigManager().getSchematicsFolderFile();
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.getLogger().info("Created schematics folder. Add .schem or .schematic files!");
            return;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.endsWith(".schem") || name.endsWith(".schematic"));

        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No schematics found in schematics folder!");
            return;
        }

        for (File file : files) {
            String name = file.getName();
            String id = name.substring(0, name.lastIndexOf('.'));

            try {
                MineSchematic schematic = loadSchematic(file);
                if (schematic != null) {
                    // Load custom region if defined
                    loadSchematicRegion(id.toLowerCase(), schematic);
                    schematics.put(id.toLowerCase(), schematic);

                    String regionInfo = schematic.hasCustomRegion() ? " [custom region]" : "";
                    plugin.getLogger().info("Loaded schematic: " + id +
                            " (" + schematic.getWidth() + "x" + schematic.getHeight() + "x" + schematic.getLength() + ")" + regionInfo);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load schematic '" + name + "': " + e.getMessage());
            }
        }
    }

    private void loadRegionsConfig() {
        regionsFile = new File(plugin.getDataFolder(), "schematic-regions.yml");
        if (!regionsFile.exists()) {
            try {
                regionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create schematic-regions.yml");
            }
        }
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }

    private void loadSchematicRegion(String schematicId, MineSchematic schematic) {
        if (regionsConfig.isConfigurationSection(schematicId)) {
            // Load region if defined
            if (regionsConfig.contains(schematicId + ".min-x")) {
                int minX = regionsConfig.getInt(schematicId + ".min-x");
                int minY = regionsConfig.getInt(schematicId + ".min-y");
                int minZ = regionsConfig.getInt(schematicId + ".min-z");
                int maxX = regionsConfig.getInt(schematicId + ".max-x");
                int maxY = regionsConfig.getInt(schematicId + ".max-y");
                int maxZ = regionsConfig.getInt(schematicId + ".max-z");
                schematic.setCustomRegion(minX, minY, minZ, maxX, maxY, maxZ);
            }

            // Load spawn if defined
            if (regionsConfig.contains(schematicId + ".spawn-x")) {
                double spawnX = regionsConfig.getDouble(schematicId + ".spawn-x");
                double spawnY = regionsConfig.getDouble(schematicId + ".spawn-y");
                double spawnZ = regionsConfig.getDouble(schematicId + ".spawn-z");
                float spawnYaw = (float) regionsConfig.getDouble(schematicId + ".spawn-yaw", 0);
                float spawnPitch = (float) regionsConfig.getDouble(schematicId + ".spawn-pitch", 0);
                schematic.setCustomSpawn(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
            }
        }
    }

    private void saveSchematicRegion(String schematicId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        regionsConfig.set(schematicId + ".min-x", minX);
        regionsConfig.set(schematicId + ".min-y", minY);
        regionsConfig.set(schematicId + ".min-z", minZ);
        regionsConfig.set(schematicId + ".max-x", maxX);
        regionsConfig.set(schematicId + ".max-y", maxY);
        regionsConfig.set(schematicId + ".max-z", maxZ);
        saveRegionsConfig();
    }

    private void saveSchematicSpawn(String schematicId, double offsetX, double offsetY, double offsetZ, float yaw, float pitch) {
        regionsConfig.set(schematicId + ".spawn-x", offsetX);
        regionsConfig.set(schematicId + ".spawn-y", offsetY);
        regionsConfig.set(schematicId + ".spawn-z", offsetZ);
        regionsConfig.set(schematicId + ".spawn-yaw", yaw);
        regionsConfig.set(schematicId + ".spawn-pitch", pitch);
        saveRegionsConfig();
    }

    private void saveRegionsConfig() {
        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save schematic-regions.yml: " + e.getMessage());
        }
    }

    /**
     * Set the spawn point for a schematic using the player's current location.
     *
     * @param player The player at the spawn location
     * @param schematicName The schematic to configure
     * @param pasteOrigin Where the schematic was pasted (for calculating offsets)
     * @return true if successful
     */
    public boolean setSchematicSpawn(Player player, String schematicName, Location pasteOrigin) {
        MineSchematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return false;
        }

        Location playerLoc = player.getLocation();

        // Calculate spawn offset relative to schematic origin
        Clipboard clipboard = schematic.getClipboard();
        BlockVector3 clipMin = clipboard.getMinimumPoint();
        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3 offset = origin.subtract(clipMin);

        // The paste origin minus the offset gives us the schematic's min corner world position
        double schematicMinX = pasteOrigin.getBlockX() - offset.getBlockX();
        double schematicMinY = pasteOrigin.getBlockY() - offset.getBlockY();
        double schematicMinZ = pasteOrigin.getBlockZ() - offset.getBlockZ();

        // Calculate spawn offsets relative to schematic min corner
        double spawnOffsetX = playerLoc.getX() - schematicMinX;
        double spawnOffsetY = playerLoc.getY() - schematicMinY;
        double spawnOffsetZ = playerLoc.getZ() - schematicMinZ;

        // Save to schematic and config
        schematic.setCustomSpawn(spawnOffsetX, spawnOffsetY, spawnOffsetZ, playerLoc.getYaw(), playerLoc.getPitch());
        saveSchematicSpawn(schematicName.toLowerCase(), spawnOffsetX, spawnOffsetY, spawnOffsetZ, playerLoc.getYaw(), playerLoc.getPitch());

        return true;
    }

    /**
     * Clear the custom spawn for a schematic.
     */
    public boolean clearSchematicSpawn(String schematicName) {
        MineSchematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return false;
        }

        schematic.clearCustomSpawn();
        regionsConfig.set(schematicName.toLowerCase() + ".spawn-x", null);
        regionsConfig.set(schematicName.toLowerCase() + ".spawn-y", null);
        regionsConfig.set(schematicName.toLowerCase() + ".spawn-z", null);
        regionsConfig.set(schematicName.toLowerCase() + ".spawn-yaw", null);
        regionsConfig.set(schematicName.toLowerCase() + ".spawn-pitch", null);
        saveRegionsConfig();
        return true;
    }

    private MineSchematic loadSchematic(File file) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IOException("Unknown schematic format");
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            return new MineSchematic(file.getName(), clipboard);
        }
    }

    /**
     * Set the mine region for a schematic using WorldEdit selection.
     * The selection should be made relative to where the schematic was pasted.
     *
     * @param player The player with a WorldEdit selection
     * @param schematicName The schematic to configure
     * @param pasteOrigin Where the schematic was pasted (for calculating offsets)
     * @return true if successful
     */
    public boolean setSchematicRegion(Player player, String schematicName, Location pasteOrigin) {
        MineSchematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return false;
        }

        try {
            // Get player's WorldEdit selection
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            com.sk89q.worldedit.session.SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
            com.sk89q.worldedit.LocalSession session = sessionManager.get(wePlayer);
            Region selection = session.getSelection(wePlayer.getWorld());

            if (selection == null) {
                return false;
            }

            BlockVector3 selMin = selection.getMinimumPoint();
            BlockVector3 selMax = selection.getMaximumPoint();

            // Calculate offsets relative to the schematic's min corner
            Clipboard clipboard = schematic.getClipboard();
            BlockVector3 clipMin = clipboard.getMinimumPoint();
            BlockVector3 origin = clipboard.getOrigin();
            BlockVector3 offset = origin.subtract(clipMin);

            // The paste origin minus the offset gives us the schematic's min corner world position
            int schematicMinX = pasteOrigin.getBlockX() - offset.getBlockX();
            int schematicMinY = pasteOrigin.getBlockY() - offset.getBlockY();
            int schematicMinZ = pasteOrigin.getBlockZ() - offset.getBlockZ();

            // Calculate region offsets relative to schematic min corner
            int minX = selMin.getBlockX() - schematicMinX;
            int minY = selMin.getBlockY() - schematicMinY;
            int minZ = selMin.getBlockZ() - schematicMinZ;
            int maxX = selMax.getBlockX() - schematicMinX;
            int maxY = selMax.getBlockY() - schematicMinY;
            int maxZ = selMax.getBlockZ() - schematicMinZ;

            // Validate that selection is within schematic bounds
            if (minX < 0 || minY < 0 || minZ < 0 ||
                maxX >= schematic.getWidth() || maxY >= schematic.getHeight() || maxZ >= schematic.getLength()) {
                plugin.getLogger().warning("Selection extends outside schematic bounds!");
            }

            // Save to schematic and config
            schematic.setCustomRegion(minX, minY, minZ, maxX, maxY, maxZ);
            saveSchematicRegion(schematicName.toLowerCase(), minX, minY, minZ, maxX, maxY, maxZ);

            return true;

        } catch (IncompleteRegionException e) {
            return false;
        }
    }

    /**
     * Clear the custom region for a schematic.
     */
    public boolean clearSchematicRegion(String schematicName) {
        MineSchematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return false;
        }

        schematic.clearCustomRegion();
        regionsConfig.set(schematicName.toLowerCase(), null);
        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save schematic-regions.yml: " + e.getMessage());
        }
        return true;
    }

    /**
     * Paste a schematic at a specific location (for setup/preview purposes).
     */
    public boolean pasteSchematicAt(MineSchematic schematic, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        try {
            World bukkitWorld = location.getWorld();
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);

            Clipboard clipboard = schematic.getClipboard();
            BlockVector3 pastePoint = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pastePoint)
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to paste schematic: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a schematic by name.
     */
    public MineSchematic getSchematic(String name) {
        return schematics.get(name.toLowerCase());
    }

    /**
     * Get all available schematic names.
     */
    public Set<String> getSchematicNames() {
        return Collections.unmodifiableSet(schematics.keySet());
    }

    /**
     * Get schematic count.
     */
    public int getSchematicCount() {
        return schematics.size();
    }

    /**
     * Paste a schematic at a location and setup the mine.
     */
    public boolean pasteSchematic(Mine mine, MineSchematic schematic) {
        Location origin = mine.getOrigin();
        if (origin == null || origin.getWorld() == null) {
            return false;
        }

        try {
            World bukkitWorld = origin.getWorld();
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);

            Clipboard clipboard = schematic.getClipboard();
            BlockVector3 pastePoint = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pastePoint)
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            // Calculate bounds from schematic
            BlockVector3 min = clipboard.getMinimumPoint();
            BlockVector3 max = clipboard.getMaximumPoint();
            BlockVector3 offset = clipboard.getOrigin().subtract(min);

            int minX = origin.getBlockX() - offset.getBlockX();
            int minY = origin.getBlockY() - offset.getBlockY();
            int minZ = origin.getBlockZ() - offset.getBlockZ();
            int maxX = minX + clipboard.getRegion().getWidth() - 1;
            int maxY = minY + clipboard.getRegion().getHeight() - 1;
            int maxZ = minZ + clipboard.getRegion().getLength() - 1;

            mine.setBounds(minX, minY, minZ, maxX, maxY, maxZ);

            // Set spawn point
            if (schematic.hasCustomSpawn()) {
                // Use custom spawn offset
                double spawnX = minX + schematic.getSpawnOffsetX();
                double spawnY = minY + schematic.getSpawnOffsetY();
                double spawnZ = minZ + schematic.getSpawnOffsetZ();
                Location spawn = new Location(bukkitWorld, spawnX, spawnY, spawnZ,
                        schematic.getSpawnYaw(), schematic.getSpawnPitch());
                mine.setSpawn(spawn);
            } else {
                // Default: top center of the mine
                double spawnX = (minX + maxX) / 2.0 + 0.5;
                double spawnY = maxY + 2;
                double spawnZ = (minZ + maxZ) / 2.0 + 0.5;
                mine.setSpawn(new Location(bukkitWorld, spawnX, spawnY, spawnZ));
            }

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to paste schematic: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the interior region of a mine (for block filling).
     * Uses custom region if defined, otherwise falls back to wall thickness calculation.
     */
    public MineRegion getInteriorRegion(Mine mine, MineSchematic schematic) {
        // If schematic has a custom region defined, use it
        if (schematic.hasCustomRegion()) {
            // Calculate actual world coordinates from schematic offsets
            int minX = mine.getMinX() + schematic.getRegionOffsetMinX();
            int minY = mine.getMinY() + schematic.getRegionOffsetMinY();
            int minZ = mine.getMinZ() + schematic.getRegionOffsetMinZ();
            int maxX = mine.getMinX() + schematic.getRegionOffsetMaxX();
            int maxY = mine.getMinY() + schematic.getRegionOffsetMaxY();
            int maxZ = mine.getMinZ() + schematic.getRegionOffsetMaxZ();

            return new MineRegion(minX, minY, minZ, maxX, maxY, maxZ);
        }

        // Fallback: use wall thickness inset
        int inset = schematic.getWallThickness();

        int minX = mine.getMinCorner().getBlockX() + inset;
        int minY = mine.getMinCorner().getBlockY() + inset;
        int minZ = mine.getMinCorner().getBlockZ() + inset;
        int maxX = mine.getMaxCorner().getBlockX() - inset;
        int maxY = mine.getMaxCorner().getBlockY() - inset;
        int maxZ = mine.getMaxCorner().getBlockZ() - inset;

        return new MineRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Get the mine region using stored schematic information.
     */
    public MineRegion getMineRegion(Mine mine) {
        MineSchematic schematic = getSchematic(mine.getSchematicName());
        if (schematic != null) {
            return getInteriorRegion(mine, schematic);
        }
        // Fallback: 1 block inset
        int inset = 1;
        int minX = mine.getMinCorner().getBlockX() + inset;
        int minY = mine.getMinCorner().getBlockY() + inset;
        int minZ = mine.getMinCorner().getBlockZ() + inset;
        int maxX = mine.getMaxCorner().getBlockX() - inset;
        int maxY = mine.getMaxCorner().getBlockY() - inset;
        int maxZ = mine.getMaxCorner().getBlockZ() - inset;

        return new MineRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Represents the fillable region of a mine.
     */
    public static class MineRegion {
        public final int minX, minY, minZ;
        public final int maxX, maxY, maxZ;

        public MineRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public int getVolume() {
            return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        }
    }
}
