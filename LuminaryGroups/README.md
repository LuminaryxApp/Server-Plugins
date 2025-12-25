# LuminaryGroups

**Permission groups with chat formatting.** Create staff ranks, donor tiers, and custom groups - all manageable in-game. No external permission plugins needed.

Built for Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Chat Formatting](images/chat-example.png) -->
<!-- ![Group List](images/group-list.png) -->
<!-- ![Permission Editor](images/perm-editor.png) -->

*Screenshots coming soon - showing chat prefixes, group management, and the permission system in action.*

---

## Why I Built This

I needed a simple groups plugin that works with my other plugins without the complexity of LuckPerms or PEX. Something where I can do `/group create VIP` and immediately start assigning players. It also needed to play nice with my ranks plugin so chat shows both prefixes.

---

## Features

### In-Game Management
Do everything with commands:
- Create and delete groups
- Set prefixes and suffixes
- Add/remove permissions
- Assign players to groups

No config editing required for basic setup.

### Chat Formatting
Automatically formats chat to show:
```
[Admin] [P5] PlayerName: Hello everyone!
  ↑       ↑
Group   Rank (from LuminaryRanks)
```

### Inheritance
Groups can inherit from other groups. VIP inherits from Default, MVP inherits from VIP, etc. Permissions cascade properly.

### Weight System
When a player has multiple groups, the one with highest weight takes priority for prefix display.

---

## Chat Preview

<!-- ![Example of formatted chat with prefixes](images/chat-preview.png) -->

```
[Owner] [R2] [P10] Steve: Welcome to the server!
[Admin] [P5] Alex: Thanks for joining
[VIP] [Z] NewPlayer: Happy to be here
[Member] [A] Guest: How do I rank up?
```

*Groups, rebirths, prestiges, and ranks all showing together.*

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/group list` | List all groups | `luminarygroups.admin` |
| `/group info <group>` | View group details | `luminarygroups.admin` |
| `/group create <name>` | Create a group | `luminarygroups.admin` |
| `/group delete <name>` | Delete a group | `luminarygroups.admin` |
| `/group setprefix <group> <prefix>` | Set prefix | `luminarygroups.admin` |
| `/group setsuffix <group> <suffix>` | Set suffix | `luminarygroups.admin` |
| `/group addperm <group> <perm>` | Add permission | `luminarygroups.admin` |
| `/group removeperm <group> <perm>` | Remove permission | `luminarygroups.admin` |
| `/group setplayer <player> <group>` | Assign player | `luminarygroups.admin` |
| `/group inherit <group> <parent>` | Set inheritance | `luminarygroups.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Drop `LuminaryGroups-1.0.0.jar` into plugins
3. Start the server
4. Create your groups with `/group create <name>`
5. Set up prefixes and permissions as needed

---

## Quick Setup Example

```
/group create Owner
/group setprefix Owner &4[Owner]
/group addperm Owner *
/group setweight Owner 100

/group create Admin
/group setprefix Admin &c[Admin]
/group addperm Admin luminarycore.ban
/group addperm Admin luminarycore.kick
/group setweight Admin 90

/group create VIP
/group setprefix VIP &6[VIP]
/group inherit VIP default
/group setweight VIP 50

/group setplayer Notch Owner
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for chat formatting
- **PlaceholderAPI** support

---

## Project Structure

```
LuminaryGroups/
├── src/main/java/com/luminary/groups/
│   ├── LuminaryGroups.java        # Main plugin class
│   ├── command/                   # Group management commands
│   ├── config/                    # Config management
│   ├── data/                      # Player-group assignments
│   ├── group/                     # Group class and logic
│   ├── hook/                      # LuminaryRanks integration
│   └── listener/                  # Chat formatting listener
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## Integration

Works with other Luminary plugins:
- **LuminaryRanks** - Combines rank prefix with group prefix in chat
- **LuminaryEconomy** - Group info shows on scoreboard sidebar

---

## What I Learned

Building a permission system from scratch taught me about inheritance chains and permission resolution. The chat formatting integration with LuminaryRanks required careful hook management since plugin load order isn't always predictable.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryRanks** - Prison rank progression
- **LuminaryEconomy** - Shows group on sidebar
- [View all plugins](#)

---

## License

Portfolio project.
