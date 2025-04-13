package qwedshuxingmianban.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.function.Consumer;

public class ConfirmScreen extends Screen {
    private final Consumer<Boolean> callback;
    private final Text message;

    public ConfirmScreen(Consumer<Boolean> callback, Text title, Text message) {
        super(title);
        this.callback = callback;
        this.message = message;
    }

    @Override
    protected void init() {
        super.init();

        // 添加确认和取消按钮
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.yes"),
                        button -> callback.accept(true))
                .dimensions(this.width / 2 - 105, this.height / 2 + 20, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.no"),
                        button -> callback.accept(false))
                .dimensions(this.width / 2 + 5, this.height / 2 + 20, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.message, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}