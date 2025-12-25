package com.luminary.crates.crate;

import com.luminary.crates.LuminaryCrates;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages crate locations in the world.
 */
public class CrateManager {

    private final LuminaryCrates plugin;
    private final Map<Location, CrateLocation> crateLocations = new HashMap<>();

    public CrateManager(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    public void loadLocations() {
        crateLocations.clear();

        FileConfiguration config = plugin.getConfigManager().getLocationsConfig();
        ConfigurationSection locationsSection = config.getConfigurationSection("locations");

        if (locationsSection == null) {
            return;
        }

        for (String key : locationsSection.getKeys(false)) {
            ConfigurationSection section = locationsSection.getConfigurationSection(key);
            if (section == null) continue;

            String worldName = section.getString("world");
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            String crateId = section.getString("crate");

            if (worldName == null || crateId == null) continue;

            var world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World not found for crate location: " + worldName);
                continue;
            }

            Location location = new Location(world, x, y, z);
            Crate crate = plugin.getCrateRegistry().getCrate(crateId);

            if (crate == null) {
                plugin.getLogger().warning("Crate type not found: " + crateId);
                continue;
            }

            crateLocations.put(location.getBlock().getLocation(), new CrateLocation(location, crate));
        }
    }

    public void saveLocations() {
        FileConfiguration config = plugin.getConfigManager().getLocationsConfig();

        // Clear existing locations
        config.set("locations", null);

        int index = 0;
        for (Map.Entry<Location, CrateLocation> entry : crateLocations.entrySet()) {
            Location loc = entry.getKey();
            CrateLocation crateLoc = entry.getValue();

            String path = "locations." + index;
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".crate", crateLoc.getCrate().getId());

            index++;
        }

        plugin.getConfigManager().saveLocationsConfig();
    }

    public void addCrateLocation(Location location, Crate crate) {
        Location blockLoc = location.getBlock().getLocation();
        crateLocations.put(blockLoc, new CrateLocation(blockLoc, crate));
        saveLocations();
    }

    public void removeCrateLocation(Location location) {
        Location blockLoc = location.getBlock().getLocation();
        crateLocations.remove(blockLoc);
        saveLocations();
    }

    public CrateLocation getCrateAt(Location location) {
        return crateLocations.get(location.getBlock().getLocation());
    }

    public boolean isCrateLocation(Location location) {
        return crateLocations.containsKey(location.getBlock().getLocation());
    }

    public Collection<CrateLocation> getAllLocations() {
        return Collections.unmodifiableCollection(crateLocations.values());
    }

    public int getLocationCount() {
        return crateLocations.size();
    }

    public List<CrateLocation> getLocationsForCrate(String crateId) {
        List<CrateLocation> result = new ArrayList<>();
        for (CrateLocation loc : crateLocations.values()) {
            if (loc.getCrate().getId().equals(crateId)) {
                result.add(loc);
            }
        }
        return result;
    }

    /**
     * Represents a crate placed in the world.
     */
    public static class CrateLocation {
        private final Location location;
        private final Crate crate;

        public CrateLocation(Location location, Crate crate) {
            this.location = location;
            this.crate = crate;
        }

        public Location getLocation() {
            return location;
        }

        public Crate getCrate() {
            return crate;
        }
    }
}
