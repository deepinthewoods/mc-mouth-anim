package ninja.trek.mouthanim.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.mouthanim.MouthAnim;
import ninja.trek.mouthanim.MouthState;

import java.util.UUID;

public class MouthAnimPayloads {

    // Client -> Server: player sends their current mouth state
    public record C2SMouthState(byte stateId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<C2SMouthState> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MouthAnim.MOD_ID, "c2s_mouth_state"));

        public static final StreamCodec<FriendlyByteBuf, C2SMouthState> STREAM_CODEC =
                StreamCodec.of(
                        (buf, val) -> buf.writeByte(val.stateId()),
                        buf -> new C2SMouthState(buf.readByte())
                );

        public MouthState mouthState() {
            return MouthState.fromId(stateId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Server -> Client: broadcast a player's mouth state to other clients
    public record S2CMouthState(UUID playerUuid, byte stateId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<S2CMouthState> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MouthAnim.MOD_ID, "s2c_mouth_state"));

        public static final StreamCodec<FriendlyByteBuf, S2CMouthState> STREAM_CODEC =
                StreamCodec.of(
                        (buf, val) -> {
                            buf.writeUUID(val.playerUuid());
                            buf.writeByte(val.stateId());
                        },
                        buf -> new S2CMouthState(buf.readUUID(), buf.readByte())
                );

        public MouthState mouthState() {
            return MouthState.fromId(stateId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(C2SMouthState.TYPE, C2SMouthState.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CMouthState.TYPE, S2CMouthState.STREAM_CODEC);
    }
}
