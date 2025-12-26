Luminary Prison Plugins

A complete plugin suite for Minecraft prison servers. Built from scratch to work together seamlessly - no dependency hell, no configuration nightmares, just plugins that talk to each other out of the box.

---

## Screenshots
Robots Plugin
https://youtu.be/Aahp-R1MI4A
<img width="1920" height="1009" alt="2025-12-26_05 55 53" src="https://github.com/user-attachments/assets/f601fbce-4847-4ebc-99b1-47e6220d4c21" />
<img width="1920" height="1009" alt="2025-12-26_05 55 40" src="https://github.com/user-attachments/assets/a723a4ad-c849-4ba2-971b-5e57a3dd68d0" />
<img width="1920" height="1009" alt="2025-12-26_05 55 37" src="https://github.com/user-attachments/assets/4560b4ad-2622-496a-8755-1e9466a2829f" />

Eco Plugin
https://youtu.be/qaCdBuClMTg
<img width="1920" height="1009" alt="2025-12-26_05 53 41" src="https://github.com/user-attachments/assets/75ad390b-d4ac-4958-aa87-09c6ec944f2f" />
<img width="1920" height="1009" alt="2025-12-26_05 53 46" src="https://github.com/user-attachments/assets/a511b722-9f91-4b2f-a639-f1da4be6a662" />
<img width="1920" height="1009" alt="2025-12-26_06 11 09" src="https://github.com/user-attachments/assets/a7c818ea-9964-4041-87f5-567a747df244" />

Crates Plugin
https://youtu.be/C9Lfll38osI
<img width="1920" height="1009" alt="2025-12-26_05 50 41" src="https://github.com/user-attachments/assets/baaeaf9f-cf40-4a26-9a97-8a210501a5ad" />
<img width="1920" height="1009" alt="2025-12-26_05 50 43" src="https://github.com/user-attachments/assets/9574a937-6a50-4eb2-96ef-44159372f405" />
<img width="1920" height="1009" alt="2025-12-26_06 01 09" src="https://github.com/user-attachments/assets/16d2c8a3-f9e6-486a-acf8-1159e56efd93" />

Enchant Plugin
https://youtu.be/80yVm3BU0eo
<img width="1920" height="1009" alt="2025-12-26_05 37 25" src="https://github.com/user-attachments/assets/d0fd9ea1-fea6-4c09-a814-66b18004bb20" />
<img width="1920" height="1009" alt="2025-12-26_05 37 06" src="https://github.com/user-attachments/assets/8eae78b5-a263-427f-96e5-88130644361c" />
<img width="1920" height="1009" alt="2025-12-26_05 38 49" src="https://github.com/user-attachments/assets/8cae4247-6ab1-411d-951b-6736d019b3c4" />

Pmine Plugin
https://youtu.be/BiuVsUFdStI
<img width="1920" height="1009" alt="2025-12-26_05 46 45" src="https://github.com/user-attachments/assets/f6fb6bb1-0fb3-4fcc-9a10-9d5d99ad6bff" />
<img width="1920" height="1009" alt="2025-12-26_05 46 02" src="https://github.com/user-attachments/assets/3db74a59-31e1-4031-9b18-7bd3cbec5586" />
<img width="1920" height="1009" alt="2025-12-26_05 46 56" src="https://github.com/user-attachments/assets/152f305b-64e7-4516-bac9-4ddde0b103e8" />

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
