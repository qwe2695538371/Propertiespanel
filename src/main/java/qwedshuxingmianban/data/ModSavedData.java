package qwedshuxingmianban.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import qwedshuxingmianban.Qwedshuxingmianban;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModSavedData extends PersistentState {
    private final Map<UUID, Map<String, Integer>> playerAttributes = new HashMap<>();

    public ModSavedData() {
        super();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();

        for (Map.Entry<UUID, Map<String, Integer>> entry : playerAttributes.entrySet()) {
            NbtCompound playerData = new NbtCompound();
            Map<String, Integer> attributes = entry.getValue();

            // 保存每个属性的等级
            for (Map.Entry<String, Integer> attrEntry : attributes.entrySet()) {
                playerData.putInt(attrEntry.getKey(), attrEntry.getValue());
            }

            // 使用UUID作为键保存玩家数据
            playersNbt.put(entry.getKey().toString(), playerData);
        }

        nbt.put("PlayerAttributes", playersNbt);
        return nbt;
    }

    public static ModSavedData createFromNbt(NbtCompound nbt) {
        ModSavedData data = new ModSavedData();

        if (nbt.contains("PlayerAttributes")) {
            NbtCompound playersNbt = nbt.getCompound("PlayerAttributes");

            // 读取所有玩家的数据
            for (String uuidStr : playersNbt.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    NbtCompound playerData = playersNbt.getCompound(uuidStr);
                    Map<String, Integer> attributes = new HashMap<>();

                    // 读取该玩家的所有属性等级
                    for (String attrKey : playerData.getKeys()) {
                        attributes.put(attrKey, playerData.getInt(attrKey));
                    }

                    data.playerAttributes.put(uuid, attributes);
                } catch (Exception e) {
                    Qwedshuxingmianban.LOGGER.error("Failed to load data for UUID: " + uuidStr, e);
                }
            }
        }

        return data;
    }

    public void setPlayerData(UUID playerUUID, Map<String, Integer> attributes) {
        playerAttributes.put(playerUUID, new HashMap<>(attributes));
        this.markDirty(); // 关键！标记数据已修改，需要保存
    }

    public Map<String, Integer> getPlayerData(UUID playerUUID) {
        return playerAttributes.computeIfAbsent(playerUUID, k -> new HashMap<>());
    }

    // 获取存档数据的静态方法
    public static ModSavedData getOrCreate(ServerWorld world) {
        // 使用世界的PersistentStateManager来获取或创建存档数据
        return world.getPersistentStateManager().getOrCreate(
                ModSavedData::createFromNbt,
                ModSavedData::new,
                Qwedshuxingmianban.MOD_ID + "_player_data" // 确保使用唯一的标识符
        );
    }
}