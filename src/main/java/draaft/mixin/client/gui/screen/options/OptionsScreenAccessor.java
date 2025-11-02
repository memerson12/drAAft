package draaft.mixin.client.gui.screen.options;

import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OptionsScreen.class)
public interface OptionsScreenAccessor {
    @Accessor("difficulty")
    Difficulty draaft$difficulty();

    @Accessor("difficulty")
    void draaft$setDifficulty(Difficulty difficulty);

    @Accessor("difficultyButton")
    ButtonWidget draaft$difficultyButton();

    @Invoker("getDifficultyButtonText")
    Text draaft$getDifficultyButtonText(Difficulty difficulty);
}
