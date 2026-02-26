# Changelog

## drAAft 2.0 Official Release

### General Changes

- Explicitly require Java 16+ (@me-nx)
- Require speedrunapi as a dependency (@memerson12)

### Drafting Changes: Networked Drafting

- Add networked drafting support (@DesktopFolder, @me-nx)
- Add login button in main menu (@DesktopFolder)
- Login button is enchanted when logged in in-game (@Matsenuc)
- Add new in-game menus for when the player is in a draft (@memerson12)
- Add login flow toasts, translation support (@me-nx)
- Show count of other players' advancements in tab menu (@memerson12, @DesktopFolder)
- Automatically download datapack and create world post-draft (@me-nx)
- Sync player position with server for future live mapping (@memerson12)

### Gameplay Changes

- Nerf elytra durability to 22,359 (as Coosh PB'd) (@DesktopFolder)
- Non-intrusively disable peaceful mode (@me-nx)
- Disable mob spawning in pyramids (@me-nx)
- Standardize temple loot (@MaximVancompernolle)
- Add pity timers to force important drops after enough attempts (@MaximVancompernolle):
  - Enchanted golden apple every 5th temple
  - Wither skeleton skulls every 40/28/22/18 wither skeletons killed for no looting/looting 1/looting 2/looting 3
  - Endermite every 24 pearls
  - Trident every 10 drowneds holding tridents
- Standardize debris. Yes, really. (@MaximVancompernolle)
- Enchanted bucket is persistent (@MaximVancompernolle)
- Standardize dead bush and lapis ore drops (@MaximVancompernolle)
- Standardize eye breaks (@MaximVancompernolle)
- Implement a variety of gambits:
  - NoF3 (disables all ways of acquiring the player's coordinates via F3-based keybinds, except Y coordinate) (@MaximVancompernolle)
  - Enchants (makes all items enchanted) (@MaximVancompernolle)
  - Exploding shells (allows randomly spawning a shell or a TNT on the player, summoned by them) (@MaximVancompernolle)
  - Dangerous pearls (removes cooldown on pearls, increases fall damage) (@DesktopFolder)
  - Poor View (requires completing Great View twice - levitation is purged after the first attempt) (@DesktopFolder)
  - Debris Debris (randomly gives the player junk items) (@MaximVancompernolle)
  - Splodeyghasts (ghast explosion power is increased to 7)
- Support per-dimension seeds, including in /seed (@me-nx)
- Make player spawn more consistently close to 0,0 (and +,+) (@MaximVancompernolle)
- Replace gold ore with diamond ore (@MaximVancompernolle)
- Remove village spawns (@MaximVancompernolle)
- Conduit power is not lost when exiting the end through the end portal (@MaximVancompernolle)
- Use legacyIGT for in game timer (@me-nx)
