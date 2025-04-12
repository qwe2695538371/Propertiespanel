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

        // 从持久化存档中读取数据
        PlayerAttributeData data = new PlayerAttributeData();
        ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
        Map<String, Integer> attributes = savedData.getPlayerData(uuid);

        Qwedshuxingmianban.LOGGER.info("Loading data for player: " + uuid + ", attributes: " + attributes);

        // 应用属性数据到 PlayerAttributeData，并同步属性（如最大生命值等）
        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            data.setAttributeLevel(entry.getKey(), entry.getValue());
            AttributeManager.applyAttributeModifier(player, entry.getKey(), entry.getValue());
            Qwedshuxingmianban.LOGGER.info("Applied attribute: " + entry.getKey() + " with level: " + entry.getValue());
        }

        CACHE.put(uuid, data);
        return data;
    }

    public static void saveData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerAttributeData data = CACHE.get(uuid);
        if (data != null) {
            ModSavedData savedData = ModSavedData.getOrCreate(player.getServerWorld());
            savedData.setPlayerData(uuid, data.getAllLevels());
            Qwedshuxingmianban.LOGGER.info("Saved data for player: " + uuid + ", attributes: " + data.getAllLevels());
        }
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        saveData(player);
        CACHE.remove(player.getUuid());
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        getData(player);
    }
}