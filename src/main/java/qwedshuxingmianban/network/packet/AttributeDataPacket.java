package qwedshuxingmianban.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import java.util.HashMap;
import java.util.Map;

public class AttributeDataPacket {
    private final Map<String, Integer> levels;
    private final int availableExperience;

    public AttributeDataPacket(Map<String, Integer> levels, int availableExperience) {
        this.levels = levels;
        this.availableExperience = availableExperience;
    }

    public static PacketByteBuf write(Map<String, Integer> levels, int availableExperience) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(levels.size());
        levels.forEach((key, value) -> {
            buf.writeString(key);
            buf.writeInt(value);
        });
        buf.writeInt(availableExperience);
        return buf;
    }

    public static AttributeDataPacket read(PacketByteBuf buf) {
        int size = buf.readInt();
        Map<String, Integer> levels = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            int value = buf.readInt();
            levels.put(key, value);
        }
        int availableExperience = buf.readInt();
        return new AttributeDataPacket(levels, availableExperience);
    }

    public Map<String, Integer> getLevels() {
        return levels;
    }

    public int getAvailableExperience() {
        return availableExperience;
    }
}