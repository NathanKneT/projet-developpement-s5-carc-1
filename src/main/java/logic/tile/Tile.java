package logic.tile;

import logic.math.Vector2;

public class Tile {
    private Vector2 position;
    private Chunk[] chunks;
    private boolean isStartingTile;

    public Tile() {
        chunks = new Chunk[ChunkOffset.values().length];
        chunks[0] = new Chunk(ChunkType.FIELD);
        chunks[1] = new Chunk(ChunkType.FIELD);
        chunks[2] = new Chunk(ChunkType.FIELD);
        chunks[3] = new Chunk(ChunkType.FIELD);
        chunks[4] = new Chunk(ChunkType.FIELD);
    }

    public Tile(boolean isStartingTile) {
        this.isStartingTile = isStartingTile;
    }

    public Tile(Chunk[] chunks, boolean isStartingTile) {
        if (chunks.length != ChunkOffset.values().length) {
            throw new IllegalArgumentException("chunks");
        }

        this.chunks = chunks;
        this.isStartingTile = isStartingTile;
    }

    public boolean isStartingTile() {
        return isStartingTile;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Chunk getChunk(ChunkOffset offset) {
        return chunks[offset.value];
    }

    public void setChunk(ChunkOffset offset, Chunk chunk) {
        chunks[offset.value] = chunk;
    }
}
