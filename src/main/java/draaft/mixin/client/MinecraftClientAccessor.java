package draaft.mixin.client;

import com.mojang.datafixers.util.Function4;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Invoker("startIntegratedServer")
    void draaft$startIntegratedServer(String worldName, RegistryTracker.Modifiable registryTracker, Function<LevelStorage.Session, DataPackSettings> datapackSettingsConsumer, Function4<LevelStorage.Session, RegistryTracker.Modifiable, ResourceManager, DataPackSettings, SaveProperties> savePropertiesConsumer, boolean safeMode, MinecraftClient.WorldLoadAction worldLoadAction);
}
