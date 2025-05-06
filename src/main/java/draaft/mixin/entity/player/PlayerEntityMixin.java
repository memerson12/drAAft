package draaft.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import draaft.api.PlayerEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccessor {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    public Random draaft_persistentPearlRng;

    @Override
    public void draaft$setPearlRandom(Random random) {
        this.draaft_persistentPearlRng = random;
    }

    @Override
    public Random draaft$getPearlRandom() {
        return this.draaft_persistentPearlRng;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void draaft_onInit(World world, BlockPos blockPos, GameProfile gameProfile, CallbackInfo ci) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        this.draaft_persistentPearlRng = new Random(seed);
    }
}
