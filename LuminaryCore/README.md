# LuminaryCore

**The foundation of the Luminary plugin ecosystem.** This core plugin provides shared utilities, configuration management, and a complete server moderation system that all other Luminary plugins build upon.

Built for Paper 1.20+ servers.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Staff Panel](images/staff-panel.png) -->
<!-- ![Punishment History](images/history-gui.png) -->
<!-- ![Broadcast Example](images/broadcast.png) -->

*Screenshots coming soon - showing staff tools, punishment system, and admin features in action.*

---

## Why I Built This

Every plugin I made needed the same things - color code parsing, config handling, common utilities. Instead of copying code everywhere, I built a central core that handles all of it. The moderation system came naturally since every server needs staff tools anyway.

---

## Features

### Moderation Suite
Full punishment system with history tracking. Bans, mutes, warns, kicks - all logged and searchable.

- **Permanent & Temp Bans** - Duration parsing like "7d" or "2h30m"
- **Mute System** - Stop players from chatting, with temp and permanent options
- **Warning System** - Track warnings with automatic escalation options
- **Freeze Command** - Lock players in place for screensharing or investigation
- **Punishment History** - Full audit log for each player

### Staff Tools
- **Vanish Mode** - Go invisible with configurable settings for interactions
- **Staff Chat** - Private channel for team communication
- **Broadcast System** - Server-wide announcements with formatting
- **Maintenance Mode** - Lock the server while allowing staff to join

### Developer API
Other Luminary plugins hook into this for:
- Text colorization and formatting
- Configuration management
- Common utilities and helpers
- Player data handling

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ban <player> [reason]` | Permanent ban | `luminarycore.ban` |
| `/tempban <player> <time> [reason]` | Temporary ban | `luminarycore.tempban` |
| `/unban <player>` | Remove a ban | `luminarycore.unban` |
| `/mute <player> [reason]` | Permanent mute | `luminarycore.mute` |
| `/tempmute <player> <time> [reason]` | Temporary mute | `luminarycore.tempmute` |
| `/unmute <player>` | Remove a mute | `luminarycore.unmute` |
| `/kick <player> [reason]` | Kick from server | `luminarycore.kick` |
| `/warn <player> <reason>` | Issue a warning | `luminarycore.warn` |
| `/freeze <player>` | Toggle freeze | `luminarycore.freeze` |
| `/history <player>` | View punishments | `luminarycore.history` |
| `/vanish` | Toggle invisibility | `luminarycore.vanish` |
| `/staffchat <message>` | Staff-only chat | `luminarycore.staffchat` |
| `/broadcast <message>` | Server announcement | `luminarycore.broadcast` |
| `/maintenance [on/off]` | Toggle maintenance | `luminarycore.maintenance` |

---

## Installation

1. Download the latest release
2. Drop `LuminaryCore-1.0.0.jar` into your `plugins` folder
3. Start (or restart) your server
4. Configure settings in `plugins/LuminaryCore/config.yml`

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for modern text components
- **YAML** for configuration

---

## Project Structure

```
LuminaryCore/
├── src/main/java/com/luminary/core/
│   ├── LuminaryCore.java          # Main plugin class
│   ├── command/                   # Command handlers
│   │   ├── moderation/            # Ban, mute, kick, etc.
│   │   └── staff/                 # Vanish, staffchat, etc.
│   ├── config/                    # Config management
│   ├── data/                      # Player data & punishment records
│   ├── listener/                  # Event handlers
│   └── util/                      # Shared utilities
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## What I Learned

Building this taught me a lot about plugin architecture - how to structure code so other plugins can depend on it without things getting messy. The punishment system was a good exercise in data persistence and making sure nothing gets lost on restarts.

---

## Part of the Luminary Suite

This is the core plugin for a complete prison server setup. Other plugins in the suite:

- **LuminaryEconomy** - Multi-currency system
- **LuminaryRanks** - Prison rank progression
- **LuminaryMines** - Private player mines
- **LuminaryEnchants** - Custom pickaxe enchants
- [View all plugins](#)

---

## License

This project is part of my personal portfolio. Feel free to look around the code.
