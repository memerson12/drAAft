package draaft.client.ws.outgoing;

import java.util.UUID;

public abstract class GameUpdate {
    public static class Advance extends GameUpdate {
        public final String variant = "AdvancementUpdate";

        public Advance(String advancement) { this.advancement = advancement; }

        public String advancement;
    }
}
