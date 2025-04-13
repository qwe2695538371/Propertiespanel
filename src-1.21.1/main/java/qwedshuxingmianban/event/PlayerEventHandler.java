package qwedshuxingmianban.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.attribute.AttributeManager;
import qwedshuxingmianban.data.PlayerAttributeData;
import qwedshuxingmianban.data.PlayerDataManager;
import qwedshuxingmianban.network.NetworkHandler;
import java.util.Map;

public class PlayerEventHandler {
    public static void init() {
        // 监听玩家复活事件
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // 获取玩家属性数据
            PlayerAttributeData data = PlayerDataManager.getData(newPlayer);
            Map<String, Integer> levels = data.getAllLevels();

            // 重新应用所有属性
            for (Map.Entry<String, Integer> entry : levels.entrySet()) {
                AttributeManager.applyAttributeModifier(newPlayer, entry.getKey(), entry.getValue());
            }

            // 同步数据到客户端
            NetworkHandler.sendAttributeSync(newPlayer);
        });
        // 监听玩家退出事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerDataManager.saveData(player);
        });
    }
}