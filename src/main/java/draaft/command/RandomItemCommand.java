package draaft.command;

import com.mojang.brigadier.CommandDispatcher;
import draaft.persistent.WorldState;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RandomItemCommand {
    private static List<ItemStack> itemStacks = null;

    private static void initializeItemStacks() {
        itemStacks = List.of(
            new ItemStack(Items.SNOWBALL, 5),
            new ItemStack(Items.CARROT_ON_A_STICK),
            new ItemStack(Items.TIPPED_ARROW),
            new ItemStack(Items.ITEM_FRAME),
            new ItemStack(Items.BEDROCK),
            new ItemStack(Items.BOWL),
            new ItemStack(Items.FEATHER),
            new ItemStack(Items.INK_SAC),
            new ItemStack(Items.EGG, 2),
            new ItemStack(Items.SUGAR),
            new ItemStack(Items.LILY_OF_THE_VALLEY),
            new ItemStack(Items.MAP),
            new ItemStack(Items.WOODEN_HOE, 17),
            new ItemStack(Items.MAGENTA_CONCRETE_POWDER),
            new ItemStack(Items.MULE_SPAWN_EGG),
            new ItemStack(Items.POLAR_BEAR_SPAWN_EGG),
            new ItemStack(Items.BAT_SPAWN_EGG),
            new ItemStack(Items.SQUID_SPAWN_EGG),
            new ItemStack(Items.EXPERIENCE_BOTTLE),
            new ItemStack(Items.SKELETON_SKULL),
            new ItemStack(Items.FLOWER_POT, 6),
            new ItemStack(Items.FIREWORK_STAR),
            new ItemStack(Items.FURNACE_MINECART),
            new ItemStack(Items.LINGERING_POTION),
            new ItemStack(Items.COMMAND_BLOCK),
            new ItemStack(Items.CHAIN_COMMAND_BLOCK),
            new ItemStack(Items.REPEATING_COMMAND_BLOCK),
            new ItemStack(Items.COMMAND_BLOCK, 3),
            new ItemStack(Items.MUSIC_DISC_MELLOHI),
            new ItemStack(Items.FLETCHING_TABLE),
            new ItemStack(Items.GRINDSTONE),
            new ItemStack(Items.COMPARATOR, 2),
            new ItemStack(Items.REPEATER),
            new ItemStack(Items.DAYLIGHT_DETECTOR),
            new ItemStack(Items.BARRIER),
            new ItemStack(Items.BARRIER, 10),
            new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
        );
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("junkitem")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(argument("targets", EntityArgumentType.players()).executes(context -> {
                    if (itemStacks == null) {
                        initializeItemStacks();
                    }
                    for (PlayerEntity player : getPlayers(context, "targets"))
                    {
                        var world = (ServerWorld) player.getEntityWorld();
                        WorldState state = WorldState.getServerState(world);
                        WorldState.RandomState junkRng = state.getOrCreateRng(WorldState.RngType.JUNK, world);
                        int randomNum = junkRng.getRandom().nextInt(itemStacks.size());
                        player.giveItemStack(itemStacks.get(randomNum).copy());
                    }
                    return 1;
                }
                )));
    }
}
