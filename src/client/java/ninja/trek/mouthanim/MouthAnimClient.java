package ninja.trek.mouthanim;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ninja.trek.mouthanim.audio.MicCapture;
import ninja.trek.mouthanim.config.MouthAnimConfig;
import ninja.trek.mouthanim.network.MouthAnimPayloads;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MouthAnimClient implements ClientModInitializer {

    private static final ConcurrentHashMap<UUID, MouthState> REMOTE_MOUTH_STATES = new ConcurrentHashMap<>();
    private static final MicCapture micCapture = new MicCapture();
    private static MouthState lastSentState = MouthState.CLOSED;

    @Override
    public void onInitializeClient() {
        // Load config
        MouthAnimConfig.HANDLER.load();
        MouthAnimConfig config = MouthAnimConfig.HANDLER.instance();

        // Apply config to mic capture
        micCapture.setThresholds(config.maxRms, config.thresholdSlightlyOpen, config.thresholdOpen, config.thresholdWideOpen);
        micCapture.setMixer(config.selectedMixer);

        // Start microphone capture
        micCapture.start();

        // Register S2C handler: receive other players' mouth states
        ClientPlayNetworking.registerGlobalReceiver(MouthAnimPayloads.S2CMouthState.TYPE, (payload, context) -> {
            REMOTE_MOUTH_STATES.put(payload.playerUuid(), payload.mouthState());
        });

        // On each client tick, send mouth state to server if it changed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (!ClientPlayNetworking.canSend(MouthAnimPayloads.C2SMouthState.TYPE)) return;

            MouthState current = micCapture.getCurrentState();
            if (current != lastSentState) {
                lastSentState = current;
                ClientPlayNetworking.send(new MouthAnimPayloads.C2SMouthState(current.getId()));
            }
        });
    }

    public static void applyConfig(MouthAnimConfig config) {
        micCapture.setThresholds(config.maxRms, config.thresholdSlightlyOpen, config.thresholdOpen, config.thresholdWideOpen);
        micCapture.setMixer(config.selectedMixer);
    }

    /**
     * Get the mouth state for a given player UUID.
     * For the local player, returns the mic capture state directly.
     * For remote players, returns the last received state from the server.
     */
    public static MouthState getMouthStateForPlayer(UUID playerUuid) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerUuid)) {
            return micCapture.getCurrentState();
        }
        return REMOTE_MOUTH_STATES.getOrDefault(playerUuid, MouthState.CLOSED);
    }
}
