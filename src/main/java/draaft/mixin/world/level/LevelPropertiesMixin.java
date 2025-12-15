package draaft.mixin.world.level;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import draaft.client.world.DraaftLevelMetadata;
import draaft.client.world.LevelPropertiesExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements LevelPropertiesExt {
    @Unique
    private @Nullable DraaftLevelMetadata metadata = null;

    @Override
    public @Nullable DraaftLevelMetadata draaft$metadata() {
        return this.metadata;
    }

    @Override
    public void draaft$setMetadata(DraaftLevelMetadata metadata) {
        this.metadata = metadata;
    }

    @Inject(method = "readProperties", at = @At("RETURN"))
    private static void readMetadata(
        Dynamic<Tag> dynamic,
        DataFixer dataFixer,
        int i,
        @Nullable CompoundTag tag,
        LevelInfo levelInfo,
        SaveVersionInfo saveVersionInfo,
        GeneratorOptions generatorOptions,
        Lifecycle lifecycle,
        CallbackInfoReturnable<LevelProperties> cir
    ) {
        var res = cir.getReturnValue();

        ((LevelPropertiesMixin) (Object) res).metadata = DraaftLevelMetadata.read((CompoundTag) dynamic.getValue());
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    void saveMetadata(RegistryTracker registryTracker, CompoundTag tag, CompoundTag player, CallbackInfo ci) {
        if (this.metadata != null) {
            this.metadata.write(tag);
        }
    }
}
