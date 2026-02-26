# drAAft

A Minecraft 1.16.1 mod that makes various gameplay adjustments for a streamlined/more consistent All Advancement
speedrun. Ideal for competitive events. [Official website](https://draaft.net).

## Features

### Gameplay

drAAft gameplay is based around a mod and datapack combo. The mod can be downloaded from [Releases](https://github.com/memerson12/drAAft/releases/).
The datapack is automatically downloaded and added to newly generated worlds after drafts are completed on [the website](https://draaft.net).

To learn about drafting, as well as general gameplay (draft picks, gambits, etc), see the "Learn to Play" section.

### World Generation

- Increased & standardized Ancient Debris generation:
  - Debris is generated exclusively between Y=5 and Y=25 (most likely near Y=15)
  - No debris exists until blocks are broken (by the player mining or any explosions)
    - When this occurs, there is a chance of a debris vein being generated:
      - Behind the block the player mines (0.5%, guaranteed after 200 blocks)
      - As a replacement for solid blocks destroyed by explosions (10% chance per explosion, guaranteed after 10 explosions)
- Increased Lapis Lazuli generation:
    - Lapis Lazuli spawns are now 4 attempts with baseline 16, spread 16
    - This does not change Bolan Clay/Gravel to Lapis Lazuli
- Increased diamond ore generation by replacing all gold ore with diamond ore
- Increased Lava Pool generation:
    - Spawn attempt per chunk is 2x as likely
    - Snapping to surface is 2.5x as likely
- Increased End Gateway generation:
    - Spawn attempt per chunk is 7x as likely
- Igloos always generate with basements (compared to 50% in vanilla)
- Desert temple chest loot is standardized based on number of chests looted, not the location of the chest
- Every 5th desert temple is guaranteed to contain an enchanted golden apple
- Beehive generation on trees is 2x as likely
- Thunder happens more often and is standardized
    - Thunder will now occur every 10 - 45 minutes (was 10 - 150 minutes)
    - Rain will occur 1 - 7 minutes before thunder starts
    - This guarantees at least two 3+ minute thunders in 1.5 hours
- Spawn algorithm changed:
  - Player cannot spawn within 5 chunks of any village center
  - Desert is now a spawnable biome, and sand is a spawnable block (this makes spawn more likely to be at or near 0,0)
  - Algorithm is biased towards +,+ (searches between -128 and +256, instead of -256 and +256)

### Mob Changes

- Mobs cannot spawn near desert pyramid chests
- Animals can now spawn on Podzol blocks (in addition to grass)
    - This is intended for pandas in jungles but has side effects in other places such as mega taigas
    - Bamboo Jungle and Bamboo Jungle Hills have a 30% chance to attempt a passive mob spawn attempt per chunk (was
      10%)
- Blaze:
    - Rod drops are standardized
- Cat:
    - Cat variant is standardized on block coordinates
    - Cat taming is standardized
- Donkey:
    - Spawn chance in Plains and Sunflower Plains is 2x as likely
    - Minimum pack size is 2 (was 1)
- Drowned:
    - Chance of spawning with Trident 18.75% (was 6.25%)
        - Dropped Tridents will have at minimum 2 durability
        - Trident drop chance is now 12.5% (was 8.5%) + 2% per level of looting (was 1%)
        - Trident drops are standardized
        - Trident drops are guaranteed every 10 trident drowneds killed
    - Chance of spawning with Fishing Rod 11.25% (was 3.75%)
    - Chance of spawning with Nautilus Shell 9% (was 3%)
    - Drowned equipment is standardized on block coordinates
- Ender Dragon:
    - Fly-away will happen less frequently
    - Dragon instant perch and straight node / diagonal node selection standardized
- Endermite:
    - Spawn chance per thrown pearl is now 7% (was 5%)
    - Endermite spawns are standardized
    - Endermite is guaranteed to spawn every 24 pearls thrown
- Phantom:
    - Require less time without sleep to spawn (10 minutes, down from 60 minutes)
    - Always spawn at least 2 phantoms on Hard difficulty
    - The local difficulty multiplier of 3.0F has been removed, so the spawn attempt should succeed 3x as often
    - Phantom spawns are standardized
- Piglin:
    - Piglin barters are standardized
- Rabbit
    - Rabbit drops are standardized
- Shulker:
    - Shell drops are standardized
- Wither Skeleton:
    - Wither Skeleton Skull drops are standardized
    - Skulls are guaranteed to drop after every 40/28/22/18 wither skeletons killed for looting level 0/1/2/3

### Item Changes

- Elytra durability increased to 22,359 (from default)
    - This is to prevent losing to Unbreaking RNG.
- Trident enchants have been tweaked
    - Channeling is now Rare (was Very Rare)
    - Loyalty is now Rare (was Uncommon)
    - First enchant is still Channeling
- Eye breaks are standardized
- Lapis ore and dead bush drops are standardized

### General Mod Changes

- Peaceful mode is disabled (will automatically set to previous non-peaceful difficulty on selection)
- Per-dimension seeds are supported
- Conduit power is not lost when exiting the end through the end portal
- If SpeedrunIGT is enabled, legacyIGT will be automatically used for the in game timer

## Installation

1. Install Fabric Loader 0.16.14 or later for Minecraft 1.16.1
2. Download the mod JAR file
3. Place the JAR file in your mods folder
4. Launch Minecraft with Fabric loader

# drAAftpack

The drAAft mod is meant to be used in combination with the drAAftpack datapack, which is automatically
downloaded after logging in and completing a draaft at https://draaft.net

## Features

### Default Datapack Changes

- Gamerule keepInventory is set to true
- Cat nearest to the player on world load becomes Black variant
- Cat spawns in village centers are doubled
- Beaconator advancement is granted
- Iron Golem drops 4 iron
- Wither Skeleton Skull drop chance is 7.5% (was 2.5%) + 3% per level of looting (was 1%)
- Bastion chest loot tables buffed to those in 1.16.2
- Desert Pyramid loot table buffed
- Piglin bartering loot table buffed
- Stronghold Library loot table buffed
- End City loot table buffed

### Drafted Changes

In addition to the default changes present for all players, drafted items will be added to their respective player's
datapack.
They will be applied to the player through a combination of /advancement grant, /effect give, and /give commands.
Any items left undrafted will be added to every player's datapack.

## Mod Dependencies

- Minecraft 1.16.1
- SpeedRunIGT 15.1
- Fabric Loader ≥0.16.14
- SpeedrunAPI >=2.0+1.16.1
- Java >=16

## License

This project is licensed under CC0 1.0 Universal. See the LICENSE file for details.
