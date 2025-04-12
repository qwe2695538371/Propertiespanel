package qwedshuxingmianban.data;

import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.attribute.AttributeManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    // 内存缓存，用于减少磁盘访问
    private static final Map<UUID, PlayerAttributeData> CACHE = new HashMap<>();

    public static PlayerAttributeData getData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        // 检查缓存
        PlayerAttributeData cachedData = CACHE.get(uuid);
        if (cachedData != null) {
            Qwedshuxingmianban.LOGGER.info("Using cached data for player: " + uuid);
            return cachedData;
        }

        // 从存档加载数据
        PlayerAttributeData data = new PlayerAttributeData();
        ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
        Map<String, Integer> attributes = savedData.getPlayerData(uuid);

        Qwedshuxingmianban.LOGGER.info("Loading data for player: " + uuid + ", found attributes: " + attributes);

        // 设置属性等级并应用
        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            String attributeId = entry.getKey();
            int level = entry.getValue();
            data.setAttributeLevel(attributeId, level);
            AttributeManager.applyAttributeModifier(player, attributeId, level);
            Qwedshuxingmianban.LOGGER.info("Applied attribute: " + attributeId + " with level: " + level);
        }

        CACHE.put(uuid, data);
        return data;
    }

    public static void saveData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerAttributeData data = CACHE.get(uuid);
        if (data != null) {
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            Map<String, Integer> attributes = data.getAllLevels();
            savedData.setPlayerData(uuid, attributes);
            Qwedshuxingmianban.LOGGER.info("Saved data for player: " + uuid + ", attributes: " + attributes);
        }
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        // 玩家离开时保存数据并清除缓存
        saveData(player);
        CACHE.remove(player.getUuid());
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        // 玩家加入时加载数据
        getData(player);
    }
}