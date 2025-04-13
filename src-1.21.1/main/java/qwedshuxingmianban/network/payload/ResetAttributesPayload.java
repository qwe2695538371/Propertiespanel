package qwedshuxingmianban.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;

public record ResetAttributesPayload() implements CustomPayload {
    public static final Id<ResetAttributesPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Qwedshuxingmianban.MOD_ID, "reset_attributes"));

    public static final PacketCodec<PacketByteBuf, ResetAttributesPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {},
                    buf -> new ResetAttributesPayload()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}