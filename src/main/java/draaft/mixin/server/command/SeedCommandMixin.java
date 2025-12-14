package draaft.mixin.server.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @Unique
    private static final String SEND_FEEDBACK_TARGET = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Lnet/minecraft/text/Text;Z)V";

    @ModifyArg(method = "method_13617", remap = false, at = @At(value = "INVOKE", target = SEND_FEEDBACK_TARGET))
    private static Text modifySeedText(Text original, @Local(argsOnly = true) CommandContext<ServerCommandSource> commandContext) {
        MutableText text = new TranslatableText("draaft.game.command.seeds.header");

        var worldKeys = commandContext.getSource().getWorldKeys();

        var server = commandContext.getSource().getMinecraftServer();

        worldKeys.stream().map(server::getWorld).forEach(world -> {
            assert world != null;

            text.append("\n");

            text.append(new TranslatableText(
                "draaft.game.command.seeds.seed",
                world.getRegistryKey().getValue().toString(),
                seedText(world.getSeed())
            ));
        });

        return text;
    }

    @Unique
    private static Text seedText(long seed) {
        var seedStr = String.valueOf(seed);

        return Texts.bracketed(
            new LiteralText(seedStr).styled(style ->
                style.withColor(Formatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, seedStr))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                    .withInsertion(seedStr)
            )
        );
    }
}
