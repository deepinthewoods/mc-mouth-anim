package ninja.trek.mouthanim;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MouthStateManager {
    private static final ConcurrentHashMap<UUID, MouthState> PLAYER_STATES = new ConcurrentHashMap<>();

    public static void setState(UUID playerUuid, MouthState state) {
        PLAYER_STATES.put(playerUuid, state);
    }

    public static MouthState getState(UUID playerUuid) {
        return PLAYER_STATES.getOrDefault(playerUuid, MouthState.CLOSED);
    }

    public static void removePlayer(UUID playerUuid) {
        PLAYER_STATES.remove(playerUuid);
    }
}
