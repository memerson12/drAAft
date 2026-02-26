package draaft.mixin.client.gui;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import draaft.draaft;
import draaft.world.WorldClientInfo;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin extends DrawableHelper {
    @Shadow
    @Nullable
    protected abstract WorldChunk getChunk();

    @Shadow
    private @Nullable ChunkPos pos;

    @ModifyReturnValue(method = "getRightText", at = @At("RETURN"))
    private List<String> modifyRightText(List<String> original) {
        original.add("");
        original.add("drAAft v" + draaft.DRAAFT_VERSION);
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
        var client = MinecraftClient.getInstance();

        assert client.world != null;

        WorldClientInfo worldClientInfo = WorldClientInfo.get(client.world);

        if (client.hasReducedDebugInfo() && client.getCameraEntity() != null) {
            var blockPos = client.getCameraEntity().getBlockPos();

            this.pos = new ChunkPos(blockPos);
            var chunk = this.getChunk();

            var o = "";

            if (client.world.isChunkLoaded(this.pos.x, this.pos.z) && chunk != null && !chunk.isEmpty()) {
                var type = Heightmap.Type.OCEAN_FLOOR;

                o = " O: " + chunk.sampleHeightmap(type, blockPos.getX(), blockPos.getZ());
            }

            lines.add(String.format(Locale.ROOT, "Y: %.5f%s", client.getCameraEntity().getY(), o));

            if (
                client.world.isChunkLoaded(this.pos.x, this.pos.z)
                    && !client.world.getChunk(this.pos.x, this.pos.z).isEmpty()
                    && blockPos.getY() >= 0
                    && blockPos.getY() < 256
            ) {
                lines.add(I18n.translate(
                    "draaft.game.reducedDebugInfo.biome",
                    Registry.BIOME.getId(client.world.getBiome(blockPos))
                ));
            } else {
                lines.add(I18n.translate("draaft.game.reducedDebugInfo.waitingForChunk"));
            }

            lines.add("");
        }

        if (worldClientInfo.showCoords() && client.world.getRegistryKey().equals(World.OVERWORLD)) {
            lines.add("Mushroom Island: " + WorldClientInfo.get(client.world).annotations().mushroomIsland);
            lines.add("Jungle: " + WorldClientInfo.get(client.world).annotations().jungle);
            lines.add("Mega Taiga: " + WorldClientInfo.get(client.world).annotations().megaTaiga);
            lines.add("Snowy: " + WorldClientInfo.get(client.world).annotations().snowy);
            lines.add("Badlands: " + WorldClientInfo.get(client.world).annotations().badlands);
            lines.add("");
        } else if (worldClientInfo.showCoords() && client.world.getRegistryKey().equals(World.NETHER)) {
            lines.add("Bastion: " + WorldClientInfo.get(client.world).annotations().bastion);
            lines.add("Fortress: " + WorldClientInfo.get(client.world).annotations().fortress);

            List<String> strongholds = WorldClientInfo.get(client.world).annotations().strongholds;
            if (strongholds != null) {
                String strongholdsString = "";
                for (int i = 0; i < Objects.requireNonNull(strongholds).size(); i++) {
                    strongholdsString = strongholdsString.concat(strongholds.get(i));
                    if (i < strongholds.size() - 1) {
                        strongholdsString = strongholdsString.concat(" / ");
                    }
                }
                lines.add("Strongholds: " + strongholdsString);
            }
            lines.add("");
        }


        if (client.hasReducedDebugInfo()) {
            return lines.add(I18n.translate("draaft.game.reducedDebugInfo.debugOpts." + draaft.currentF3Taunt % 12));
        } else {
            return lines.add((String) e);
        }
    }
}
