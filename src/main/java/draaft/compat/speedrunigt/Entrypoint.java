package draaft.compat.speedrunigt;

import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.timer.category.RunCategory;

import java.util.Collection;
import java.util.List;

public class Entrypoint implements SpeedRunIGTApi {
    @Override
    public Collection<RunCategory> registerCategories() {
        return List.of(DraaftCategory.INSTANCE);
    }
}
