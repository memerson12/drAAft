package draaft.compat.atum;

import me.voidxwalker.autoreset.Atum;
import me.voidxwalker.autoreset.interfaces.ISeedStringHolder;
import net.minecraft.world.gen.GeneratorOptions;

public abstract class AtumCompat {
    public static void stopResetting() {
        Atum.stopRunning();
    }

    public static void restoreSeedString(GeneratorOptions instance, GeneratorOptions from) {
        if (((ISeedStringHolder) from).atum$getSeedString() != null) {
            ((ISeedStringHolder) instance).atum$setSeedString(((ISeedStringHolder) from).atum$getSeedString());
        }
    }
}
