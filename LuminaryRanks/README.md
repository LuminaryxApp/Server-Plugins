# LuminaryRanks

**Prison rank progression with prestiges and rebirths.** Players climb through A-Z ranks, prestige for multipliers, then rebirth for permanent bonuses. The classic prison grind loop.

Built for Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Rank Progress GUI](images/rank-menu.png) -->
<!-- ![Prestige Confirmation](images/prestige-confirm.png) -->
<!-- ![Rebirth Rewards](images/rebirth-rewards.png) -->

*Screenshots coming soon - showing rank menus, prestige animations, and progression tracking.*

---

## Why I Built This

Every prison server needs a rank system, but most plugins either do too little or too much. I wanted something that handles the full A-Z + Prestige + Rebirth loop cleanly, integrates with my economy plugin, and doesn't require a PhD to configure.

---

## Features

### Three-Tier Progression

**Ranks (A → Z)**
Start at A, grind your way to Z. Each rank costs more than the last. Simple and satisfying.

**Prestige (P1, P2, P3...)**
Hit rank Z and you can prestige. Resets your rank to A but gives you:
- Multiplier bonuses
- Prestige prefix
- Unlocks for other features

**Rebirth (R1, R2, R3...)**
The ultimate reset. Clears ranks AND prestiges but grants permanent bonuses that persist forever. For the dedicated grinders.

### Visual Feedback
- Progress bars showing distance to next rank
- Animated prestige/rebirth confirmations
- Custom prefixes that update in chat

### Flexible Rewards
Configure what happens on rankup, prestige, and rebirth:
- Currency payouts
- Crate keys
- Permission grants
- Custom commands

---

## Progression Flow

```
[A] → [B] → [C] → ... → [Z]
                          ↓
                     [PRESTIGE]
                          ↓
           [P1-A] → ... → [P1-Z]
                          ↓
                     [PRESTIGE]
                          ↓
           [P2-A] → ... → [P2-Z]
                          ↓
                         ...
                          ↓
                     [REBIRTH]
                          ↓
              Permanent bonuses applied
              Start over at [A]
```

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rankup` | Rank up to next letter | `luminaryranks.rankup` |
| `/prestige` | Prestige at rank Z | `luminaryranks.prestige` |
| `/rebirth` | Rebirth for permanent bonuses | `luminaryranks.rebirth` |
| `/ranks` | View rank list and progress | `luminaryranks.use` |
| `/ranks admin setrank <player> <rank>` | Set player rank | `luminaryranks.admin` |
| `/ranks admin setprestige <player> <level>` | Set prestige | `luminaryranks.admin` |
| `/ranks admin setrebirth <player> <level>` | Set rebirth | `luminaryranks.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Install **LuminaryEconomy** (optional, for currency costs)
3. Drop `LuminaryRanks-1.0.0.jar` into plugins
4. Configure ranks, prestiges, and rebirths in the config
5. Set up costs and rewards to match your server's economy

---

## Configuration Example

```yaml
ranks:
  A:
    display-name: "&7[A]"
    prefix: "&7[A] "
    cost: 1000

  B:
    display-name: "&7[B]"
    prefix: "&7[B] "
    cost: 2500

  # ... through Z

prestiges:
  1:
    display-name: "&5[P1]"
    prefix: "&5[P1] "
    cost: 1000000
    multiplier: 1.5

rebirths:
  1:
    display-name: "&c[R1]"
    prefix: "&c✦ "
    cost: 50000000
    permanent-multiplier: 1.25
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for text formatting
- **PlaceholderAPI** support

---

## Project Structure

```
LuminaryRanks/
├── src/main/java/com/luminary/ranks/
│   ├── LuminaryRanks.java         # Main plugin class
│   ├── command/                   # Rankup, prestige, rebirth commands
│   ├── config/                    # Config management
│   ├── data/                      # Player progression data
│   ├── rank/                      # Rank, Prestige, Rebirth classes
│   └── placeholder/               # PlaceholderAPI expansion
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## Integration

- **LuminaryEconomy** - Rank costs use economy currencies
- **LuminaryCrates** - Give keys as rankup rewards
- **LuminaryGroups** - Rank prefix shows alongside group prefix

---

## What I Learned

The tricky part was making the prestige/rebirth math work right - multipliers stacking correctly, permanent bonuses persisting through resets. Also learned about clean config design so server owners don't need to touch Java to customize everything.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryEconomy** - Currency for rank costs
- **LuminaryGroups** - Permission management
- [View all plugins](#)

---

## License

Portfolio project.
