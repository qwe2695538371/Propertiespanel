package qwedshuxingmianban.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import qwedshuxingmianban.client.gui.AttributeScreen;
import qwedshuxingmianban.network.NetworkHandler;
import qwedshuxingmianban.network.packet.AttributeDataPacket;

public class QwedshuxingmianbanClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册按键
        KeyBindings.register();

        // 注册按键监听
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.openAttributePanel.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(new AttributeScreen());
                }
            }
        });

        // 注册网络包接收处理
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.SYNC_ATTRIBUTES, (client, handler, buf, responseSender) -> {
            AttributeDataPacket packet = AttributeDataPacket.read(buf);
            client.execute(() -> {
                ClientAttributeData.updateData(packet.getLevels(), packet.getAvailableExperience());
                if (client.currentScreen instanceof AttributeScreen) {
                    ((AttributeScreen) client.currentScreen).refreshData();
                }
            });
        });
    }
}