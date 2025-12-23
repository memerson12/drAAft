package draaft.client.gui.screen;

import draaft.mixin.world.gen.chunk.ChunkGeneratorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;

import java.util.function.Supplier;

public class DraaftWorldOptionsScreen extends Screen {
    private final Screen parent;
    private final Supplier<GeneratorOptions> generatorOptionsSupplier;

    public DraaftWorldOptionsScreen(Screen parent, Supplier<GeneratorOptions> generatorOptionsSupplier) {
        super(new TranslatableText("draaft.worlds.options.title"));

        this.parent = parent;
        this.generatorOptionsSupplier = generatorOptionsSupplier;
    }

    @Override
    protected void init() {
        var initialGenOpts = this.generatorOptionsSupplier.get();

        var nether = this.addButton(new TextFieldWidget(
            textRenderer,
            this.width / 2 - 100, 60,
            200, 20,
            new TranslatableText("draaft.worlds.options.nether")
        ));
        nether.setText(getSeedString(initialGenOpts, DimensionOptions.NETHER));
        nether.setChangedListener(seedStr -> {
            var genOpts = generatorOptionsSupplier.get();

            changeSeed(genOpts, DimensionOptions.NETHER, !seedStr.isEmpty() ? parseSeed(seedStr) : genOpts.getSeed());
        });

        var end = this.addButton(new TextFieldWidget(
            textRenderer,
            this.width / 2 - 100, 100,
            200, 20,
            new TranslatableText("draaft.worlds.options.end")
        ));
        end.setText(getSeedString(initialGenOpts, DimensionOptions.END));
        end.setChangedListener(seedStr -> {
            var genOpts = generatorOptionsSupplier.get();

            changeSeed(genOpts, DimensionOptions.END, !seedStr.isEmpty() ? parseSeed(seedStr) : genOpts.getSeed());
        });

        this.addButton(new ButtonWidget(
            this.width / 2 - 75, 160,
            150, 20,
            ScreenTexts.DONE,
            btn -> this.onClose()
        ));
    }

    public static void changeSeed(GeneratorOptions genOpts, RegistryKey<DimensionOptions> world, long seed) {
        var dimOpts = genOpts.getDimensionMap().get(world);
        var dimOptsId = genOpts.getDimensionMap().getRawId(dimOpts);

        assert dimOpts != null;

        dimOpts = new DimensionOptions(dimOpts.getDimensionTypeSupplier(), dimOpts.getChunkGenerator().withSeed(seed));

        genOpts.getDimensionMap().set(dimOptsId, world, dimOpts);
    }

    public static void restoreSeeds(GeneratorOptions genOpts, GeneratorOptions from) {
        changeSeed(genOpts, DimensionOptions.NETHER, getSeed(from, DimensionOptions.NETHER));
        changeSeed(genOpts, DimensionOptions.END, getSeed(from, DimensionOptions.END));
    }

    private static long getSeed(GeneratorOptions genOpts, RegistryKey<DimensionOptions> dimension) {
        var dimOpts = genOpts.getDimensionMap().get(dimension);

        assert dimOpts != null;

        return ((ChunkGeneratorAccessor) dimOpts.getChunkGenerator()).draaft$seed();
    }

    private static String getSeedString(GeneratorOptions genOpts, RegistryKey<DimensionOptions> dimension) {
        var seed = getSeed(genOpts, dimension);

        return seed != genOpts.getSeed()
            ? Long.toString(seed)
            : "";
    }

    private static long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException e) {
            return seed.hashCode();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xffffff);

        this.drawTextWithShadow(
            matrices,
            this.textRenderer,
            new TranslatableText("draaft.worlds.options.nether"),
            this.width / 2 - 100,
            47,
            0xa0a0a0
        );

        this.drawTextWithShadow(
            matrices,
            this.textRenderer,
            new TranslatableText("draaft.worlds.options.end"),
            this.width / 2 - 100,
            87,
            0xa0a0a0
        );

        this.drawTextWithShadow(
            matrices,
            this.textRenderer,
            new TranslatableText("draaft.worlds.options.seedInfo"),
            this.width / 2 - 100,
            125,
            0xa0a0a0
        );

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(this.parent);
    }
}
