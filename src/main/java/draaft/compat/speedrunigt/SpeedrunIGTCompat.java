package draaft.compat.speedrunigt;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class SpeedrunIGTCompat {
    private static final int ADVANCEMENT_COUNT = 80;

    public static void initializeTimer(String worldName, GameMode gameMode, boolean cheatsAllowed, Difficulty difficulty) {
        InGameTimer.start(worldName, RunType.RANDOM_SEED);

        var timer = InGameTimer.getInstance();
        timer.setCategory(DraaftCategory.INSTANCE, false);
        timer.setDefaultGameMode(gameMode.getId());
        timer.setCheatAvailable(cheatsAllowed);
        timer.checkDifficulty(difficulty);

        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
    }

    public static void onAdvancements(AdvancementManager advancementManager, Map<Advancement, AdvancementProgress> advancementProgresses) {
        var timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory().equals(DraaftCategory.INSTANCE)) {
            var completed = timer.getAdvancementsTracker().getAdvancements().entrySet()
                .stream()
                .filter(advancement -> advancement.getValue().isAdvancement() && advancement.getValue().isComplete())
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

            for (var advancement : advancementManager.getAdvancements()) {
                if (advancementProgresses.containsKey(advancement) && advancement.getDisplay() != null) {
                    var progress = advancementProgresses.get(advancement);
                    progress.init(advancement.getCriteria(), advancement.getRequirements());

                    var id = advancement.getId().toString();

                    if (progress.isDone() && completed.contains(id)) {
                        timer.tryInsertNewAdvancement(id, null, true);
                    }
                }
            }

            if (completed.size() >= ADVANCEMENT_COUNT) {
                InGameTimer.complete();
            }
        }
    }
}
