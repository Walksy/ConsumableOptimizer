package walksy.consumableoptimizer.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record HandshakePayload() implements CustomPacketPayload {
    public static final Type<HandshakePayload> ID = new Type<>(Identifier.parse("consumable_optimizer:handshake_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HandshakePayload> CODEC = StreamCodec.unit(new HandshakePayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}