# drAAft

A Minecraft 1.16.1 mod that makes various gameplay adjustments for a streamlined/ more consistent All Advancement
speedrun. Ideal for competitive events

## Features

### World Generation

- Increased Ancient Debris generation in the Nether
  - Ancient Debris spawns are now 2 attempts with baseline 16, spread 8, one with size 5 and one with size 4.
  - This does not change the optimal y-level for debris mining.
- Increased Lapis Lazuli generation
  - Lapis Lazuli spawns are now 4 attempts with baseline 16, spread 16, size 9 (was 7).
  - This does not change Bolan Clay/Gravel to Lapis Lazuli.
- Increased Lava Pool generation
  - Spawn attempt per chunk is 2x as likely
  - Snapping to surface is 2.5x as likely
- Igloos always generate with basements (was 50%)
- Thunder happens more often and is standardized per seed
    - Thunder will now occur every 10 - 45 minutes (was 10 - 150 minutes)
    - Rain will occur 1 - 7 minutes before thunder starts
    - This guarantees at least two 3+ minute thunders in 1.5 hours

### Mob Changes

- Animals can now spawn on Podzol blocks (in addition to grass)
    - This is intended for pandas in jungles but has side effects in other places such as mega taigas.
    - Bamboo Jungle and Bamboo Jungle Hills have a 30% chance to attempt a passive mob spawn attempt per chunk (was
      10%).
- Drowned:
    - Chance of spawning with Trident 18.75% (was 6.25%)
    - Chance of spawning with Fishing Rod 11.25% (was 3.75%)
    - Chance of spawning with Nautilus Shell 9% (was 3%).
    - Drowned equipment is standardized based on the world seed and block coordinates.
- Phantoms:
    - Spawn more frequently (15-60 second cooldown, down from 60-120 seconds)
    - Require less time without sleep to spawn (10 minutes, down from 60 minutes)
    - Always spawn at least 2 phantoms on Hard difficulty
    - The local difficulty multiplier of 3.0F has been removed, so the spawn attempt should succeed 3x as often
- Ender Dragon:
    - Fly-away will happen less frequently.
    - Dragon instant perch and straight node / diagonal node selection standardized based on the world seed.
- Endermite spawn chance per thrown pearl is now 7% (was 5%).
    - This changes the amount of pearls needed for a 99% chance of getting an Endermite to 64 (was 90).
    - Endermite spawns are standardized based on the world seed.

### Item Changes

- Elytra durability increased to 22,824 (from default)
    - This is to prevent losing to Unbreaking RNG.

## Installation

1. Install Fabric Loader 0.16.13 or later for Minecraft 1.16.1
2. Download the mod JAR file
3. Place the JAR file in your mods folder
4. Launch Minecraft with Fabric loader

# drAAftpack

The drAAft mod is meant to be used in combination with the drAAftpack datapack, which can be generated from https://disrespec.tech/draaft/

## Features

### Default Changes

- Gamerule keepInventory is set to true
- Cat nearest to the player on world load becomes Black variant
- Beaconator advancement is granted
- Iron Golem drops at least 4 iron
- Wither Skeleton Skull drop chance is 7.5% (was 2.5%) + 3% per level of looting (was 1%)
- Bastion chest loot tables buffed to those in 1.16.2
- Stronghold Library loot table buffed
- Desert Pyramid loot table buffed
- Piglin bartering loot table buffed
- End City loot table buffed

### Drafted Changes

In addition to the default changes present for all players, drafted items will be added to their respective player's datapack.
They will be applied to the player through a combination of /advancement grant, /effect give, and /give commands.
Any items left undrafted will be added to every player's datapack.

## Mod Dependencies

- Minecraft 1.16.1
- Fabric Loader ≥0.16.13

## License

This project is licensed under CC0 1.0 Universal. See the LICENSE file for details.
