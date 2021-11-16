package logic.tile;

import logic.config.GameConfig;
import logic.config.excel.TileExcelConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class TileStack {
    private final LinkedList<Tile> tiles;

    public TileStack() {
        this.tiles = new LinkedList<>();
    }

    public void fill(ArrayList<Tile> tiles) {
        this.tiles.addAll(tiles);
    }

    public void fill(GameConfig config) {
        for (TileExcelConfig tileConfig : config.tiles) {
            for (int i = 0; i < tileConfig.count; i++) {
                tiles.add(tileConfig.createTile());
            }
        }
    }

    public int getNumTiles() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return getNumTiles() == 0;
    }

    public Tile remove() {
        return tiles.remove();
    }

    public Tile peek() {
        return tiles.peek();
    }

    public void shuffle() {
        Collections.shuffle(tiles);
        Tile startingTile = tiles.stream().filter(f -> f.hasFlags(TileFlags.STARTING)).findAny().orElse(null);

        if (startingTile != null) {
            tiles.remove(startingTile);
            tiles.addFirst(startingTile);
        }
    }
}