package draaft;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;

public class RemoteMapUpdater {
    private static final Logger logger = draaft.LOGGER;

    private static int currentTickCount = 0;
    private static final int UPDATE_FREQUENCY_SECONDS = 1;
    private static final int TOTAL_TICKS_TO_WAIT = UPDATE_FREQUENCY_SECONDS * 20;

    private static final HttpClient httpclient = HttpClients.createDefault();

    public static void updateRemoteMap(double x, double y, double z) {
        if (currentTickCount >= TOTAL_TICKS_TO_WAIT) {
            currentTickCount = 0;

            HttpPost httppost = new HttpPost(draaft.draaftConfig.competitionMapUrl);

            JsonObject body = new JsonObject();
            body.addProperty("x", Integer.toString((int) x));
            body.addProperty("y", Integer.toString((int) y));
            body.addProperty("z", Integer.toString((int) z));

            StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            httppost.setEntity(requestEntity);
            try {
                HttpResponse res = httpclient.execute(httppost);

                int statusCode = res.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    logger.warn("Map update responded with code: {}", statusCode);
                }

                // Ensure the response body is consumed
                if (res.getEntity() != null) {
                    res.getEntity().getContent().close(); // OR use EntityUtils.consume(res.getEntity());
                }

            } catch (IOException e) {
                logger.warn("Map update failed", e);
            }

        } else {
            currentTickCount++;
        }
    }
}
