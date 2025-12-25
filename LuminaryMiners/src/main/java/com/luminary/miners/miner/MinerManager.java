package com.luminary.miners.miner;

import com.luminary.miners.LuminaryMiners;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all miner types and configurations.
 */
public class MinerManager {

    private final LuminaryMiners plugin;
    private final Map<String, MinerType> minerTypes = new LinkedHashMap<>();

    public MinerManager(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    public void loadMiners() {
        minerTypes.clear();

        FileConfiguration config = plugin.getConfigManager().getMinersConfig();
        ConfigurationSection minersSection = config.getConfigurationSection("miners");

        if (minersSection != null) {
            for (String minerId : minersSection.getKeys(false)) {
                ConfigurationSection section = minersSection.getConfigurationSection(minerId);
                if (section != null) {
                    MinerType type = new MinerType(minerId, section);
                    minerTypes.put(type.getId(), type);
                }
            }
        }

        // Create default miners if none exist
        if (minerTypes.isEmpty()) {
            createDefaultMiners();
            saveMiners();
        }

        plugin.getLogger().info("Loaded " + minerTypes.size() + " miner types.");
    }

    private void createDefaultMiners() {
        // Token Robot
        MinerType tokenRobot = new MinerType("token_robot");
        tokenRobot.setDisplayName("&6Token Robot");
        tokenRobot.setCategory(MinerCategory.ROBOT);
        tokenRobot.setResourceType(ResourceType.TOKENS);
        tokenRobot.setBaseProduction(100);
        tokenRobot.setBaseStorage(1000);
        tokenRobot.setPurchaseCost(5000);
        tokenRobot.setPurchaseCurrency("tokens");
        tokenRobot.setDescription("Mines tokens automatically");
        tokenRobot.setIcon(org.bukkit.Material.GOLD_BLOCK);
        minerTypes.put(tokenRobot.getId(), tokenRobot);

        // Beacon Robot
        MinerType beaconRobot = new MinerType("beacon_robot");
        beaconRobot.setDisplayName("&bBeacon Robot");
        beaconRobot.setCategory(MinerCategory.ROBOT);
        beaconRobot.setResourceType(ResourceType.BEACONS);
        beaconRobot.setBaseProduction(50);
        beaconRobot.setBaseStorage(500);
        beaconRobot.setPurchaseCost(10000);
        beaconRobot.setPurchaseCurrency("tokens");
        beaconRobot.setDescription("Mines beacons automatically");
        beaconRobot.setIcon(org.bukkit.Material.BEACON);
        minerTypes.put(beaconRobot.getId(), beaconRobot);

        // Gem Robot
        MinerType gemRobot = new MinerType("gem_robot");
        gemRobot.setDisplayName("&dGem Robot");
        gemRobot.setCategory(MinerCategory.ROBOT);
        gemRobot.setResourceType(ResourceType.GEMS);
        gemRobot.setBaseProduction(25);
        gemRobot.setBaseStorage(250);
        gemRobot.setPurchaseCost(25000);
        gemRobot.setPurchaseCurrency("tokens");
        gemRobot.setDescription("Mines gems automatically");
        gemRobot.setIcon(org.bukkit.Material.DIAMOND_BLOCK);
        minerTypes.put(gemRobot.getId(), gemRobot);

        // Token Golem
        MinerType tokenGolem = new MinerType("token_golem");
        tokenGolem.setDisplayName("&6Token Golem");
        tokenGolem.setCategory(MinerCategory.GOLEM);
        tokenGolem.setResourceType(ResourceType.TOKENS);
        tokenGolem.setBaseProduction(150);
        tokenGolem.setBaseStorage(1500);
        tokenGolem.setPurchaseCost(15000);
        tokenGolem.setPurchaseCurrency("tokens");
        tokenGolem.setDescription("Ancient golem that gathers tokens");
        tokenGolem.setIcon(org.bukkit.Material.TERRACOTTA);
        minerTypes.put(tokenGolem.getId(), tokenGolem);

        // Beacon Golem
        MinerType beaconGolem = new MinerType("beacon_golem");
        beaconGolem.setDisplayName("&bBeacon Golem");
        beaconGolem.setCategory(MinerCategory.GOLEM);
        beaconGolem.setResourceType(ResourceType.BEACONS);
        beaconGolem.setBaseProduction(75);
        beaconGolem.setBaseStorage(750);
        beaconGolem.setPurchaseCost(30000);
        beaconGolem.setPurchaseCurrency("tokens");
        beaconGolem.setDescription("Ancient golem that gathers beacons");
        beaconGolem.setIcon(org.bukkit.Material.PRISMARINE);
        minerTypes.put(beaconGolem.getId(), beaconGolem);

        // Gem Golem
        MinerType gemGolem = new MinerType("gem_golem");
        gemGolem.setDisplayName("&dGem Golem");
        gemGolem.setCategory(MinerCategory.GOLEM);
        gemGolem.setResourceType(ResourceType.GEMS);
        gemGolem.setBaseProduction(40);
        gemGolem.setBaseStorage(400);
        gemGolem.setPurchaseCost(50000);
        gemGolem.setPurchaseCurrency("tokens");
        gemGolem.setDescription("Ancient golem that gathers gems");
        gemGolem.setIcon(org.bukkit.Material.AMETHYST_BLOCK);
        minerTypes.put(gemGolem.getId(), gemGolem);
    }

    public void saveMiners() {
        FileConfiguration config = plugin.getConfigManager().getMinersConfig();

        // Clear existing
        config.set("miners", null);

        // Save all miner types
        for (MinerType type : minerTypes.values()) {
            ConfigurationSection section = config.createSection("miners." + type.getId());
            type.save(section);
        }

        plugin.getConfigManager().saveMinersConfig();
    }

    public MinerType getMinerType(String id) {
        return minerTypes.get(id.toLowerCase());
    }

    public Collection<MinerType> getAllMinerTypes() {
        return Collections.unmodifiableCollection(minerTypes.values());
    }

    public List<MinerType> getMinersByCategory(MinerCategory category) {
        List<MinerType> result = new ArrayList<>();
        for (MinerType type : minerTypes.values()) {
            if (type.getCategory() == category) {
                result.add(type);
            }
        }
        return result;
    }

    public List<MinerType> getMinersByResource(ResourceType resourceType) {
        List<MinerType> result = new ArrayList<>();
        for (MinerType type : minerTypes.values()) {
            if (type.getResourceType() == resourceType) {
                result.add(type);
            }
        }
        return result;
    }

    public int getMinerTypeCount() {
        return minerTypes.size();
    }

    public boolean minerTypeExists(String id) {
        return minerTypes.containsKey(id.toLowerCase());
    }

    public MinerType createMinerType(String id) {
        if (minerTypeExists(id)) {
            return null;
        }

        MinerType type = new MinerType(id);
        minerTypes.put(type.getId(), type);
        saveMiners();
        return type;
    }

    public boolean deleteMinerType(String id) {
        MinerType removed = minerTypes.remove(id.toLowerCase());
        if (removed != null) {
            saveMiners();
            return true;
        }
        return false;
    }
}
