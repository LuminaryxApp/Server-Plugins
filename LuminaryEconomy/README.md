# LuminaryEconomy

Multi-currency economy system built for prison servers. Comes with a sidebar scoreboard that shows player stats.

## Features

### Multiple Currencies
Not just one balance - this handles tokens, beacons, gems, or whatever currencies you want to set up. Each currency can have its own icon and formatting.

### Scoreboard Sidebar
The sidebar shows player info at a glance:
- Current rank (pulls from LuminaryRanks if installed)
- Currency balances
- Group/prefix (pulls from LuminaryGroups if installed)
- Online player count
- Custom lines you define

The scoreboard updates automatically and players can toggle it off if they want with `/scoreboard`.

### Commands
- `/economy` or `/bal` - check your balances
- `/tokens`, `/beacons`, `/gems` - quick access to specific currencies
- `/pay` - send currency to another player
- `/scoreboard` - toggle the sidebar on/off

Admin commands let you give, take, set, and reset player currencies.

## Setup

1. Install LuminaryCore first (required)
2. Drop this jar in plugins
3. Start the server
4. Edit the generated configs to set up your currencies

## Config Files

- `config.yml` - main settings and currency definitions
- `scoreboard.yml` - customize the sidebar layout and placeholders

## Placeholders

Works with PlaceholderAPI. Also integrates with LuminaryRanks and LuminaryGroups to show rank/group info on the scoreboard.

## Dependencies

**Required:** LuminaryCore

**Optional:** PlaceholderAPI, LuminaryRanks, LuminaryGroups
