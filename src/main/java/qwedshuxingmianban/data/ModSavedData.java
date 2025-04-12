package qwedshuxingmianban.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import qwedshuxingmianban.Qwedshuxingmianban;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModSavedData extends PersistentState {
    private final Map<UUID, Map<String, Integer>> playerAttributes = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();

        // 保存每个玩家的属性数据
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerAttributes.entrySet()) {
            NbtCompound playerData = new NbtCompound();
            Map<String, Integer> attributes = entry.getValue();

            for (Map.Entry<String, Integer> attrEntry : attributes.entrySet()) {
                playerData.putInt(attrEntry.getKey(), attrEntry.getValue());
            }

            playersNbt.put(entry.getKey().toString(), playerData);
            Qwedshuxingmianban.LOGGER.info("Saving data for player: " + entry.getKey() +
                    ", attributes: " + attributes);
        }

        nbt.put("PlayerAttributes", playersNbt);
        return nbt;
    }

    public static ModSavedData createFromNbt(NbtCompound nbt) {
        ModSavedData data = new ModSavedData();

        if (nbt.contains("PlayerAttributes", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playersNbt = nbt.getCompound("PlayerAttributes");

            for (String uuidStr : playersNbt.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    NbtCompound playerData = playersNbt.getCompound(uuidStr);
                    Map<String, Integer> attributes = new HashMap<>();

                    for (String attrKey : playerData.getKeys()) {
                        attributes.put(attrKey, playerData.getInt(attrKey));
                    }

                    data.playerAttributes.put(uuid, attributes);
                    Qwedshuxingmianban.LOGGER.info("Loaded data for player: " + uuid +
                            ", attributes: " + attributes);
                } catch (Exception e) {
                    Qwedshuxingmianban.LOGGER.error("Failed to load data for UUID: " + uuidStr, e);
                }
            }
        } else {
            Qwedshuxingmianban.LOGGER.warn("No PlayerAttributes found in NBT!");
        }

        return data;
    }

    public void setPlayerData(UUID playerUUID, Map<String, Integer> attributes) {
        playerAttributes.put(playerUUID, new HashMap<>(attributes));
        this.markDirty();
    }

    public Map<String, Integer> getPlayerData(UUID playerUUID) {
        return playerAttributes.computeIfAbsent(playerUUID, k -> new HashMap<>());
    }

    public static ModSavedData getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                ModSavedData::createFromNbt,
                ModSavedData::new,
                Qwedshuxingmianban.MOD_ID + "_player_data"
        );
    }
}