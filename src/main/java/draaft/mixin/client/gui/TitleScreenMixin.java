package draaft.mixin.client.gui;

import draaft.client.ServerClient;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final String DEFAULT_BUTTON_HOVER = "drAAft Login";

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void addDraaftLoginButton(CallbackInfo info) {
        int login_button_width = 20;
        int login_button_heigh = 20;
        // Can probably get these from the Bucket Item somehow but didn't bother
        int bucket_texture_width = 16;
        int bucket_texture_height = 16;
        int single_player_button_y = this.height / 4 + 48;
        int single_player_button_half_width = 100;
        int horizontal_button_spacing = 4;
        this.addButton(new ButtonWidget(
            this.width / 2 + single_player_button_half_width + horizontal_button_spacing,
            single_player_button_y,
            login_button_width,
            login_button_heigh,
            LiteralText.EMPTY,
            button -> { ServerClient.getInstance().draaftLogin(); })
        {
            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);

                Item bucket_item = Items.BUCKET;
                ItemStack stack = new ItemStack(bucket_item);
                // I mean... Of course a bucket has AQUA AFFINITY 10
                stack.addEnchantment(Enchantments.AQUA_AFFINITY, 10);

                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                ItemRenderer itemRenderer = minecraftClient.getItemRenderer();
                itemRenderer.renderInGui(
                    stack,
                    this.x + (login_button_width - bucket_texture_width) / 2,
                    this.y + (login_button_heigh - bucket_texture_height) / 2
                );

                var inst = ServerClient.getInstance();
                if (this.isHovered() || inst.connectingStatus != null) {
                    String s = inst.connectingStatus;
                    if (s == null) {
                        s = DEFAULT_BUTTON_HOVER;
                    }
                    else {
                        if (inst.connectingStatusTimestamp == 0 && !this.isHovered()) {
                            // Timed out already, just return
                            return;
                        }
                        long t = Instant.now().getEpochSecond();
                        if (t - inst.connectingStatusTimestamp > 10) {
                            inst.connectingStatusTimestamp = 0;
                        }
                    }
                    // todo - I can probably make this more efficient? lol
                    this.drawCenteredText(matrices, TitleScreenMixin.this.textRenderer, TextUtil.literal(s), this.x + this.width / 2, this.y - 15, 16777215);
                }
            }
        });
    }
}