package qwedshuxingmianban.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import qwedshuxingmianban.Qwedshuxingmianban;

public record RequestSyncPayload() implements CustomPayload {
    public static final Id<RequestSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Qwedshuxingmianban.MOD_ID, "request_sync"));

    public static final PacketCodec<PacketByteBuf, RequestSyncPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {},
                    buf -> new RequestSyncPayload()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}