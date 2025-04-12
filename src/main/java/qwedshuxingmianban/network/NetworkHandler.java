package qwedshuxingmianban.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.data.ModSavedData;
import qwedshuxingmianban.data.PlayerAttributeData;
import qwedshuxingmianban.data.PlayerDataManager;
import net.minecraft.network.PacketByteBuf;
import qwedshuxingmianban.network.packet.AttributeDataPacket;

import java.util.Map;

public class NetworkHandler {
    public static final Identifier SYNC_ATTRIBUTES = new Identifier(Qwedshuxingmianban.MOD_ID, "sync_attributes");
    public static final Identifier UPGRADE_ATTRIBUTE = new Identifier(Qwedshuxingmianban.MOD_ID, "upgrade_attribute");
    public static final Identifier RESET_ATTRIBUTES = new Identifier(Qwedshuxingmianban.MOD_ID, "reset_attributes");
    public static final Identifier REQUEST_SYNC = new Identifier(Qwedshuxingmianban.MOD_ID, "request_sync"); // 新增

    public static void registerServerHandlers() {
        // 处理属性升级请求
        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_ATTRIBUTE, (server, player, handler, buf, responseSender) -> {
            String attributeId = buf.readString();
            server.execute(() -> {
                PlayerAttributeData data = PlayerDataManager.getData(player);
                int currentLevel = data.getAttributeLevel(attributeId);
                int cost = AttributeManager.calculateExperienceCost(attributeId, currentLevel);

                if (player.experienceLevel >= cost &&
                        currentLevel < Qwedshuxingmianban.CONFIG.attributes.get(attributeId).maxLevel) {

                    player.addExperienceLevels(-cost);
                    data.setAttributeLevel(attributeId, currentLevel + 1);
                    AttributeManager.applyAttributeModifier(player, attributeId, currentLevel + 1);
                    PlayerDataManager.saveData(player);

                    // 同步数据到客户端
                    sendAttributeSync(player);
                }
            });
        });
        // 处理客户端请求同步的消息
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_SYNC, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                Qwedshuxingmianban.LOGGER.info("[NetworkHandler] Received sync request from: " +
                        player.getName().getString());
                sendAttributeSync(player);
            });
        });

        // 处理重置属性请求
        ServerPlayNetworking.registerGlobalReceiver(RESET_ATTRIBUTES, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                // 获取玩家数据
                PlayerAttributeData data = PlayerDataManager.getData(player);

                // 计算返还经验
                int totalExp = AttributeManager.calculateTotalExperience(data.getAllLevels());
                int refundExp = AttributeManager.calculateRefundExperience(totalExp);

                // 重置所有属性等级
                data.resetAllAttributes();

                // 移除所有属性修改器
                AttributeManager.resetAttributeModifiers(player);

                // 返还经验
                player.addExperienceLevels(refundExp);

                // 保存数据
                PlayerDataManager.saveData(player);

                // 同步到客户端
                sendAttributeSync(player);
            });
        });
        // 添加请求同步的处理器
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_SYNC, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                // 直接发送同步数据
                sendAttributeSync(player);
            });
        });
    }

    public static void sendAttributeSync(ServerPlayerEntity player) {
        try {
            // 获取最新的存档数据
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            Map<String, Integer> levels = savedData.getPlayerData(player.getUuid());

            Qwedshuxingmianban.LOGGER.info("[NetworkHandler] Sending sync data to client. Player: " +
                    player.getName().getString() + ", Data: " + levels);

            // 发送到客户端
            PacketByteBuf buf = AttributeDataPacket.write(levels, player.experienceLevel);
            ServerPlayNetworking.send(player, SYNC_ATTRIBUTES, buf);
        } catch (Exception e) {
            Qwedshuxingmianban.LOGGER.error("[NetworkHandler] Failed to sync attributes", e);
        }
    }
}