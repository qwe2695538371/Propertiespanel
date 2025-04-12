package qwedshuxingmianban.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.client.ClientAttributeData;
import qwedshuxingmianban.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.Map;
import java.util.HashMap;
import qwedshuxingmianban.network.NetworkHandler;

public class AttributeScreen extends Screen {
    // 添加滚动相关变量
    private float scrollPosition = 0; // 当前滚动位置
    private boolean isDragging = false; // 是否正在拖动
    private int maxScroll; // 最大滚动距离
    private static final int ITEM_HEIGHT = 30; // 每个属性项的高度
    private static final int SCROLLBAR_WIDTH = 6; // 滚动条宽度
    private static final int VISIBLE_ITEMS = 5; // 可见属性数量
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
    private static final int GUI_HEIGHT = 256;


    public AttributeScreen() {
        super(Text.translatable("screen.qwedshuxingmianban.attribute_panel"));
    }

    @Override
    protected void init() {
        super.init();
        requestSync();

        // 居中显示界面
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        // 计算最大滚动距离
        int totalAttributes = Qwedshuxingmianban.CONFIG.attributes.size();
        maxScroll = Math.max(0, totalAttributes - VISIBLE_ITEMS) * ITEM_HEIGHT;

        // 更新属性按钮位置
        updateButtonPositions();

        // 添加重置按钮
        int resetButtonY = guiTop + GUI_HEIGHT - 30;
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.qwedshuxingmianban.reset"),
                        button -> showResetConfirmation())
                .dimensions(
                        guiLeft + (GUI_WIDTH - 100) / 2,
                        resetButtonY,
                        100,
                        20)
                .build());

        updateButtonStates();
    }

    private void updateButtonPositions() {
        upgradeButtons.clear();
        clearChildren();

        int index = 0;
        int visibleIndex = 0;

        // 调整升级按钮位置，使其更靠近中线
        int buttonX = guiLeft + GUI_WIDTH/2 + 80; // 将按钮移到更靠近中间的位置

        for (Map.Entry<String, ModConfig.AttributeConfig> entry : Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            String attributeId = entry.getKey();

            if (index * ITEM_HEIGHT >= scrollPosition && visibleIndex < VISIBLE_ITEMS) {
                int buttonY = guiTop + 40 + (visibleIndex * ITEM_HEIGHT);
                ButtonWidget upgradeButton = ButtonWidget.builder(
                                Text.literal("+"),
                                button -> onAttributeUpgrade(attributeId))
                        .dimensions(buttonX, buttonY - 2, 20, 20)
                        .build();

                this.addDrawableChild(upgradeButton);
                upgradeButtons.put(attributeId, upgradeButton);
                visibleIndex++;
            }
            index++;
        }

        // 重置按钮居中
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.qwedshuxingmianban.reset"),
                        button -> showResetConfirmation())
                .dimensions(
                        guiLeft + (GUI_WIDTH - 100) / 2,
                        guiTop + GUI_HEIGHT - 30,
                        100,
                        20)
                .build());
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

        // 渲染纯白色背景
        int backgroundColor = 0xFFFFFFFF; // 纯白色，带不透明度
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, backgroundColor);

        // 可以添加一个深色边框
        int borderColor = 0xFF808080; // 灰色边框
        // 绘制边框
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 1, borderColor); // 上边框
        context.fill(guiLeft, guiTop + GUI_HEIGHT - 1, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, borderColor); // 下边框
        context.fill(guiLeft, guiTop, guiLeft + 1, guiTop + GUI_HEIGHT, borderColor); // 左边框
        context.fill(guiLeft + GUI_WIDTH - 1, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, borderColor); // 右边框

        // 渲染标题
        context.drawText(this.textRenderer, this.title,
                guiLeft + (GUI_WIDTH - textRenderer.getWidth(this.title)) / 2,
                guiTop + 8,
                0x000000, // 黑色文字
                false);

        // 渲染经验等级
        String expText = "当前经验等级: " + ClientAttributeData.getAvailableExperience();
        context.drawText(this.textRenderer, expText,
                guiLeft + (GUI_WIDTH - textRenderer.getWidth(expText)) / 2,
                guiTop + 22,
                0x000000,
                false);

        // 渲染属性列表
        int attributesTop = guiTop + 40;
        int index = 0;
        int visibleIndex = 0;

        // 计算左侧起始位置，使内容更居中
        int leftColumnX = guiLeft + GUI_WIDTH/2 - 100;
        int rightColumnX = guiLeft + GUI_WIDTH/2 + 10;

        for (Map.Entry<String, ModConfig.AttributeConfig> entry : Qwedshuxingmianban.CONFIG.attributes.entrySet()) {
            if (index * ITEM_HEIGHT >= scrollPosition && visibleIndex < VISIBLE_ITEMS) {
                String attributeId = entry.getKey();
                ModConfig.AttributeConfig config = entry.getValue();
                int currentLevel = ClientAttributeData.getAttributeLevel(attributeId);

                int textY = attributesTop + (visibleIndex * ITEM_HEIGHT);

                // 渲染属性名称
                context.drawText(this.textRenderer,
                        config.displayName,
                        leftColumnX,
                        textY,
                        0x000000,
                        false);

                // 渲染当前等级
                String levelText = String.format("等级: %d / %d", currentLevel, config.maxLevel);
                context.drawText(this.textRenderer,
                        levelText,
                        rightColumnX,
                        textY,
                        currentLevel >= config.maxLevel ? 0x8B4513 : 0x000000,
                        false);

                // 渲染具体属性值
                double attributeValue = getAttributeValue(attributeId);
                String valueFormat = attributeId.equals("movement_speed") ? "%.2f" : "%.1f";
                String valueText = String.format("当前值: " + valueFormat, attributeValue);
                context.drawText(this.textRenderer,
                        valueText,
                        leftColumnX,
                        textY + 12,
                        0x000000,
                        false);

                // 渲染升级消耗
                if (currentLevel < config.maxLevel) {
                    int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);
                    int costColor = ClientAttributeData.getAvailableExperience() >= cost ?
                            0x008000 : 0x800000; // 使用深绿色和深红色
                    String costText = String.format("消耗: %d", cost);
                    context.drawText(this.textRenderer,
                            costText,
                            rightColumnX,
                            textY + 12,
                            costColor,
                            false);
                }

                visibleIndex++;
            }
            index++;
        }

        // 渲染滚动条（深色）
        if (maxScroll > 0) {
            int scrollbarHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            int scrollbarX = guiLeft + GUI_WIDTH - SCROLLBAR_WIDTH - 8;
            int scrollbarY = guiTop + 40;

            // 滚动条背景
            context.fill(scrollbarX, scrollbarY,
                    scrollbarX + SCROLLBAR_WIDTH,
                    scrollbarY + scrollbarHeight,
                    0x20000000);

            // 滚动条滑块
            float scrollPercentage = scrollPosition / maxScroll;
            int sliderHeight = Math.max(32, scrollbarHeight * VISIBLE_ITEMS /
                    Qwedshuxingmianban.CONFIG.attributes.size());
            int sliderY = scrollbarY + (int)((scrollbarHeight - sliderHeight) * scrollPercentage);

            context.fill(scrollbarX, sliderY,
                    scrollbarX + SCROLLBAR_WIDTH,
                    sliderY + sliderHeight,
                    0xFF808080);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        float scroll = (float) (amount * ITEM_HEIGHT);
        scrollPosition = Math.max(0, Math.min(maxScroll, scrollPosition - scroll));
        updateButtonPositions();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverScrollbar(mouseX, mouseY)) {
            isDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
    public boolean shouldPause() {
        return false;
    }
}