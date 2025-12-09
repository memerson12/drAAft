package draaft.mixin.server.function;

import draaft.command.CommandFunctionManagerInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandFunctionManager.class)
public abstract class CommandFunctionManagerMixin implements CommandFunctionManagerInterface {
    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract int execute(CommandFunction function, ServerCommandSource source);

    @Unique
    private static final Identifier ENTITY_SPAWN = new Identifier("draaft", "entity_spawn");

    @Unique
    private List<CommandFunction> entitySpawnFunctions = List.of();

    @Inject(method = "method_29773", remap = false, at = @At("TAIL"))
    private void afterLoad(FunctionLoader functionLoader, CallbackInfo ci) {
        entitySpawnFunctions = functionLoader.getTags().getOrCreate(ENTITY_SPAWN).values();
    }

    @Override
    public void draaft$entitySpawn(Entity entity) {
        this.server.getProfiler().push(ENTITY_SPAWN::toString);

        for (var cmd : entitySpawnFunctions) {
            this.execute(cmd, entity.getCommandSource().withLevel(2).withSilent());
        }

        this.server.getProfiler().pop();
    }
}
