package qwedshuxingmianban.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.data.ModSavedData;
import qwedshuxingmianban.data.PlayerAttributeData;
import qwedshuxingmianban.data.PlayerDataManager;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.network.payload.*;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandler {

    public static void registerServerHandlers() {
        // 处理属性升级请求
        ServerPlayNetworking.registerGlobalReceiver(AttributeUpgradePayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    context.server().execute(() -> {
                        PlayerAttributeData data = PlayerDataManager.getData(player);
                        int currentLevel = data.getAttributeLevel(payload.attributeId());
                        int cost = AttributeManager.calculateExperienceCost(payload.attributeId(), currentLevel);

                        if (player.experienceLevel >= cost &&
                                currentLevel < Qwedshuxingmianban.CONFIG.attributes.get(payload.attributeId()).maxLevel) {
                            player.addExperienceLevels(-cost);
                            data.setAttributeLevel(payload.attributeId(), currentLevel + 1);
                            AttributeManager.applyAttributeModifier(player, payload.attributeId(), currentLevel + 1);
                            PlayerDataManager.saveData(player);
                            sendAttributeSync(player);
                        }
                    });
                }
        );

        // 处理重置属性请求
        ServerPlayNetworking.registerGlobalReceiver(ResetAttributesPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    context.server().execute(() -> {
                        PlayerAttributeData data = PlayerDataManager.getData(player);
                        int totalExp = AttributeManager.calculateTotalExperience(data.getAllLevels());
                        int refundExp = AttributeManager.calculateRefundExperience(totalExp);

                        data.resetAllAttributes();
                        AttributeManager.resetAttributeModifiers(player);
                        player.addExperienceLevels(refundExp);
                        PlayerDataManager.saveData(player);
                        sendAttributeSync(player);
                    });
                }
        );

        // 处理同步请求
        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    context.server().execute(() -> sendAttributeSync(player));
                }
        );
    }

    public static void sendAttributeSync(ServerPlayerEntity player) {
        try {
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            Map<String, Integer> levels = savedData.getPlayerData(player.getUuid());
            Map<String, Double> attributeValues = new HashMap<>();

            AttributeManager.ATTRIBUTES.forEach((attributeId, attribute) -> {
                if (attribute != null) {
                    double value = player.getAttributeValue(attribute);
                    attributeValues.put(attributeId, value);
                }
            });

            // 发送同步数据包，包含玩家当前经验等级
            ServerPlayNetworking.send(
                    player,
                    new AttributeSyncPayload(levels, player.experienceLevel, attributeValues)
            );
        } catch (Exception e) {
            Qwedshuxingmianban.LOGGER.error("[NetworkHandler] Failed to sync attributes", e);
        }
    }
}