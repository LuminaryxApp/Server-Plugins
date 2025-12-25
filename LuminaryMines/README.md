# LuminaryMines

**Private mines for each player.** Using WorldEdit schematics, every player gets their own mine in a dedicated void world. Mines reset automatically and can be upgraded.

Built for prison servers on Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Mine Overview](images/mine-overview.png) -->
<!-- ![Mine Menu GUI](images/mine-menu.png) -->
<!-- ![Reset Animation](images/mine-reset.png) -->
<!-- ![Void World](images/void-world.png) -->

*Screenshots coming soon - showing private mines, the management GUI, and reset mechanics.*

---

## Why I Built This

Public mines get crowded. Players compete for blocks, lag increases, and it's not a great experience. Private mines solve all of that - everyone gets their own space to grind in peace. The void world setup keeps your main world clean.

---

## Features

### Personal Mines
Every player owns their own mine. No sharing, no competition. They can:
- Teleport to it anytime
- Manually trigger resets
- Invite friends via whitelist
- Upgrade to bigger sizes

### Schematic-Based
Build mine templates with WorldEdit, save them as schematics. Players create mines from these templates. Want different tiers? Just make different schematics.

### Void World Isolation
Mines spawn in a dedicated void world:
- Keeps your main world clean
- No lag from hundreds of mines
- Players teleport in and out seamlessly

### Smart Resets
- **Timer-based**: Reset every X minutes
- **Threshold-based**: Reset when 80% mined
- **Players stay put**: Blocks refill around them, no annoying teleports

### Block Composition
Configure what ore mix spawns in mines:
```yaml
STONE: 50
COAL_ORE: 25
IRON_ORE: 15
DIAMOND_ORE: 10
```

---

## How It Works

```
1. Admin creates schematic template with WorldEdit
         ↓
2. Admin saves it: /mineadmin schematic <name>
         ↓
3. Admin sets spawn point: /mineadmin setspawn <name>
         ↓
4. Player creates mine: /mine create
         ↓
5. Plugin pastes schematic in void world
         ↓
6. Player teleports to their new mine
         ↓
7. Mine auto-resets on timer or threshold
```

---

## Commands

### Player Commands
| Command | Description |
|---------|-------------|
| `/mine` | Open mine management menu |
| `/mine create` | Create your private mine |
| `/mine tp` | Teleport to your mine |
| `/mine reset` | Manually reset your mine |
| `/mine whitelist add <player>` | Let someone use your mine |
| `/mine whitelist remove <player>` | Remove access |

### Admin Commands
| Command | Description |
|---------|-------------|
| `/mineadmin schematic <name>` | Save WorldEdit selection as template |
| `/mineadmin setspawn <name>` | Set spawn point for a schematic |
| `/mineadmin clearspawn <name>` | Clear custom spawn |
| `/mineadmin delete <player>` | Delete a player's mine |
| `/mineadmin reset <player>` | Force reset a mine |
| `/mineadmin tp <player>` | Teleport to any mine |

---

## Installation

1. Install **LuminaryCore** (required)
2. Install **WorldEdit** (required for schematics)
3. Drop `LuminaryMines-1.0.0.jar` into plugins
4. Create your mine schematics (see below)
5. Configure reset timers and block composition

---

## Creating a Mine Schematic

1. Build your mine structure in-game (walls, ladders, floor, etc.)
2. Select it with WorldEdit wand (`//wand`, click corners)
3. Run `/mineadmin schematic MineSmall`
4. Stand where players should spawn
5. Run `/mineadmin setspawn MineSmall`
6. Done! Players can now create mines using this template.

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **WorldEdit API** for schematic handling
- **Custom Void ChunkGenerator** for the mine world

---

## Project Structure

```
LuminaryMines/
├── src/main/java/com/luminary/mines/
│   ├── LuminaryMines.java         # Main plugin class
│   ├── command/                   # Player and admin commands
│   ├── config/                    # Config management
│   ├── mine/                      # Mine class and MineManager
│   ├── schematic/                 # WorldEdit integration
│   ├── listener/                  # Block break tracking
│   └── gui/                       # Mine management GUI
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## The Void World

When the plugin loads, it creates (or loads) a void world called `luminary_mines`. This world:
- Has no terrain generation
- Contains only player mines
- Isolates mine activity from your main world

Players teleport between worlds seamlessly.

---

## What I Learned

The WorldEdit schematic handling was complex - understanding clipboard transforms, paste origins, and offset calculations took time. Creating a void world generator and managing cross-world teleportation taught me a lot about Bukkit's world loading system.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **WorldEdit** - Required for schematics
- **LuminaryEconomy** - Currency for upgrades
- **LuminaryEnchants** - Enchants only work inside mines
- [View all plugins](#)

---

## License

Portfolio project.
