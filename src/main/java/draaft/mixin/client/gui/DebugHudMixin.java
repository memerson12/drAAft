package draaft.mixin.client.gui;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import draaft.draaft;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

import static draaft.draaft.getDraaftVersion;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin extends DrawableHelper {
    @ModifyReturnValue(method = "getRightText", at = @At("RETURN"))
    private List<String> modifyRightText(List<String> original) {
        original.add("drAAft v" + getDraaftVersion());
        return original;
    }

    @Unique
    private static final String NEW_ARRAY_LIST_TARGET = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;";

    @Redirect(method = "getLeftText", at = @At(value = "INVOKE", ordinal = 0, target = NEW_ARRAY_LIST_TARGET, remap = false))
    private ArrayList<String> modifyReducedLeftText(Object[] _elements, @Local String serverInfo) {
        var client = MinecraftClient.getInstance();

        assert client.world != null;

        return Lists.newArrayList(
            "Minecraft %s (%s/%s)".formatted(SharedConstants.getGameVersion().getName(), client.getGameVersion(), ClientBrandRetriever.getClientModName()),
            client.fpsDebugString,
            serverInfo,
            I18n.translate("draaft.game.reducedDebugInfo.C"),
            I18n.translate("draaft.game.reducedDebugInfo.E"),
            I18n.translate("draaft.game.reducedDebugInfo.PT"),
            client.world.getDebugString(),
            "",
            String.format("Chunk-relative: %d %d %d", 42, -1, 67)
        );
    }

    @Redirect(method = "renderLeftText", at = @At(value = "INVOKE", ordinal = 1, target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean modifyDebugOptsText(List<String> lines, Object e) {
        if (MinecraftClient.getInstance().hasReducedDebugInfo()) {
            return lines.add(I18n.translate("draaft.game.reducedDebugInfo.debugOpts." + draaft.currentF3Taunt % 6));
        } else {
            return lines.add((String) e);
        }
    }
}
