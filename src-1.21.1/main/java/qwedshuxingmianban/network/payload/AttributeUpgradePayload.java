package qwedshuxingmianban.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;

public record AttributeUpgradePayload(String attributeId) implements CustomPayload {
    public static final Id<AttributeUpgradePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Qwedshuxingmianban.MOD_ID, "upgrade_attribute"));

    public static final PacketCodec<PacketByteBuf, AttributeUpgradePayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeString(value.attributeId, 32767), // 添加最大长度限制
                    buf -> new AttributeUpgradePayload(buf.readString(32767))  // 添加最大长度限制
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}