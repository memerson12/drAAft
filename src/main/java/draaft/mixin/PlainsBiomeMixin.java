package draaft.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.PlainsBiome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlainsBiome.class)
public abstract class PlainsBiomeMixin extends Biome {
    protected PlainsBiomeMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/PlainsBiome;addSpawn(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/biome/Biome$SpawnEntry;)V"))
    private void modifyDonkeyMinGroupSize(PlainsBiome instance, SpawnGroup spawnGroup, SpawnEntry spawnEntry) {
        if (spawnEntry.type == EntityType.DONKEY) {
            this.addSpawn(spawnGroup, new Biome.SpawnEntry(EntityType.DONKEY, 2, 2, 3));
        } else {
            this.addSpawn(spawnGroup, spawnEntry);
        }
    }
}
