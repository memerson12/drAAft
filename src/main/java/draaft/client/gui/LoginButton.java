package draaft.client.gui;

import draaft.client.DraaftServices;
import draaft.client.ServerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class LoginButton extends ButtonWidget {
    private final BooleanSupplier glintSupplier;
    private final ItemStack item;
    private final ItemStack itemWithGlint;

    public LoginButton(int x, int y, Item guiItem, BooleanSupplier glintSupplier, DraaftServices draaftServices) {
        super(x, y, 20, 20, LiteralText.EMPTY, (btn) -> ServerClient.login(draaftServices));

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
