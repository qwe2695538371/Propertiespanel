package qwedshuxingmianban.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.client.ClientAttributeData;
import qwedshuxingmianban.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.Formatting;
import java.util.Map;
import java.util.HashMap;
import qwedshuxingmianban.network.NetworkHandler;

public class AttributeScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier(Qwedshuxingmianban.MOD_ID, "textures/gui/attribute_panel.png");
    private static final int TEXTURE_WIDTH = 256; // 增加宽度以容纳更多信息
    private static final int TEXTURE_HEIGHT = 200; // 增加高度以避免重叠
    private int guiLeft;
    private int guiTop;
    private final Map<String, ButtonWidget> upgradeButtons = new HashMap<>();
    private boolean isLoading = true;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private long lastSyncRequest = 0;
    private static final long RETRY_DELAY = 1000; // 1秒重试延迟

    public AttributeScreen() {
        super(Text.translatable("screen.qwedshuxingmianban.attribute_panel"));
    }

    @Override
    protected void init() {
        super.init();
        // 发送同步请求
        requestSync();

        this.guiLeft = (this.width - TEXTURE_WIDTH) / 2;
        this.guiTop = (this.height - TEXTURE_HEIGHT) / 2;

        // 添加属性按钮
        int buttonY = guiTop + 30; // 调整起始Y坐标
        for (Map.Entry<String, ModConfig.AttributeConfig> entry : Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            String attributeId = entry.getKey();

            ButtonWidget upgradeButton = ButtonWidget.builder(
                            Text.literal("+"),
                            button -> onAttributeUpgrade(attributeId))
                    .dimensions(guiLeft + TEXTURE_WIDTH - 40, buttonY, 20, 20)
                    .build();

            this.addDrawableChild(upgradeButton);
            upgradeButtons.put(attributeId, upgradeButton);

            buttonY += 30; // 增加间距
        }

        // 添加重置按钮，移到底部中央
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.qwedshuxingmianban.reset"),
                        button -> showResetConfirmation())
                .dimensions(guiLeft + (TEXTURE_WIDTH - 100) / 2, guiTop + TEXTURE_HEIGHT - 30, 100, 20)
                .build());

        updateButtonStates();
    }
    private void requestSync() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncRequest < RETRY_DELAY) {
            return; // 防止过于频繁的请求
        }

        if (retryCount < MAX_RETRIES) {
            isLoading = true;
            lastSyncRequest = currentTime;
            PacketByteBuf buf = PacketByteBufs.create();
            ClientPlayNetworking.send(NetworkHandler.REQUEST_SYNC, buf);
            retryCount++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // 渲染背景纹理
        context.drawTexture(TEXTURE, guiLeft, guiTop, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 渲染标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, guiTop + 10, 0xFFFFFF);

        // 渲染经验等级 - 移到顶部
        String expText = "当前经验等级: " + ClientAttributeData.getAvailableExperience();
        context.drawTextWithShadow(this.textRenderer, Text.literal(expText),
                guiLeft + 10, guiTop + 10, 0xFFFFFF);

        // 渲染属性信息
        int textY = guiTop + 35; // 调整起始Y坐标
        for (Map.Entry<String, ModConfig.AttributeConfig> entry : Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            String attributeId = entry.getKey();
            ModConfig.AttributeConfig config = entry.getValue();
            int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);

            // 渲染属性名称
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal(config.displayName),
                    guiLeft + 10, textY, 0xFFFFFF);

            // 渲染当前等级
            Text levelText = Text.literal(String.format("等级: %d / %d", currentLevel, config.maxLevel));
            context.drawTextWithShadow(this.textRenderer, levelText,
                    guiLeft + 120, textY, currentLevel >= config.maxLevel ? 0xFFAA00 : 0xFFFFFF);

            // 渲染具体属性值
            double attributeValue = getAttributeValue(attributeId);
            String valueFormat = attributeId.equals("movement_speed") ? "%.2f" : "%.1f";
            Text valueText = Text.literal(String.format("当前值: " + valueFormat, attributeValue));
            context.drawTextWithShadow(this.textRenderer, valueText,
                    guiLeft + 10, textY + 12, 0x00FF00);

            // 渲染升级消耗
            if (currentLevel < config.maxLevel) {
                int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);
                Text costText = Text.literal(String.format("升级消耗: %d", cost))
                        .formatted(ClientAttributeData.getAvailableExperience() >= cost ? Formatting.GREEN : Formatting.RED);
                context.drawTextWithShadow(this.textRenderer, costText,
                        guiLeft + 120, textY + 12, 0xFFFFFF);
            }

            textY += 30; // 增加间距
        }

        super.render(context, mouseX, mouseY, delta);
    }
    private double getAttributeValue(String attributeId) {
        if (this.client != null && this.client.player != null) {
            EntityAttribute attribute = AttributeManager.getAttributeById(attributeId);
            EntityAttributeInstance attributeInstance = this.client.player.getAttributeInstance(attribute);
            if (attributeInstance != null) {
                return attributeInstance.getValue();
            }
        }
        return 0.0;
    }

    private void onAttributeUpgrade(String attributeId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(attributeId);
        ClientPlayNetworking.send(NetworkHandler.UPGRADE_ATTRIBUTE, buf);
    }

    private void showResetConfirmation() {
        this.client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        ClientPlayNetworking.send(NetworkHandler.RESET_ATTRIBUTES, buf);
                    }
                    this.client.setScreen(this);
                },
                Text.translatable("screen.qwedshuxingmianban.reset_confirm.title"),
                Text.translatable("screen.qwedshuxingmianban.reset_confirm.message")
        ));
    }

    public void refreshData() {
        isLoading = false;
        retryCount = 0;
        // 强制重新渲染
        if (this.client != null) {
            this.client.execute(() -> {
                if (this.client.currentScreen == this) {
                    // 只在当前屏幕仍然是属性面板时更新
                    this.client.currentScreen.resize(this.client, this.width, this.height);
                }
            });
        }
    }

    private void updateButtonStates() {
        for (Map.Entry<String, ButtonWidget> entry : upgradeButtons.entrySet()) {
            String attributeId = entry.getKey();
            ButtonWidget button = entry.getValue();

            int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);
            ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
            int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);

            // 禁用按钮如果:
            // 1. 达到最大等级
            // 2. 经验不足
            boolean canUpgrade = currentLevel < config.maxLevel &&
                    ClientAttributeData.getAvailableExperience() >= cost;
            button.active = canUpgrade;
        }
    }

    @Override
    public boolean shouldPause() {  // 改为 shouldPause
        return false;
    }
}