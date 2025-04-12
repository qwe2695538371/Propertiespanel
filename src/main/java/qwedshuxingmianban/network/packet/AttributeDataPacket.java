package qwedshuxingmianban.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.HashMap;
import java.util.Map;

public class AttributeDataPacket {
    private final Map<String, Integer> levels;
    private final int availableExperience;
    private final Map<String, Double> attributeValues;

    public AttributeDataPacket(Map<String, Integer> levels, int availableExperience, Map<String, Double> attributeValues) {
        this.levels = levels;
        this.availableExperience = availableExperience;
        this.attributeValues = attributeValues;
    }

    public static PacketByteBuf write(Map<String, Integer> levels, int experienceLevel, Map<String, Double> attributeValues) {
        PacketByteBuf buf = PacketByteBufs.create();

        // 写入经验等级
        buf.writeInt(experienceLevel);

        // 写入属性等级
        buf.writeInt(levels.size());
        levels.forEach((attributeId, level) -> {
            buf.writeString(attributeId);
            buf.writeInt(level);
        });

        // 写入属性当前值
        buf.writeInt(attributeValues.size());
        attributeValues.forEach((attributeId, value) -> {
            buf.writeString(attributeId);
            buf.writeDouble(value);
        });

        return buf;
    }

    public static AttributeDataPacket read(PacketByteBuf buf) {
        // 读取经验等级
        int experienceLevel = buf.readInt();

        // 读取属性等级
        Map<String, Integer> levels = new HashMap<>();
        int levelCount = buf.readInt();
        for (int i = 0; i < levelCount; i++) {
            String attributeId = buf.readString();
            int level = buf.readInt();
            levels.put(attributeId, level);
        }

        // 读取属性当前值
        Map<String, Double> attributeValues = new HashMap<>();
        int valueCount = buf.readInt();
        for (int i = 0; i < valueCount; i++) {
            String attributeId = buf.readString();
            double value = buf.readDouble();
            attributeValues.put(attributeId, value);
        }

        return new AttributeDataPacket(levels, experienceLevel, attributeValues);
    }

    // Getters
    public Map<String, Integer> getLevels() {
        return levels;
    }

    public int getAvailableExperience() {
        return availableExperience;
    }

    public Map<String, Double> getAttributeValues() {
        return attributeValues;
    }
}