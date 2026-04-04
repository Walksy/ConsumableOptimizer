package walksy.consumableoptimizer.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record DisableModPayload() implements CustomPacketPayload {
    public static final Type<DisableModPayload> ID = new Type<>(Identifier.parse("consumable_optimizer:disable_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DisableModPayload> CODEC = StreamCodec.unit(new DisableModPayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}