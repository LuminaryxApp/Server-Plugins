# LuminaryMines

Private mine system for prison servers. Each player gets their own mine that resets automatically.

## How it works

Players can create their own private mine using a schematic template. The mine spawns in a dedicated void world so it doesn't clutter up your main world. Mines reset on a timer or when a certain percentage of blocks have been mined.

## Features

### Private Mines
- Each player owns their own mine
- Mines are placed in a separate void world
- Whitelist system so players can invite friends

### Schematics
Set up mine templates using WorldEdit schematics. Players create mines from these templates. You can have different tiers/sizes of mines.

### Auto Reset
- Timer-based resets (configurable interval)
- Threshold-based resets (reset when X% mined)
- Warning messages before reset
- Players stay in place during reset - blocks just refill around them

### Block Composition
Configure what blocks spawn in each mine. Set percentages for different ore types:
```
STONE: 60
COAL_ORE: 20
IRON_ORE: 15
DIAMOND_ORE: 5
```

## Commands

**Player Commands:**
- `/mine` - open the mine menu
- `/mine create` - create your mine
- `/mine tp` - teleport to your mine
- `/mine reset` - manually reset your mine
- `/mine whitelist add/remove <player>` - manage who can use your mine

**Admin Commands:**
- `/mineadmin schematic <name>` - create a schematic from WorldEdit selection
- `/mineadmin setspawn <schematic>` - set spawn point for a schematic
- `/mineadmin reset <player>` - force reset a player's mine

## Setup

1. Install LuminaryCore and WorldEdit
2. Build a mine structure in-game
3. Select it with WorldEdit (`//wand`, select corners)
4. Run `/mineadmin schematic <name>` to save it
5. Stand where players should spawn and run `/mineadmin setspawn <name>`
6. Players can now create mines using that schematic

## Dependencies

**Required:** LuminaryCore, WorldEdit

**Optional:** LuminaryEconomy, PlaceholderAPI
