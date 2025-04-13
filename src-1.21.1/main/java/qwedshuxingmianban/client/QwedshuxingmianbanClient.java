package qwedshuxingmianban.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import qwedshuxingmianban.client.gui.AttributeScreen;
import qwedshuxingmianban.network.NetworkRegistry;
import qwedshuxingmianban.network.payload.*;

public class QwedshuxingmianbanClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册网络包类型（现在只在一个地方注册）
        NetworkRegistry.registerCommonPayloads();

        // 注册客户端接收器
        ClientPlayNetworking.registerGlobalReceiver(AttributeSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientAttributeData.updateData(
                        payload.levels(),
                        payload.experienceLevel(),
                        payload.attributeValues()
                );
                if (context.client().currentScreen instanceof AttributeScreen) {
                    ((AttributeScreen) context.client().currentScreen).refreshData();
                }
            });
        });

        // 注册按键等其他客户端功能
        KeyBindings.register();
        registerKeyListeners();
    }

    private void registerKeyListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.openAttributePanel.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(new AttributeScreen());
                }
            }
        });
    }
}