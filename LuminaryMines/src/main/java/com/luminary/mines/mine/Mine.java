package com.luminary.mines.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a private mine owned by a player.
 */
public class Mine {

    private final UUID mineId;
    private final UUID ownerId;
    private String ownerName;
    private String schematicName;

    // Mine location and bounds
    private String worldName;
    private int originX, originY, originZ;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    // Spawn point within the mine
    private double spawnX, spawnY, spawnZ;
    private float spawnYaw, spawnPitch;

    // Mine settings
    private int tier = 1;
    private int resetInterval = 300; // seconds
    private long lastReset;
    private boolean autoReset = true;
    private final Set<UUID> whitelist = new HashSet<>();
    private final Map<String, Integer> blockComposition = new LinkedHashMap<>();

    // State
    private boolean dirty = false;

    public Mine(UUID ownerId, String ownerName, String schematicName) {
        this.mineId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.schematicName = schematicName;
        this.lastReset = System.currentTimeMillis();
    }

    public Mine(ConfigurationSection section) {
        this.mineId = UUID.fromString(section.getString("id"));
        this.ownerId = UUID.fromString(section.getString("owner-id"));
        this.ownerName = section.getString("owner-name", "Unknown");
        this.schematicName = section.getString("schematic");

        // Load location
        this.worldName = section.getString("world");
        this.originX = section.getInt("origin.x");
        this.originY = section.getInt("origin.y");
        this.originZ = section.getInt("origin.z");

        this.minX = section.getInt("bounds.min-x");
        this.minY = section.getInt("bounds.min-y");
        this.minZ = section.getInt("bounds.min-z");
        this.maxX = section.getInt("bounds.max-x");
        this.maxY = section.getInt("bounds.max-y");
        this.maxZ = section.getInt("bounds.max-z");

        // Load spawn
        this.spawnX = section.getDouble("spawn.x");
        this.spawnY = section.getDouble("spawn.y");
        this.spawnZ = section.getDouble("spawn.z");
        this.spawnYaw = (float) section.getDouble("spawn.yaw", 0);
        this.spawnPitch = (float) section.getDouble("spawn.pitch", 0);

        // Load settings
        this.tier = section.getInt("tier", 1);
        this.resetInterval = section.getInt("reset-interval", 300);
        this.lastReset = section.getLong("last-reset", System.currentTimeMillis());
        this.autoReset = section.getBoolean("auto-reset", true);

        // Load whitelist
        List<String> whitelistStrings = section.getStringList("whitelist");
        for (String uuidStr : whitelistStrings) {
            try {
                whitelist.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Load block composition
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String block : blocksSection.getKeys(false)) {
                blockComposition.put(block.toUpperCase(), blocksSection.getInt(block));
            }
        }
    }

    public void save(ConfigurationSection section) {
        section.set("id", mineId.toString());
        section.set("owner-id", ownerId.toString());
        section.set("owner-name", ownerName);
        section.set("schematic", schematicName);

        // Save location
        section.set("world", worldName);
        section.set("origin.x", originX);
        section.set("origin.y", originY);
        section.set("origin.z", originZ);

        section.set("bounds.min-x", minX);
        section.set("bounds.min-y", minY);
        section.set("bounds.min-z", minZ);
        section.set("bounds.max-x", maxX);
        section.set("bounds.max-y", maxY);
        section.set("bounds.max-z", maxZ);

        // Save spawn
        section.set("spawn.x", spawnX);
        section.set("spawn.y", spawnY);
        section.set("spawn.z", spawnZ);
        section.set("spawn.yaw", spawnYaw);
        section.set("spawn.pitch", spawnPitch);

        // Save settings
        section.set("tier", tier);
        section.set("reset-interval", resetInterval);
        section.set("last-reset", lastReset);
        section.set("auto-reset", autoReset);

        // Save whitelist
        List<String> whitelistStrings = new ArrayList<>();
        for (UUID uuid : whitelist) {
            whitelistStrings.add(uuid.toString());
        }
        section.set("whitelist", whitelistStrings);

        // Save block composition
        for (Map.Entry<String, Integer> entry : blockComposition.entrySet()) {
            section.set("blocks." + entry.getKey(), entry.getValue());
        }

        dirty = false;
    }

    // Location methods
    public void setOrigin(Location location) {
        this.worldName = location.getWorld().getName();
        this.originX = location.getBlockX();
        this.originY = location.getBlockY();
        this.originZ = location.getBlockZ();
        dirty = true;
    }

    public void setBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        dirty = true;
    }

    public void setSpawn(Location location) {
        this.spawnX = location.getX();
        this.spawnY = location.getY();
        this.spawnZ = location.getZ();
        this.spawnYaw = location.getYaw();
        this.spawnPitch = location.getPitch();
        dirty = true;
    }

    public Location getOrigin() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, originX, originY, originZ);
    }

    public Location getSpawnLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    public Location getMinCorner() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, minX, minY, minZ);
    }

    public Location getMaxCorner() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, maxX, maxY, maxZ);
    }

    public boolean isInMine(Location location) {
        if (!location.getWorld().getName().equals(worldName)) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    // Whitelist methods
    public boolean isWhitelisted(UUID playerId) {
        return ownerId.equals(playerId) || whitelist.contains(playerId);
    }

    public boolean addToWhitelist(UUID playerId) {
        if (whitelist.add(playerId)) {
            dirty = true;
            return true;
        }
        return false;
    }

    public boolean removeFromWhitelist(UUID playerId) {
        if (whitelist.remove(playerId)) {
            dirty = true;
            return true;
        }
        return false;
    }

    public Set<UUID> getWhitelist() {
        return Collections.unmodifiableSet(whitelist);
    }

    // Block composition
    public void setBlockComposition(Map<String, Integer> composition) {
        blockComposition.clear();
        blockComposition.putAll(composition);
        dirty = true;
    }

    public Map<String, Integer> getBlockComposition() {
        return Collections.unmodifiableMap(blockComposition);
    }

    // Reset timing
    public boolean needsReset() {
        if (!autoReset) return false;
        return System.currentTimeMillis() - lastReset >= resetInterval * 1000L;
    }

    public long getTimeUntilReset() {
        long elapsed = System.currentTimeMillis() - lastReset;
        long remaining = (resetInterval * 1000L) - elapsed;
        return Math.max(0, remaining);
    }

    public void markReset() {
        this.lastReset = System.currentTimeMillis();
        dirty = true;
    }

    // Getters and setters
    public UUID getMineId() {
        return mineId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        dirty = true;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
        dirty = true;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
        dirty = true;
    }

    public int getResetInterval() {
        return resetInterval;
    }

    public void setResetInterval(int resetInterval) {
        this.resetInterval = resetInterval;
        dirty = true;
    }

    public boolean isAutoReset() {
        return autoReset;
    }

    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
        dirty = true;
    }

    public long getLastReset() {
        return lastReset;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    // Direct bound accessors
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
}
