package draaft.client.gui.screen;

import draaft.client.DraaftState;
import draaft.client.gui.DraaftToast;
import draaft.draaft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class WaitingForRoomScreen extends Screen {

    public WaitingForRoomScreen(Screen parent) {
        super(new TranslatableText("draaft.waiting_for_room_screen.title"));
    }

    @Override
    protected void init() {
        super.init();

        // TODO: Temp button until we get join room events from the websocket
        this.addButton(new ButtonWidget(
            this.width / 2 - 50, 60, 150, 20,
            new LiteralText("DEEBUG: Open Draaft Screen"),
            button -> {
                if (DraaftState.getInstance().getRoom() == null) {
                    draaft.LOGGER.info("No room found in DraaftState, cannot open DraaftScreen");
                    DraaftToast.showError(
                        new TranslatableText("Could Not Open Draaft Screen"),
                        new TranslatableText("Could not open Draaft Screen because no room was found")
                    );
                    return;
                }
                this.client.openScreen(new DraaftScreen(this));
            }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.height / 2 - 10, 0xffffff);
        drawCenteredText(matrices, this.textRenderer,
            new LiteralText("Temp Waiting Text"),
            this.width / 2, this.height / 2 + 10, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
