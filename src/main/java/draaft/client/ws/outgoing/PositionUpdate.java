package draaft.client.ws.outgoing;

public abstract class PositionUpdate {
    public static class PositionUpdateEvent extends PositionUpdate {
        String variant = "PositionUpload";

        public double x, y, z;
        public String dimension;

        public PositionUpdateEvent(double x, double y, double z, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }
    }
}
