package draaft.mixin.client.gui.screen.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import draaft.client.gui.screen.DraaftWorldOptionsScreen;
import draaft.compat.ModCompat;
import draaft.compat.atum.AtumCompat;
import draaft.mixin.client.gui.screen.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalLong;

@Mixin(MoreOptionsDialog.class)
public abstract class MoreOptionsDialogMixin implements TickableElement, Drawable {
    @Shadow
    private GeneratorOptions generatorOptions;
    @Unique
    private ButtonWidget splitSeedsDialogBtn;

    @Inject(method = "init", at = @At("RETURN"))
    void afterInit(CreateWorldScreen parent, MinecraftClient client, TextRenderer textRenderer, CallbackInfo ci) {
        var screen = (ScreenAccessor) parent;

        this.splitSeedsDialogBtn = screen.draaft$addButton(new ButtonWidget(
            parent.width / 2 + 100 + 6, 60 - 2,
            24, 24,
            Text.of(""),
            btn -> MinecraftClient.getInstance().openScreen(
                new DraaftWorldOptionsScreen(parent, () -> this.generatorOptions)
            )
        ) {
            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                // TODO(me-nx): extract textures
                client.getTextureManager().bindTexture(new Identifier("draaft", this.isHovered() ? "logo.hovered.png" : "logo.png"));

                DrawableHelper.drawTexture(
                    matrices,
                    this.x, this.y,
                    0, 0,
                    24, 24,
                    24, 24
                );
            }
        });
    }

    @Inject(method = "setVisible", at = @At("RETURN"))
    void afterSetVisible(boolean visible, CallbackInfo ci) {
        this.splitSeedsDialogBtn.visible = visible;
    }

    @Redirect(
        method = "getGeneratorOptions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/GeneratorOptions;withHardcore(ZLjava/util/OptionalLong;)Lnet/minecraft/world/gen/GeneratorOptions;"
        )
    )
    GeneratorOptions getGeneratorOptions(GeneratorOptions original, boolean hardcore, OptionalLong optSeed) {
        var dimOptsMap = original.getDimensionMap();

        long seed = optSeed.orElse(original.getSeed());

        var genOpts = new GeneratorOptions(
            seed,
            original.shouldGenerateStructures(),
            original.hasBonusChest() && !hardcore,
            dimOptsMap
        );

        DraaftWorldOptionsScreen.changeSeed(genOpts, DimensionOptions.OVERWORLD, seed);

        if (ModCompat.hasAtum()) {
            AtumCompat.restoreSeedString(genOpts, original);
        }

        return genOpts;
    }

    @WrapOperation(
        method = "method_28093",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/gui/screen/world/MoreOptionsDialog;generatorOptions:Lnet/minecraft/world/gen/GeneratorOptions;"
        )
    )
    void restoreDimensionSeeds(MoreOptionsDialog instance, GeneratorOptions value, Operation<Void> original) {
        DraaftWorldOptionsScreen.restoreSeeds(value, this.generatorOptions);

        original.call(instance, value);
    }
}
