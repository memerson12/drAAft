package draaft.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CompatMixinConfigPlugin implements IMixinConfigPlugin {
    private static final Pattern MIXIN_NAME = Pattern.compile("^draaft\\.mixin\\.compat\\.([^.]+)\\..+$");

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var matcher = MIXIN_NAME.matcher(mixinClassName);

        var matchesFound = matcher.matches();

        if (!matchesFound) {
            throw new RuntimeException("Invalid draaft-compat mixin %s".formatted(mixinClassName));
        }

        return FabricLoader.getInstance().isModLoaded(matcher.group(1));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
