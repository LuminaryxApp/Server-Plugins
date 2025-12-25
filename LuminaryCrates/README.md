# LuminaryCrates

**Virtual crate system with spinning animations.** No physical crate locations needed - everything runs through a sleek GUI. Keys are tracked per-player, rewards are fully configurable.

Built for Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Crate Menu](images/crate-menu.png) -->
<!-- ![Spin Animation](images/spin-animation.gif) -->
<!-- ![Reward Win](images/reward-win.png) -->
<!-- ![Preview Screen](images/reward-preview.png) -->

*Screenshots coming soon - showing the crate selection, spinning animation, and reward system.*

---

## Why I Built This

Physical crate locations are annoying to set up and maintain. They cause lag when lots of players cluster around them. A virtual system is cleaner - open crates from anywhere, no walking to spawn, no crowding.

---

## Features

### Virtual Everything
- No physical crate blocks
- Keys are virtual (not inventory items)
- Open crates from the `/crates` menu anywhere

### Spinning Animation
The classic crate experience:
1. Click to open
2. Rewards spin past
3. Slows down dramatically
4. Lands on your prize
5. Celebration effects

### Configurable Rewards
Set up any rewards you want:
- Currency (tokens, gems, etc.)
- Items with custom names/lore
- Commands (give ranks, permissions, etc.)
- Enchanted pickaxes
- Other crate keys

### Rarity Weights
Control drop rates with weights. Higher weight = more common. Easy to balance.

---

## Animation Preview

<!-- ![Crate spinning animation](images/crate-spin-demo.gif) -->

```
┌──────────────────────────────────────────┐
│  [Rare] [Epic] [Rare] [>>LEGENDARY<<]    │
│                         ▲                │
│                      Winner!             │
└──────────────────────────────────────────┘
```

*The spinner builds tension before revealing the reward.*

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/crates` | Open crate selection menu | `luminarycrates.use` |
| `/crates open <type>` | Quick-open a specific crate | `luminarycrates.use` |
| `/crates keys` | View your key counts | `luminarycrates.use` |
| `/crates preview <type>` | Preview possible rewards | `luminarycrates.use` |
| `/crates give <player> <crate> <amount>` | Give keys | `luminarycrates.give` |
| `/crates giveall <crate> <amount>` | Give keys to everyone | `luminarycrates.admin` |
| `/crates reload` | Reload configuration | `luminarycrates.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Drop `LuminaryCrates-1.0.0.jar` into plugins
3. Configure crate types in `config.yml`
4. Set up rewards with weights
5. Decide how players earn keys (voting, ranks, shop, etc.)

---

## Configuration Example

```yaml
crates:
  vote:
    display-name: "&a&lVote Crate"
    icon: EMERALD
    rewards:
      - id: tokens_small
        display: "&e1,000 Tokens"
        commands:
          - "eco give {player} tokens 1000"
        weight: 50

      - id: tokens_large
        display: "&6&l10,000 Tokens"
        commands:
          - "eco give {player} tokens 10000"
        weight: 10
        rarity: rare

      - id: legendary_pick
        display: "&d&lLegendary Pickaxe"
        commands:
          - "give {player} diamond_pickaxe{...}"
        weight: 1
        rarity: legendary

  legendary:
    display-name: "&6&lLegendary Crate"
    icon: NETHER_STAR
    # Better rewards, harder to get keys
```

---

## Rarity System

Rewards can have rarity tags that affect display:

| Rarity | Color | Typical Weight |
|--------|-------|----------------|
| Common | White | 50-100 |
| Uncommon | Green | 25-50 |
| Rare | Blue | 10-25 |
| Epic | Purple | 5-10 |
| Legendary | Gold | 1-5 |

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for animations
- **Scheduler** for smooth spinning

---

## Project Structure

```
LuminaryCrates/
├── src/main/java/com/luminary/crates/
│   ├── LuminaryCrates.java        # Main plugin class
│   ├── command/                   # Commands
│   ├── config/                    # Config management
│   ├── crate/                     # Crate definitions
│   ├── gui/                       # Crate menus
│   │   └── CrateMenuManager.java  # Animation handling
│   ├── key/                       # Key management
│   └── reward/                    # Reward processing
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## The Animation System

The spin animation uses a scheduled task:

```java
// Simplified animation logic
int speed = 2;  // Start fast
for (int i = 0; i < 40; i++) {
    // Show next item in row
    updateDisplay(items[(index + i) % items.length]);

    // Gradually slow down
    if (i > 20) speed += 1;
    if (i > 30) speed += 2;

    wait(speed);
}
// Final reveal with effects
revealWinner();
```

---

## What I Learned

Animating GUIs in Minecraft is tricky since you're working with inventory updates. Had to balance smoothness with performance. The weighted random selection system was a good exercise in probability - making sure the math actually produces the expected distributions.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryEconomy** - Currency rewards
- **LuminaryEnchants** - Enchanted items as prizes
- **LuminaryRanks** - Keys as rankup rewards
- [View all plugins](#)

---

## License

Portfolio project.
