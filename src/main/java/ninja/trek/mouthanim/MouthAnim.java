package ninja.trek.mouthanim;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.mouthanim.network.MouthAnimPayloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouthAnim implements ModInitializer {
	public static final String MOD_ID = "mouth-anim";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register custom payloads
		MouthAnimPayloads.registerPayloads();

		// Handle C2S mouth state packets from clients
		ServerPlayNetworking.registerGlobalReceiver(MouthAnimPayloads.C2SMouthState.TYPE, (payload, context) -> {
			ServerPlayer sender = context.player();
			MouthState state = payload.mouthState();

			// Store the state on the server
			MouthStateManager.setState(sender.getUUID(), state);

			// Broadcast to all players tracking this player
			MouthAnimPayloads.S2CMouthState broadcastPacket =
					new MouthAnimPayloads.S2CMouthState(sender.getUUID(), payload.stateId());

			for (ServerPlayer tracker : PlayerLookup.tracking(sender)) {
				ServerPlayNetworking.send(tracker, broadcastPacket);
			}
		});

		// Clean up player state on disconnect
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			MouthStateManager.removePlayer(handler.getPlayer().getUUID());
		});

		LOGGER.info("Mouth Anim mod initialized");
	}
}
