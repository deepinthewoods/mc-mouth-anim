package ninja.trek.mouthanim;

public enum MouthState {
    CLOSED(0),
    SLIGHTLY_OPEN(1),
    OPEN(2),
    WIDE_OPEN(3);

    private final byte id;

    MouthState(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }

    public static MouthState fromId(byte id) {
        return switch (id) {
            case 1 -> SLIGHTLY_OPEN;
            case 2 -> OPEN;
            case 3 -> WIDE_OPEN;
            default -> CLOSED;
        };
    }
}
