package com.luminary.groups.group;

import com.luminary.groups.LuminaryGroups;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all groups and their hierarchy.
 */
public class GroupManager {

    private final LuminaryGroups plugin;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private Group defaultGroup;

    public GroupManager(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    public void loadGroups() {
        groups.clear();
        defaultGroup = null;

        FileConfiguration config = plugin.getConfigManager().getGroupsConfig();
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");

        if (groupsSection != null) {
            for (String groupId : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupId);
                if (groupSection != null) {
                    Group group = new Group(groupId, groupSection);
                    groups.put(group.getId(), group);

                    if (group.isDefault()) {
                        defaultGroup = group;
                    }
                }
            }
        }

        // Ensure default group exists
        if (defaultGroup == null) {
            String defaultId = plugin.getConfigManager().getDefaultGroup();
            defaultGroup = groups.get(defaultId);

            if (defaultGroup == null) {
                // Create a basic default group
                defaultGroup = new Group("default");
                defaultGroup.setDisplayName("Member");
                defaultGroup.setPrefix("&7[Member]");
                defaultGroup.setDefault(true);
                groups.put("default", defaultGroup);
                plugin.getLogger().info("Created default group.");
            }
        }

        plugin.getLogger().info("Loaded " + groups.size() + " groups.");
    }

    public void saveGroups() {
        FileConfiguration config = plugin.getConfigManager().getGroupsConfig();

        // Clear existing groups section
        config.set("groups", null);

        // Save all groups
        for (Group group : groups.values()) {
            ConfigurationSection section = config.createSection("groups." + group.getId());
            group.save(section);
        }

        plugin.getConfigManager().saveGroupsConfig();
    }

    public Group getGroup(String id) {
        return groups.get(id.toLowerCase());
    }

    public Group getDefaultGroup() {
        return defaultGroup;
    }

    public Collection<Group> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public int getGroupCount() {
        return groups.size();
    }

    public boolean groupExists(String id) {
        return groups.containsKey(id.toLowerCase());
    }

    public Group createGroup(String id) {
        if (groupExists(id)) {
            return null;
        }

        Group group = new Group(id);
        groups.put(group.getId(), group);
        saveGroups();
        return group;
    }

    public boolean deleteGroup(String id) {
        if (id.equalsIgnoreCase("default")) {
            return false; // Cannot delete default group
        }

        Group removed = groups.remove(id.toLowerCase());
        if (removed != null) {
            saveGroups();
            return true;
        }
        return false;
    }

    /**
     * Get all permissions for a group, including inherited permissions.
     */
    public Set<String> getAllPermissions(Group group) {
        Set<String> allPerms = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>();

        collectPermissions(group, allPerms, visited);
        return allPerms;
    }

    private void collectPermissions(Group group, Set<String> permissions, Set<String> visited) {
        if (group == null || visited.contains(group.getId())) {
            return;
        }
        visited.add(group.getId());

        // Add this group's permissions
        permissions.addAll(group.getPermissions());

        // Collect from inherited groups
        for (String inheritId : group.getInheritedGroups()) {
            Group inherited = getGroup(inheritId);
            collectPermissions(inherited, permissions, visited);
        }
    }

    /**
     * Get groups sorted by weight (highest first).
     */
    public List<Group> getGroupsSortedByWeight() {
        List<Group> sorted = new ArrayList<>(groups.values());
        sorted.sort((a, b) -> Integer.compare(b.getWeight(), a.getWeight()));
        return sorted;
    }

    /**
     * Get the highest weight group from a set of group IDs.
     */
    public Group getHighestPriorityGroup(Set<String> groupIds) {
        Group highest = null;
        for (String id : groupIds) {
            Group group = getGroup(id);
            if (group != null) {
                if (highest == null || group.getWeight() > highest.getWeight()) {
                    highest = group;
                }
            }
        }
        return highest != null ? highest : defaultGroup;
    }
}
