# LuminaryEconomy

**Multi-currency economy with a live scoreboard sidebar.** Handle tokens, gems, beacons, or any custom currencies you need. The sidebar keeps players informed with real-time stats.

Built for prison servers running Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Scoreboard Sidebar](images/scoreboard.png) -->
<!-- ![Balance Menu](images/balance-gui.png) -->
<!-- ![Currency Setup](images/currencies.png) -->

*Screenshots coming soon - showing the sidebar in action, currency menus, and admin panel.*

---

## Why I Built This

Prison servers need more than just one currency. You've got tokens for enchants, gems for cosmetics, beacons for special items - trying to track all that with a basic economy plugin doesn't cut it. Plus I wanted a sidebar that actually shows useful info without needing a separate scoreboard plugin.

---

## Features

### Multiple Currencies
Define as many currencies as you need. Each one gets its own:
- Display name and formatting
- Icon for menus
- Shorthand commands (`/tokens`, `/gems`, etc.)
- PlaceholderAPI support

### Live Scoreboard
The sidebar updates in real-time showing:
- Player's current rank (from LuminaryRanks)
- All currency balances
- Group prefix (from LuminaryGroups)
- Online player count
- Custom lines you define

Players can toggle it off if they want a cleaner screen.

### Player-to-Player Payments
Send any currency to other players. Configurable tax rates if you want to create money sinks.

---

## Screenshots Showcase

### Sidebar Display
<!-- ![Sidebar showing rank, currencies, and player info](images/sidebar-example.png) -->
*The sidebar pulls data from multiple plugins to give players a complete overview.*

### Admin Panel
<!-- ![Admin currency management interface](images/admin-panel.png) -->
*Easy management of player balances and currency settings.*

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/balance` or `/bal` | Check all balances | `luminaryeconomy.use` |
| `/tokens [player]` | Check token balance | `luminaryeconomy.use` |
| `/beacons [player]` | Check beacon balance | `luminaryeconomy.use` |
| `/gems [player]` | Check gem balance | `luminaryeconomy.use` |
| `/pay <player> <amount> [currency]` | Send currency | `luminaryeconomy.pay` |
| `/scoreboard` | Toggle sidebar | `luminaryeconomy.scoreboard` |
| `/eco give <player> <amount> <currency>` | Give currency | `luminaryeconomy.admin.give` |
| `/eco take <player> <amount> <currency>` | Remove currency | `luminaryeconomy.admin.take` |
| `/eco set <player> <amount> <currency>` | Set balance | `luminaryeconomy.admin.set` |

---

## Installation

1. Install **LuminaryCore** first (required dependency)
2. Drop `LuminaryEconomy-1.0.0.jar` into your `plugins` folder
3. Start the server
4. Edit `plugins/LuminaryEconomy/config.yml` to set up currencies
5. Customize `scoreboard.yml` for sidebar layout

---

## Configuration Example

```yaml
currencies:
  tokens:
    display-name: "&eTokens"
    icon: GOLD_INGOT
    starting-balance: 0
    format: "&e{amount} Tokens"

  beacons:
    display-name: "&bBeacons"
    icon: BEACON
    starting-balance: 0
    format: "&b{amount} Beacons"
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Adventure API** for text/scoreboard
- **PlaceholderAPI** integration (optional)

---

## Project Structure

```
LuminaryEconomy/
├── src/main/java/com/luminary/economy/
│   ├── LuminaryEconomy.java       # Main plugin class
│   ├── command/                   # Economy commands
│   ├── config/                    # Config management
│   ├── currency/                  # Currency definitions
│   ├── data/                      # Player balance storage
│   ├── placeholder/               # PlaceholderAPI expansion
│   └── scoreboard/                # Sidebar management
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    └── scoreboard.yml
```

---

## Integration

This plugin talks to other Luminary plugins:
- **LuminaryRanks** - Shows current rank on sidebar
- **LuminaryGroups** - Shows group prefix on sidebar
- **LuminaryShop** - Uses these currencies for transactions

---

## What I Learned

The scoreboard system was tricky - Minecraft has limits on entry length and you need to handle color codes carefully. I also learned about lazy initialization for plugin hooks since load order with soft dependencies isn't guaranteed.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryRanks** - Rank progression system
- **LuminaryGroups** - Permission groups
- [View all plugins](#)

---

## License

Portfolio project. Code is available for review.
