package com.luminary.crates.crate;

import com.luminary.crates.LuminaryCrates;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Registry for all crate types.
 */
public class CrateRegistry {

    private final LuminaryCrates plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();

    public CrateRegistry(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    public void loadCrates() {
        crates.clear();

        FileConfiguration config = plugin.getConfigManager().getCratesConfig();
        ConfigurationSection cratesSection = config.getConfigurationSection("crates");

        if (cratesSection == null) {
            plugin.getLogger().warning("No crates section found in crates.yml!");
            return;
        }

        for (String key : cratesSection.getKeys(false)) {
            ConfigurationSection crateSection = cratesSection.getConfigurationSection(key);
            if (crateSection != null) {
                try {
                    Crate crate = new Crate(crateSection);
                    crates.put(crate.getId(), crate);
                    plugin.getLogger().info("Loaded crate: " + crate.getId() +
                            " with " + crate.getRewards().size() + " rewards");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load crate '" + key + "': " + e.getMessage());
                }
            }
        }
    }

    public Crate getCrate(String id) {
        return crates.get(id);
    }

    public Collection<Crate> getAllCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    public Set<String> getCrateIds() {
        return Collections.unmodifiableSet(crates.keySet());
    }

    public boolean hasCrate(String id) {
        return crates.containsKey(id);
    }

    public int getCrateCount() {
        return crates.size();
    }

    public List<Crate> getCratesByTier(CrateTier tier) {
        List<Crate> result = new ArrayList<>();
        for (Crate crate : crates.values()) {
            if (crate.getTier() == tier) {
                result.add(crate);
            }
        }
        return result;
    }
}
