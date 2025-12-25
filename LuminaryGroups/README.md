# LuminaryGroups

Permission groups with chat formatting. Handles prefixes, suffixes, and permissions without needing a separate permissions plugin.

## Features

### Permission Groups
Create groups like Admin, Mod, VIP, etc. Each group can have:
- A display name and prefix/suffix
- A list of permissions
- Inheritance from other groups
- A weight for priority ordering

### Chat Formatting
Automatically formats chat messages to show group prefixes. If LuminaryRanks is installed, it can combine the group prefix with rank/prestige/rebirth prefixes.

### In-Game Management
Most stuff can be done with commands, no need to edit configs manually:
- Create and delete groups
- Set prefixes, suffixes, permissions
- Add players to groups
- Set inheritance chains

## Commands

`/group` handles everything:
- `/group create <name>` - make a new group
- `/group delete <name>` - remove a group
- `/group setprefix <group> <prefix>` - set the chat prefix
- `/group addperm <group> <permission>` - add a permission
- `/group setplayer <player> <group>` - assign a player to a group
- `/group info <group>` - view group details

## Integration

Works alongside LuminaryRanks - the rank prefix and group prefix can both show in chat. The scoreboard in LuminaryEconomy also pulls group info.

## Dependencies

**Required:** LuminaryCore

**Optional:** LuminaryRanks, LuminaryEconomy, PlaceholderAPI
