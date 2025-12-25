# LuminaryEnchants

**Custom pickaxe enchantments for prison mining.** Explosive, Laser, Jackhammer, and more. These aren't vanilla enchants - they're designed specifically for the prison grind.

Built for Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Enchant Menu](images/enchant-menu.png) -->
<!-- ![Explosive Effect](images/explosive-effect.png) -->
<!-- ![Laser Breaking Blocks](images/laser-effect.png) -->
<!-- ![Jackhammer Layer](images/jackhammer.png) -->

*Screenshots coming soon - showing the enchant GUI and effects in action.*

---

## Why I Built This

Vanilla enchants are boring for prison. Fortune III doesn't cut it when you're mining thousands of blocks. These custom enchants make mining faster, more satisfying, and give players something meaningful to spend tokens on.

---

## Features

### Pickaxe-Only Enchants
All enchants work exclusively on pickaxes. Each one has multiple levels:

**Explosive**
Breaks blocks in a radius. Higher levels = bigger explosions.

**Laser**
Breaks blocks in a straight line from where you're looking.

**Jackhammer**
Destroys an entire horizontal layer. Satisfying for clearing mines.

**Speed**
Adds haste effect while mining. Stack it high for instant breaks.

**Fortune+**
Enhanced fortune beyond vanilla limits.

**Auto-Smelt**
Ores drop smelted ingots directly.

### Token-Based Upgrades
Players spend currency (usually tokens) to level up enchants. Higher levels cost exponentially more - creates a nice progression curve.

### Mine Region Protection
Enchants are smart about what they break. If LuminaryMines is installed:
- Effects only break blocks inside the mine region
- Walls, ladders, and borders are protected
- No accidental destruction of mine structure

---

## Enchant Effects Preview

### Explosive
<!-- ![3x3 explosion of blocks](images/explosive-demo.gif) -->
*Breaks a 3x3 (up to 5x5 at max level) sphere of blocks.*

### Laser
<!-- ![Line of blocks breaking](images/laser-demo.gif) -->
*Pierces through blocks in the direction you're facing.*

### Jackhammer
<!-- ![Entire layer disappearing](images/jackhammer-demo.gif) -->
*Clears the entire Y-level. Very satisfying.*

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/enchants` or `/pe` | Open enchant menu | `luminaryenchants.use` |
| `/pe reload` | Reload configuration | `luminaryenchants.reload` |
| `/pe apply <enchant> <level> [player]` | Force apply enchant | `luminaryenchants.admin` |
| `/pe clear [player]` | Remove all enchants | `luminaryenchants.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Install **LuminaryMines** (recommended for region protection)
3. Drop `LuminaryEnchants-1.0.0.jar` into plugins
4. Configure enchants in `config.yml`
5. Set costs, max levels, and effect parameters

---

## Configuration Example

```yaml
enchants:
  explosive:
    display-name: "&c&lExplosive"
    description: "Breaks blocks in a radius"
    max-level: 5
    base-cost: 5000
    cost-multiplier: 2.5  # Each level costs 2.5x more
    effect:
      base-radius: 1
      radius-per-level: 0.5

  laser:
    display-name: "&b&lLaser"
    description: "Breaks blocks in a line"
    max-level: 10
    base-cost: 3000
    cost-multiplier: 2.0
    effect:
      base-length: 5
      length-per-level: 2
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **NBT/PDC** for storing enchant data on items
- **Raycasting** for laser targeting

---

## Project Structure

```
LuminaryEnchants/
├── src/main/java/com/luminary/enchants/
│   ├── LuminaryEnchants.java      # Main plugin class
│   ├── command/                   # Commands
│   ├── config/                    # Config management
│   ├── enchant/                   # Enchant definitions
│   ├── gui/                       # Enchant menu
│   ├── listener/                  # Block break handler
│   ├── trigger/                   # Effect triggers
│   │   └── effects/               # Individual effect classes
│   └── api/                       # MineRegionProvider interface
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## Mine Protection System

The plugin exposes a `MineRegionProvider` interface that LuminaryMines implements:

```java
// Before breaking a block, check if it's allowed
if (mineProvider.isAvailable()) {
    if (!mineProvider.canPlayerBreakAt(player, block.getLocation())) {
        return; // Don't break - outside mine region
    }
}
```

This keeps enchant effects contained to the mineable area.

---

## What I Learned

Building a custom enchant system taught me about NBT/PersistentDataContainer for storing custom data on items. The region protection system required designing a clean API that other plugins could implement. Raycasting for the laser effect was a fun geometry challenge.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryMines** - Region protection
- **LuminaryEconomy** - Currency for upgrades
- **LuminaryCrates** - Give enchanted picks as rewards
- [View all plugins](#)

---

## License

Portfolio project.
