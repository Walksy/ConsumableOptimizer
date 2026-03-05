package walksy.consumableoptimizer.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HandshakePayload() implements CustomPayload {
    public static final Id<HandshakePayload> ID = new Id<>(Identifier.of("consumable_optimizer:handshake_payload"));
    public static final PacketCodec<RegistryByteBuf, HandshakePayload> CODEC = PacketCodec.unit(new HandshakePayload());

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
