package aethereal.type;

public enum PushType {
    BLOCKS,
    PLAYERS,
    WATER;

    public boolean isBlocks() {
        return this == BLOCKS;
    }

    public boolean isPlayers() {
        return this == PLAYERS;
    }

    public boolean isWater() {
        return this == WATER;
    }
}
