package draaft.mixin.world.biome;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.SunflowerPlainsBiome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SunflowerPlainsBiome.class)
public abstract class SunflowerPlainsBiomeMixin extends Biome {
    protected SunflowerPlainsBiomeMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/SunflowerPlainsBiome;addSpawn(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/biome/Biome$SpawnEntry;)V"))
    private void modifyDonkeyMinGroupSize(SunflowerPlainsBiome instance, SpawnGroup spawnGroup, SpawnEntry spawnEntry) {
        if (spawnEntry.type == EntityType.DONKEY) {
            this.addSpawn(spawnGroup, new Biome.SpawnEntry(EntityType.DONKEY, 2, 2, 3));
        } else {
            this.addSpawn(spawnGroup, spawnEntry);
        }
    }
}
