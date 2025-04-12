package qwedshuxingmianban.data;

import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static final Map<UUID, PlayerAttributeData> CACHE = new HashMap<>();

    public static PlayerAttributeData getData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        // 首先检查缓存中是否已有数据
        PlayerAttributeData data = CACHE.get(uuid);
        if (data == null) {
            // 只在缓存中没有数据时创建新实例
            data = new PlayerAttributeData();
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            Map<String, Integer> attributes = savedData.getPlayerData(uuid);

            // 使用新增的 updateFromMap 方法更新数据
            data.updateFromMap(attributes);

            // 应用所有属性
            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                AttributeManager.applyAttributeModifier(player, entry.getKey(), entry.getValue());
            }

            CACHE.put(uuid, data);
            Qwedshuxingmianban.LOGGER.info("Loaded data for player: " + uuid + ", attributes: " + attributes);
        }

        return data;
    }

    public static void saveData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerAttributeData data = CACHE.get(uuid);
        if (data != null && data.hasChanged()) {  // 只在数据发生变化时保存
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            savedData.setPlayerData(uuid, data.getAllLevels());
            data.resetChanged();  // 重置变化标记
            Qwedshuxingmianban.LOGGER.info("Saved data for player: " + uuid + ", attributes: " + data.getAllLevels());
        }
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        saveData(player);
        CACHE.remove(player.getUuid());
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        getData(player);  // 这会从存档加载数据并缓存
    }
}