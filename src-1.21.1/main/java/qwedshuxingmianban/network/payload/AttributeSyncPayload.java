package qwedshuxingmianban.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;

import java.util.Map;
import java.util.HashMap;

public record AttributeSyncPayload(
        Map<String, Integer> levels,
        int experienceLevel,
        Map<String, Double> attributeValues
) implements CustomPayload {
    public static final Id<AttributeSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Qwedshuxingmianban.MOD_ID, "sync_attributes"));

    public static final PacketCodec<PacketByteBuf, AttributeSyncPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        // 1. 先写入 Map 的大小
                        buf.writeVarInt(value.levels.size());
                        // 2. 写入 levels map
                        value.levels.forEach((key, val) -> {
                            buf.writeString(key);
                            buf.writeVarInt(val);
                        });

                        // 3. 写入经验等级
                        buf.writeVarInt(value.experienceLevel);

                        // 4. 写入属性值 Map 的大小
                        buf.writeVarInt(value.attributeValues.size());
                        // 5. 写入属性值
                        value.attributeValues.forEach((key, val) -> {
                            buf.writeString(key);
                            buf.writeDouble(val);
                        });
                    },
                    buf -> {
                        // 1. 读取 levels map
                        int levelsSize = buf.readVarInt();
                        Map<String, Integer> levels = new HashMap<>();
                        for (int i = 0; i < levelsSize; i++) {
                            String key = buf.readString();
                            int value = buf.readVarInt();
                            levels.put(key, value);
                        }

                        // 2. 读取经验等级
                        int expLevel = buf.readVarInt();

                        // 3. 读取属性值 map
                        int attrSize = buf.readVarInt();
                        Map<String, Double> attributes = new HashMap<>();
                        for (int i = 0; i < attrSize; i++) {
                            String key = buf.readString();
                            double value = buf.readDouble();
                            attributes.put(key, value);
                        }

                        return new AttributeSyncPayload(levels, expLevel, attributes);
                    }
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}