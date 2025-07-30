package draaft;

import com.google.gson.JsonObject;
import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import me.contaria.speedrunapi.config.api.annotations.Config;

import java.net.URI;

public class DraaftConfig implements SpeedrunConfig {
    @Config.Strings.MaxChars(128)
    public String competitionMapUrl = "";

    @Override
    public String modID() {
        return "draaft";
    }

    {
        draaft.draaftConfig = this;
    }
//
//    @Override
//    public void onSave(JsonObject jsonObject) {
//        String uri = jsonObject.get("competitionMapUrl").getAsString();
//        if (!uri.isEmpty()) return;
//        RemoteMapUpdater.updateRemoteMapUrl(URI.create(uri));
//    }
}