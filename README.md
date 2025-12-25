# Luminary Prison Plugins

A complete plugin suite for Minecraft prison servers. Built from scratch to work together seamlessly - no dependency hell, no configuration nightmares, just plugins that talk to each other out of the box.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Server Gameplay](images/gameplay.png) -->
<!-- ![Scoreboard Sidebar](images/sidebar.png) -->
<!-- ![Mining with Enchants](images/mining.png) -->
<!-- ![Crate Opening](images/crate-spin.gif) -->

*Screenshots coming soon*

---

## The Problem

Setting up a prison server usually means:
- 15+ plugins from different developers
- Hours of configuration to make them work together
- Placeholder conflicts, dependency issues, version mismatches
- Features that almost do what you want but not quite

I built this suite to solve that. One ecosystem, one style, everything integrated.

---

## The Plugins

| Plugin | Description |
|--------|-------------|
| [**LuminaryCore**](./LuminaryCore) | Foundation plugin with moderation, staff tools, and shared utilities |
| [**LuminaryEconomy**](./LuminaryEconomy) | Multi-currency system with live scoreboard sidebar |
| [**LuminaryRanks**](./LuminaryRanks) | A-Z rank progression with prestiges and rebirths |
| [**LuminaryGroups**](./LuminaryGroups) | Permission groups with chat formatting |
| [**LuminaryMines**](./LuminaryMines) | Private mines per player using WorldEdit schematics |
| [**LuminaryMiners**](./LuminaryMiners) | AFK mining bots for passive income |
| [**LuminaryEnchants**](./LuminaryEnchants) | Custom pickaxe enchants (Explosive, Laser, Jackhammer, etc.) |
| [**LuminaryCrates**](./LuminaryCrates) | Virtual crates with spinning animations |
| [**LuminaryBackpacks**](./LuminaryBackpacks) | Auto-pickup backpacks with auto-sell |

---

## How They Connect

```
                    ┌─────────────────┐
                    │  LuminaryCore   │
                    │   (Required)    │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ LuminaryRanks │◄──►│LuminaryEconomy│◄──►│LuminaryGroups │
│  (Ranks A-Z)  │    │  (Currencies) │    │ (Permissions) │
└───────┬───────┘    └───────┬───────┘    └───────────────┘
        │                    │
        │         ┌──────────┼──────────┐
        │         │          │          │
        ▼         ▼          ▼          ▼
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  LuminaryMines ◄─► LuminaryEnchants ◄─► LuminaryBackpacks│
│       │                   │                             │
│       ▼                   ▼                             │
│  LuminaryMiners    LuminaryCrates                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Key Integrations:**
- Scoreboard shows rank, prestige, rebirth, group, and all currencies
- Enchants only break blocks inside mine regions (protects walls/ladders)
- Crates can reward currency, keys, enchanted picks, rank-ups
- Backpacks
- Chat shows group prefix + rebirth + prestige + rank together

---

## Quick Start

### Requirements
- Paper 1.20.1+ (or Purpur)
- Java 17+
- WorldEdit (for LuminaryMines)

### Installation

1. **Download all JARs** from releases
2. **Drop into plugins folder** in this order:
   ```
   1. LuminaryCore (always first)
   2. LuminaryEconomy
   3. LuminaryRanks
   4. LuminaryGroups
   5. Everything else
   ```
3. **Start the server** - configs generate automatically
4. **Configure currencies** in LuminaryEconomy
5. **Set up ranks** in LuminaryRanks
6. **Create mine schematics** with WorldEdit + LuminaryMines

### First-Time Setup Checklist

- [ ] Configure currencies (tokens, gems, beacons, etc.)
- [ ] Set up A-Z ranks with costs
- [ ] Create prestige and rebirth tiers
- [ ] Build and save mine schematics
- [ ] Set mine spawn points
- [ ] Configure enchant costs and effects
- [ ] Set up crate rewards
- [ ] Create permission groups for staff

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for modern text handling
- **WorldEdit API** for schematic operations
- **PlaceholderAPI** support (optional)
- **YAML** configuration throughout

---

## Project Structure

```
Server-Plugins/
├── LuminaryCore/           # Foundation - moderation, utilities
├── LuminaryEconomy/        # Multi-currency + scoreboard
├── LuminaryRanks/          # A-Z, Prestige, Rebirth
├── LuminaryGroups/         # Permissions + chat formatting
├── LuminaryMines/          # Private mines + schematics
├── LuminaryMiners/         # AFK mining bots
├── LuminaryEnchants/       # Custom pickaxe enchants
├── LuminaryCrates/         # Virtual crates
├── LuminaryBackpacks/      # Auto-pickup storage
```

Each plugin has its own README with detailed documentation.

---

## Features at a Glance

### For Players
- Rank up from A to Z, prestige, rebirth for permanent bonuses
- Own a private mine that resets automatically
- Custom enchants that make mining satisfying
- Backpacks that auto-collect and auto-sell
- Virtual crates with animated openings
- AFK miners that work while you're offline

### For Staff
- Full moderation suite (ban, mute, kick, warn, freeze)
- Vanish mode and staff chat
- Punishment history tracking
- Maintenance mode

### For Owners
- Everything configurable via YAML
- No database required (flat file storage)
- Plugins work independently or together
- Easy to add custom rewards and progression

---

## Why I Built This

I wanted to run a prison server without the usual plugin chaos. Every "must-have" plugin came with its own problems - weird configs, abandoned updates, conflicts with other plugins.

So I built my own ecosystem. Each plugin does one thing well, and they all speak the same language. The code is clean, the configs make sense, and everything just works together.

This is also a portfolio piece showing what I can build with Java and the Bukkit/Paper API.

---

## What I Learned

Building this suite taught me:
- **Plugin architecture** - designing systems that other plugins can hook into
- **Cross-plugin communication** - soft dependencies, lazy loading, event-driven updates
- **WorldEdit integration** - schematics, clipboard transforms, paste operations
- **Custom enchant systems** - NBT/PDC storage, effect triggering, region protection
- **GUI design** - inventory-based menus, animations, user experience
- **Data persistence** - player data, punishment records, offline calculations
- **Performance** - handling hundreds of players, efficient block operations

---

## Contributing

This is primarily a personal project, but if you spot bugs or have suggestions, feel free to open an issue.

---

## License

Portfolio project. Code is available for review.

---

## Contact

Built by Luminary

<!-- Add your contact info -->
<!-- - Discord: yourdiscord -->
<!-- - Twitter: @yourhandle -->
<!-- - Email: you@email.com -->
