package draaft.command;

import com.mojang.brigadier.CommandDispatcher;
import draaft.persistent.WorldState;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ExplodingShellsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("toshellwithyou")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(argument("targets", EntityArgumentType.players()).executes(context -> {
                    for (PlayerEntity player : getPlayers(context, "targets")) {
                        var world = (ServerWorld) player.getEntityWorld();
                        WorldState state = WorldState.getServerState(world);
                        WorldState.RandomState shellRng = state.getOrCreateRng(WorldState.RngType.EXPLODING_SHELLS, world);
                        if (shellRng.getRandom().nextFloat() < 0.5F) {
                            player.giveItemStack(new ItemStack(Items.NAUTILUS_SHELL));
                        } else {
                            TntEntity tntEntity = new TntEntity(world, player.getX(), player.getY() + 1, player.getZ(), player);
                            world.spawnEntity(tntEntity);
                            world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                    return 1;
                }))
        );
    }
}
