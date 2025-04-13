package qwedshuxingmianban.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.network.payload.*;

public class NetworkRegistry {
    private static boolean isInitialized = false;

    public static void registerCommonPayloads() {
        if (isInitialized) {
            Qwedshuxingmianban.LOGGER.info("网络注册表已经初始化，跳过...");
            return;
        }

        try {
            // 确保使用正确的注册表
            var c2sRegistry = PayloadTypeRegistry.playC2S();
            var s2cRegistry = PayloadTypeRegistry.playS2C();

            // 注册所有 Payload 类型
            c2sRegistry.register(AttributeUpgradePayload.ID, AttributeUpgradePayload.CODEC);
            c2sRegistry.register(ResetAttributesPayload.ID, ResetAttributesPayload.CODEC);
            c2sRegistry.register(RequestSyncPayload.ID, RequestSyncPayload.CODEC);
            s2cRegistry.register(AttributeSyncPayload.ID, AttributeSyncPayload.CODEC);

            isInitialized = true;
            Qwedshuxingmianban.LOGGER.info("成功注册所有网络 payload");
        } catch (Exception e) {
            Qwedshuxingmianban.LOGGER.error("注册网络 payload 失败", e);
            e.printStackTrace(); // 添加更详细的错误信息
        }
    }
}