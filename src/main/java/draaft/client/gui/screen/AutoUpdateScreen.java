package draaft.client.gui.screen;

import draaft.client.AutoUpdater;
import draaft.client.DraaftServices;
import draaft.draaft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class AutoUpdateScreen extends Screen {
    private final DraaftServices draaftServices;
    private final Runnable onSuccess;
    private final Runnable onError;
    private ButtonWidget cancelButton;

    private TranslatableText message = new TranslatableText("draaft.login.update.checking");
    private AutoUpdater.DraaftRelease release = null;

    public AutoUpdateScreen(Runnable onSuccess, Runnable onError, DraaftServices draaftServices) {
        super(new TranslatableText("draaft.login.update.checking"));

        assert draaftServices.githubRepo() != null;

        this.draaftServices = draaftServices;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    protected void init() {
        var client = MinecraftClient.getInstance();

        client.execute(() -> {
            var release = AutoUpdater.checkForUpdates(draaftServices);

            if (release != null) {
                draaft.LOGGER.warn("New drAAft release found: `{}`", release);

                message = new TranslatableText("draaft.login.update.updateFound");
                this.release = release;

                this.cancelButton = this.addButton(new ButtonWidget(
                    this.width / 2 - 110,
                    this.height / 3 + 70,
                    100,
                    20,
                    ScreenTexts.BACK,
                    (btn) -> onSuccess.run()
                ));

                this.addButton(new ButtonWidget(
                    this.width / 2 + 10,
                    this.height / 3 + 70,
                    100,
                    20,
                    new TranslatableText("draaft.login.update.button"),
                    (btn) -> update(btn, release)
                ));
            } else {
                draaft.LOGGER.info("No new releases found");
                onSuccess.run();
            }
        });
    }

    private void update(ButtonWidget button, AutoUpdater.DraaftRelease release) {
        var client = MinecraftClient.getInstance();

        this.message = new TranslatableText("draaft.login.update.updating");

        button.visible = false;
        this.cancelButton.visible = false;

        client.execute(() -> {
            if (AutoUpdater.update(draaftServices, release)) {
                draaft.LOGGER.warn("Mod updated, closing the client");
                client.scheduleStop();
            } else {
                this.onError.run();
            }
        });
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        this.drawCenteredText(matrices, this.textRenderer, this.message, this.width / 2, this.height / 3, 0xffffff);

        if (release != null) {
            this.drawCenteredText(matrices, this.textRenderer, Text.of(release.toString()), this.width / 2, this.height / 3 + 20, 0xffffff);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public static void tryUpdate(Screen parent, DraaftServices draaftServices, Runnable callback) {
        var client = MinecraftClient.getInstance();

        Runnable afterUpdate = () -> {
            client.openScreen(parent);
            callback.run();
        };

        if (AutoUpdater.shouldCheckForUpdates()) {
            client.openScreen(new AutoUpdateScreen(afterUpdate, () -> client.openScreen(parent), draaftServices));
        } else {
            draaft.LOGGER.info("Not checking for updates");
            afterUpdate.run();
        }
    }
}
