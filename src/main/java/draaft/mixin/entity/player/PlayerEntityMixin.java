package draaft.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import draaft.api.PlayerEntityAccessor;
import draaft.persistent.WorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        if (this.world instanceof ServerWorld) {
            WorldState state = WorldState.getServerState((ServerWorld) this.world);
            state.setPearlRng(random);
        }
    }

    @Override
    public Random draaft$getPearlRandom() {
        return this.draaft_persistentPearlRng;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void draaft_onInit(World world, BlockPos blockPos, GameProfile gameProfile, CallbackInfo ci) {
        MinecraftServer server = world.getServer();
        if (server == null)
            return;
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();

        if (world instanceof ServerWorld) {
            WorldState state = WorldState.getServerState((ServerWorld) world);
            Random storedRng = state.getPearlRng();
            if (storedRng != null) {
                this.draaft_persistentPearlRng = storedRng;
            } else {
                this.draaft_persistentPearlRng = new Random(seed);
                state.setPearlRng(this.draaft_persistentPearlRng);
            }
        } else {
            this.draaft_persistentPearlRng = new Random(seed);
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void draaft_writeTag(CompoundTag tag, CallbackInfo ci) {
        if (this.draaft_persistentPearlRng != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(this.draaft_persistentPearlRng);
                tag.putByteArray("draaft_pearl_rng", baos.toByteArray());
            } catch (IOException e) {
                // If serialization fails, fall back to just storing the seed
                tag.putLong("draaft_pearl_seed", this.draaft_persistentPearlRng.nextLong());
            }
        }
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void draaft_readTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("draaft_pearl_rng")) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(tag.getByteArray("draaft_pearl_rng"));
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                this.draaft_persistentPearlRng = (Random) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // If deserialization fails, fall back to just using the seed
                if (tag.contains("draaft_pearl_seed")) {
                    this.draaft_persistentPearlRng = new Random(tag.getLong("draaft_pearl_seed"));
                }
            }
        } else if (tag.contains("draaft_pearl_seed")) {
            this.draaft_persistentPearlRng = new Random(tag.getLong("draaft_pearl_seed"));
        }
    }
}
