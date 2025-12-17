package draaft.compat.speedrunigt;

import com.redlimerl.speedrunigt.timer.category.RunCategory;
import draaft.client.DraaftServices;

class DraaftCategory extends RunCategory {
    public static final DraaftCategory INSTANCE = new DraaftCategory();

    private DraaftCategory() {
        super("DRAAFT", "", "draaft.compat.speedrunigt.category");
    }

    @Override
    public String getLeaderboardUrl() {
        return DraaftServices.DEFAULT.webBase().toString();
    }
}
