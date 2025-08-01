# DropUtils
Simple lightweight Minecraft Paper plugin made in an hour that gives or places a customizable dragon egg the first time each player kills the Ender Dragon. Prevents duplicate drops by granting a marker permission after the first kill.

## Features
- On first dragon kill per player:
  - Give a custom dragon egg item or spawn a dragon egg block at a configurable location.
  - Customize the egg's display name, lore, and optional player-name line.
  - Send a configurable message to the player.
  - Execute a console command (e.g. via LuckPerms) to persistently grant a marker permission.
- Subsequent dragon kills by the same player will not trigger the drop.
- If inventory is full when giving the item, the egg is dropped at the player’s head location.

## Requirements
- Java 21
- PaperMC 1.21.x (API version 1.21)
- (Optional) LuckPerms or any permissions plugin to manage the marker permission

## Configuration (`config.yml`)
```yaml
# Message sent on first kill
first-kill-message: '&d&lᴇɢɢ &8| &fYou killed the dragon for the first time, an egg has been dropped!'

# Give the egg item instead of spawning a block
give-egg: true

# Whether to add a custom player line to lore
add-player-name: true

# Console command template to grant marker permission
# Use {player} to substitute the killer's name
permission-command: 'lp user {player} permission set droputils.dragonegg true'

egg-item:
  # Custom display name (leave blank to use default)
  name: '&d&lDragon Egg'

  # Custom lore lines (markdown-style codes supported)
  lore:
    - '&7This egg marks your triumph'
    - '&7Use it wisely'

  # Player line template; leave blank to disable
  player-lore: '&r&fPlayer: &#37bbe2{playername}'

# If give-egg is false, spawn a dragon egg block instead
drop-location:
  world: 'world'
  x: 0
  y: 80
  z: 0
```

## Permissions
- `droputils.dragonegg` (default: false)
  - Marker permission granted after first kill. Players with this permission will NOT receive another egg or spawn.
  - Negating this permission from a player allows them to receive the egg again.

## License
This project is licensed under the CC-BY-SA-4.0 license. You are free to use, modify, and distribute this code as long as you provide attribution and share any derivative works under the same license.

*Might add more features (like customizing drops for other mobs) in the future, feel free to contribute!*
