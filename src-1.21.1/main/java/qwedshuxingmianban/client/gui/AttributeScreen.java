package qwedshuxingmianban.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.client.ClientAttributeData;
import qwedshuxingmianban.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.Map;
import java.util.HashMap;
import qwedshuxingmianban.network.NetworkHandler;
import qwedshuxingmianban.network.payload.AttributeUpgradePayload;
import qwedshuxingmianban.network.payload.RequestSyncPayload;
import qwedshuxingmianban.network.payload.ResetAttributesPayload;

public class AttributeScreen extends Screen {
    // 添加滚动相关变量
    private float scrollPosition = 0; // 当前滚动位置
    private boolean isDragging = false; // 是否正在拖动
    private int maxScroll; // 最大滚动距离
    private static final int ITEM_HEIGHT = 32; // 每个属性项的高度
    private static final int SCROLLBAR_WIDTH = 6; // 滚动条宽度
    private static final int VISIBLE_ITEMS = 5; // 可见属性数量
    // 颜色常量
    private static final int COLOR_TITLE = 0x4F3A2C; // 深棕色标题
    private static final int COLOR_TEXT = 0x3F3F3F; // 深灰色文本
    private static final int COLOR_VALUE = 0x2C5A8F; // 蓝色属性值
    private static final int COLOR_MAX_LEVEL = 0x8B4513; // 棕色最大等级
    private static final int COLOR_COST_AFFORDABLE = 0x2E7D32; // 绿色可支付
    private static final int COLOR_COST_EXPENSIVE = 0xC62828; // 红色不可支付
    private static final int COLOR_BUTTON_ENABLED = 0x4CAF50; // 绿色按钮
    private static final int COLOR_BUTTON_DISABLED = 0x9E9E9E; // 灰色按钮

    private int guiLeft;
    private int guiTop;
    private final Map<String, ButtonWidget> upgradeButtons = new HashMap<>();
    private boolean isLoading = true;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private long lastSyncRequest = 0;
    private static final long RETRY_DELAY = 1000; // 1秒重试延迟
    // 设置更大的界面尺寸，但保持原书本的宽高比
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 230;

    private static final String TRANSLATION_PREFIX = "gui.qwedshuxingmianban.";
    private static final String ATTRIBUTE_PREFIX = "attribute.qwedshuxingmianban.";


    public AttributeScreen() {
        super(Text.translatable(TRANSLATION_PREFIX + "title"));
    }

    @Override
    protected void init() {
        super.init();

        // 居中显示界面
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        // 计算最大滚动距离
        int totalAttributes = Qwedshuxingmianban.CONFIG.attributes.size();
        maxScroll = Math.max(0, totalAttributes - VISIBLE_ITEMS) * ITEM_HEIGHT;

        // 先清除所有按钮
        this.clearChildren();

        // 先添加升级按钮
        updateButtonPositions();

        // 最后添加重置按钮，确保它在最上层
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.qwedshuxingmianban.reset"),
                        button -> showResetConfirmation())
                .dimensions(
                        guiLeft + (GUI_WIDTH - 100) / 2,
                        guiTop + GUI_HEIGHT - 30,
                        100,
                        20)
                .build());

        // 请求同步数据
        requestSync();
    }

    private void updateButtonPositions() {
        // 保存旧按钮的状态
        Map<String, Boolean> oldButtonStates = new HashMap<>();
        upgradeButtons.forEach((id, button) -> oldButtonStates.put(id, button.active));

        // 清除旧的升级按钮
        upgradeButtons.clear();

        // 移除旧的升级按钮，但保留重置按钮
        this.children().removeIf(child ->
                child instanceof ButtonWidget &&
                        !((ButtonWidget)child).getMessage().getString().equals(
                                Text.translatable("button.qwedshuxingmianban.reset").getString()
                        )
        );

        int index = 0;
        int visibleIndex = 0;
        int buttonX = guiLeft + GUI_WIDTH/2 + 80;

        // 添加新的升级按钮
        for (Map.Entry<String, ModConfig.AttributeConfig> entry : Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            String attributeId = entry.getKey();

            if (index * ITEM_HEIGHT >= scrollPosition && visibleIndex < VISIBLE_ITEMS) {
                int buttonY = guiTop + 40 + (visibleIndex * ITEM_HEIGHT);

                ButtonWidget upgradeButton = ButtonWidget.builder(
                                Text.literal("+"),
                                button -> onAttributeUpgrade(attributeId)
                        )
                        .dimensions(buttonX, buttonY - 2, 20, 20)
                        .build();

                // 恢复按钮状态
                if (oldButtonStates.containsKey(attributeId)) {
                    upgradeButton.active = oldButtonStates.get(attributeId);
                }

                // 确保按钮被添加到屏幕
                this.addDrawableChild(upgradeButton);
                upgradeButtons.put(attributeId, upgradeButton);

                visibleIndex++;
            }
            index++;
        }

        // 更新按钮状态
        updateButtonStates();
    }
    private void requestSync() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncRequest < RETRY_DELAY) {
            return;
        }

        if (retryCount < MAX_RETRIES) {
            isLoading = true;
            lastSyncRequest = currentTime;
            // 使用新的 RequestSyncPayload
            ClientPlayNetworking.send(new RequestSyncPayload());
            retryCount++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || Qwedshuxingmianban.CONFIG == null ||
                Qwedshuxingmianban.CONFIG.attributes == null ||
                Qwedshuxingmianban.CONFIG.attributes.isEmpty()) {
            return;
        }

        // 渲染半透明背景
        //this.renderBackground(context);

        // 渲染主面板背景
        renderPanelBackground(context);

        // 渲染装饰性边框和阴影
        renderDecorations(context);

        super.render(context, mouseX, mouseY, delta);

        try {
            // 渲染标题和经验等级
            renderHeader(context);

            // 渲染属性列表
            renderAttributes(context);

            // 渲染滚动条
            renderScrollbar(context);

            // 渲染悬浮提示
            //renderTooltips(context, mouseX, mouseY);
        } catch (Exception ignored) {
        }
    }
    private void renderPanelBackground(DrawContext context) {
        // 主面板背景
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFFF5F5F5);

        // 渐变边框
        int borderStart = 0xFFE0E0E0;
        int borderEnd = 0xFFBDBDBD;

        // 上边框渐变
        for (int i = 0; i < 2; i++) {
            int color = interpolateColor(borderStart, borderEnd, i / 2f);
            context.fill(guiLeft, guiTop + i, guiLeft + GUI_WIDTH, guiTop + i + 1, color);
        }

        // 下边框渐变
        for (int i = 0; i < 2; i++) {
            int color = interpolateColor(borderStart, borderEnd, i / 2f);
            context.fill(guiLeft, guiTop + GUI_HEIGHT - 2 + i,
                    guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT - 1 + i, color);
        }
    }
    private void renderDecorations(DrawContext context) {
        // 顶部装饰条
        context.fill(guiLeft + 10, guiTop + 30,
                guiLeft + GUI_WIDTH - 10, guiTop + 32,
                0x1F000000);

        // 属性区域的轻微阴影
        context.fill(guiLeft + 5, guiTop + 40,
                guiLeft + GUI_WIDTH - 5, guiTop + 42,
                0x1F000000);
    }
    private void renderHeader(DrawContext context) {
        // 渲染标题
        Text title = this.title;
        if (title != null) {
            context.drawText(this.textRenderer, title,
                    guiLeft + (GUI_WIDTH - textRenderer.getWidth(title)) / 2,
                    guiTop + 10,
                    COLOR_TITLE,
                    false);
        }

        // 渲染经验等级，使用翻译键
        String expText = Text.translatable(TRANSLATION_PREFIX + "experience_level",
                ClientAttributeData.getAvailableExperience()).getString();
        int expWidth = textRenderer.getWidth(expText);
        int expX = guiLeft + (GUI_WIDTH - expWidth) / 2;
        int expY = guiTop + 22;

        // 绘制经验球图标
        context.drawText(this.textRenderer, "✧",
                expX - 12, expY, 0x55FF55, true);

        // 绘制经验文本
        context.drawText(this.textRenderer, expText,
                expX, expY, COLOR_VALUE, false);
    }

    private void renderAttributes(DrawContext context) {
        int attributesTop = guiTop + 45;
        int index = 0;
        int visibleIndex = 0;
        int leftColumnX = guiLeft + 15;
        int rightColumnX = guiLeft + GUI_WIDTH - 120;

        for (Map.Entry<String, ModConfig.AttributeConfig> entry :
                Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            if (entry == null || entry.getKey() == null) continue;

            if (index * ITEM_HEIGHT >= scrollPosition && visibleIndex < VISIBLE_ITEMS) {
                String attributeId = entry.getKey();
                ModConfig.AttributeConfig config = entry.getValue();
                if (config == null) continue;

                int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);
                int textY = attributesTop + (visibleIndex * ITEM_HEIGHT);

                // 绘制交替背景
                if (visibleIndex % 2 == 0) {
                    context.fill(guiLeft + 10, textY - 2,
                            guiLeft + GUI_WIDTH - 10, textY + ITEM_HEIGHT - 2,
                            0x08000000);
                }

                // 渲染属性名称和图标
                String icon = getAttributeIcon(attributeId);
                Text attrName = Text.translatable(ATTRIBUTE_PREFIX + attributeId);
                context.drawText(this.textRenderer,
                        icon + " " + attrName.getString(),
                        leftColumnX, textY + 2,
                        COLOR_TEXT, false);

                // 渲染等级信息
                String levelText = Text.translatable(TRANSLATION_PREFIX + "level",
                        currentLevel, config.maxLevel).getString();
                context.drawText(this.textRenderer,
                        levelText,
                        rightColumnX, textY + 2,
                        currentLevel >= config.maxLevel ? COLOR_MAX_LEVEL : COLOR_TEXT,
                        false);

                // 渲染属性值
                double attributeValue = getAttributeValue(attributeId);
                String valueFormat = attributeId.equals("movement_speed") ? "%.2f" : "%.1f";
                String valueText = Text.translatable(TRANSLATION_PREFIX + "current_value",
                        String.format(valueFormat, attributeValue)).getString();
                context.drawText(this.textRenderer,
                        valueText,
                        leftColumnX, textY + 14,
                        COLOR_VALUE, false);

                // 渲染升级消耗
                if (currentLevel < config.maxLevel) {
                    int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);
                    int costColor = ClientAttributeData.getAvailableExperience() >= cost ?
                            COLOR_COST_AFFORDABLE : COLOR_COST_EXPENSIVE;
                    String costText = Text.translatable(TRANSLATION_PREFIX + "upgrade_cost",
                            cost).getString();
                    context.drawText(this.textRenderer,
                            costText,
                            rightColumnX, textY + 14,
                            costColor, false);
                }

                visibleIndex++;
            }
            index++;
        }
    }
    // 辅助方法：获取属性图标
    private String getAttributeIcon(String attributeId) {
        return switch (attributeId) {
            case "movement_speed" -> "⚡"; // 速度
            case "max_health" -> "♥"; // 生命
            case "attack_damage" -> "⚔"; // 攻击
            case "defense" -> "🛡"; // 防御
            default -> "✦"; // 默认图标
        };
    }

    // 辅助方法：颜色插值
    private int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        int a = (int) (a1 + (a2 - a1) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    private void renderScrollbar(DrawContext context) {
        if (maxScroll > 0) {
            try {
                int scrollbarHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
                int scrollbarX = guiLeft + GUI_WIDTH - SCROLLBAR_WIDTH - 8;
                int scrollbarY = guiTop + 40;

                // 滚动条背景
                context.fill(scrollbarX, scrollbarY,
                        scrollbarX + SCROLLBAR_WIDTH,
                        scrollbarY + scrollbarHeight,
                        0x40000000);

                // 滚动条滑块
                float scrollPercentage = scrollPosition / maxScroll;
                int sliderHeight = Math.max(32, scrollbarHeight * VISIBLE_ITEMS /
                        Qwedshuxingmianban.CONFIG.attributes.size());
                int sliderY = scrollbarY + (int)((scrollbarHeight - sliderHeight) * scrollPercentage);

                context.fill(scrollbarX, sliderY,
                        scrollbarX + SCROLLBAR_WIDTH,
                        sliderY + sliderHeight,
                        0xFF606060);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverScrollbar(mouseX, mouseY)) {
            isDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    // 添加鼠标滚轮支持
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollPosition = (float) Math.max(0, Math.min(maxScroll,
                    scrollPosition - verticalAmount * ITEM_HEIGHT / 2));
            updateButtonPositions();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            int scrollbarHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            float dragPercentage = (float) (mouseY - (guiTop + 35)) / scrollbarHeight;
            scrollPosition = Math.max(0, Math.min(maxScroll, maxScroll * dragPercentage));
            updateButtonPositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int scrollbarX = guiLeft + GUI_WIDTH - SCROLLBAR_WIDTH - 8;
        int scrollbarY = guiTop + 40;
        int scrollbarHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
        return mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }
    private double getAttributeValue(String attributeId) {
        // 直接从ClientAttributeData获取同步的值
        return ClientAttributeData.getAttributeValue(attributeId);
    }

    private void onAttributeUpgrade(String attributeId) {
        if (this.client != null && this.client.player != null) {
            try {
                // 检查是否可以升级
                int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);
                ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
                if (config == null) return;

                int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);
                if (currentLevel >= config.maxLevel ||
                        ClientAttributeData.getAvailableExperience() < cost) {
                    return;
                }

                // 发送升级请求
                ClientPlayNetworking.send(new AttributeUpgradePayload(attributeId));
            } catch (Exception e) {
                Qwedshuxingmianban.LOGGER.error("Error sending attribute upgrade request", e);
            }
        }
    }

    private void showResetConfirmation() {
        if (this.client != null) {
            this.client.setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            // 使用新的 ResetAttributesPayload
                            ClientPlayNetworking.send(new ResetAttributesPayload());
                        }
                        this.client.setScreen(this);
                    },
                    Text.translatable("screen.qwedshuxingmianban.reset_confirm.title"),
                    Text.translatable("screen.qwedshuxingmianban.reset_confirm.message")
            ));
        }
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
        if (!upgradeButtons.isEmpty()) {
            for (Map.Entry<String, ButtonWidget> entry : upgradeButtons.entrySet()) {
                String attributeId = entry.getKey();
                ButtonWidget button = entry.getValue();

                if (button != null && Qwedshuxingmianban.CONFIG.attributes.containsKey(attributeId)) {
                    int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);
                    ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
                    int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);

                    // 使用 ClientAttributeData 中的经验等级
                    boolean canUpgrade = currentLevel < config.maxLevel &&
                            ClientAttributeData.getAvailableExperience() >= cost;
                    button.active = canUpgrade;
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}