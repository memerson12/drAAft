package draaft.compat.atum;

import me.voidxwalker.autoreset.Atum;

public abstract class AtumCompat {
    public static void stopResetting() {
        Atum.stopRunning();
    }
}
