package walksy.consumableoptimizer.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DisableModPayload() implements CustomPayload {
    public static final Id<DisableModPayload> ID = new Id<>(Identifier.of("consumable_optimizer:disable_payload"));
    public static final PacketCodec<RegistryByteBuf, DisableModPayload> CODEC = PacketCodec.unit(new DisableModPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
