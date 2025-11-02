package draaft.client.gui;

import draaft.client.DraaftServices;
import draaft.client.DraaftState;
import draaft.client.ServerClient;
import draaft.client.gui.screen.DraaftScreen;
import draaft.client.gui.screen.WaitingForRoomScreen;
import draaft.client.models.Room;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

import java.util.function.BooleanSupplier;

public class LoginButton extends ButtonWidget {
    private final BooleanSupplier glintSupplier;
    private final ItemStack item;
    private final ItemStack itemWithGlint;

    public LoginButton(
        int x, int y,
        Item guiItem,
        BooleanSupplier glintSupplier,
        TooltipSupplier tooltipSupplier,
        DraaftServices draaftServices,
        Screen parent
    ) {
        super(x, y, 20, 20, LiteralText.EMPTY, (btn) -> {
            ServerClient.login(draaftServices);

            // todo it feels kind of weird for this logic to be here in the button class
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            DraaftState draaftState = DraaftState.getInstance();
            draaftState.setCurrentState(DraaftState.STATE.WAITING_FOR_ROOM);

            Room room = draaftState.getRoom();
            if (room == null) {
                minecraftClient.openScreen(new WaitingForRoomScreen(parent));
            } else {
                draaftState.setCurrentState(DraaftState.STATE.IN_ROOM);
                minecraftClient.openScreen(new DraaftScreen(parent));
            }
        }, tooltipSupplier);

        this.glintSupplier = glintSupplier;

        this.item = new ItemStack(guiItem);

        this.itemWithGlint = new ItemStack(guiItem);
        // I mean... Of course a bucket has AQUA AFFINITY 10 ~ Matsenuc
        this.itemWithGlint.addEnchantment(Enchantments.AQUA_AFFINITY, 10);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        var client = MinecraftClient.getInstance();

        int bucketTextureSize = 16;

        client.getItemRenderer().renderInGui(
            glintSupplier.getAsBoolean() ? itemWithGlint : item,
            this.x + (this.width - bucketTextureSize) / 2,
            this.y + (this.height - bucketTextureSize) / 2
        );
    }
}
