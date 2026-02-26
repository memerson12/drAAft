package draaft.client.world;

import dev.menx.worldimporter.RegionId;
import dev.menx.worldimporter.net.RegionServer;

import java.io.InputStream;

class DraaftRegionServer implements RegionServer {
    @Override
    public InputStream downloadRegion(RegionId regionId) {
        // TODO(me-nx)
        throw new AssertionError("not implemented");
    }
}
