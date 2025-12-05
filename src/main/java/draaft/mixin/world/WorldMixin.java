package draaft.mixin.world;

import draaft.world.WorldInterface;
import draaft.world.WorldClientInfo;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public abstract class WorldMixin implements WorldInterface {
    @Unique
    private WorldClientInfo clientInfo = null;


    @Override
    public @Nullable WorldClientInfo draaft$clientInfo() {
        return clientInfo;
    }

    @Override
    public void draaft$setClientInfo(WorldClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
}
