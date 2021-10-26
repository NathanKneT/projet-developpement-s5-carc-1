package logic.tile;

import logic.config.GameConfig;
import logic.config.TileConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TileStackTest {
    private static final String config0 = "{\"MIN_PLAYERS\":2,\"MAX_PLAYERS\":5,\"PLAYER_DECK_CAPACITY\":25,\"TILES\":{\"START\": { \"DECK_COUNT\": 5 }, \"ROAD\": { \"DECK_COUNT\": 5 }, \"RIVER\": { \"DECK_COUNT\": 5 }, \"TOWN_CHUNK\": { \"DECK_COUNT\": 5 }}}";
    private static final String config1 = "{\"MIN_PLAYERS\":-5,\"MAX_PLAYERS\":5,\"PLAYER_DECK_CAPACITY\":25,\"TILES\":{\"START\": { \"DECK_COUNT\": 5 }, \"ROAD\": { \"DECK_COUNT\": 5 }, \"RIVER\": { \"DECK_COUNT\": 5 }, \"TOWN_CHUNK\": { \"DECK_COUNT\": 5 }}}";

    @Test
    void testPick() {
        TileStack tileStack = new TileStack();
        TileData tileData = new TileData(TileType.ROAD);

        tileStack.fill(new ArrayList<>() {{
            add(tileData);
        }});

        assertEquals(1, tileStack.getNumTiles());
        assertEquals(tileData, tileStack.remove());
    }

    @Test
    void testFill() {
        GameConfig config = new GameConfig() {{
            TILES = new HashMap<>() {{
                put(TileType.ROAD, new TileConfig() {{
                    DECK_COUNT = 1000;
                }});
                put(TileType.ABBEY, new TileConfig() {{
                    DECK_COUNT = 1000;
                }});
            }};
        }};

        TileStack stack = new TileStack();
        stack.fill(config);

        HashMap<TileType, Integer> tileCountByType = new HashMap<>();

        while (stack.getNumTiles() >= 1) {
            TileType tileType = stack.remove().getType();

            if (tileCountByType.containsKey(tileType)) {
                tileCountByType.replace(tileType, tileCountByType.get(tileType) + 1);
            } else {
                tileCountByType.put(tileType, 1);
            }
        }

        for (Map.Entry<TileType, TileConfig> e : config.TILES.entrySet()) {
            assertEquals(e.getValue().DECK_COUNT, tileCountByType.getOrDefault(e.getKey(), 0));
        }
    }

    @Test
    void testShuffle() { // If the shuffle works properly
        GameConfig config = new GameConfig() {{
            TILES = new HashMap<>() {{
                put(TileType.ROAD, new TileConfig() {{
                    DECK_COUNT = 1000;
                }});
                put(TileType.ABBEY, new TileConfig() {{
                    DECK_COUNT = 1000;
                }});
            }};
        }};

        TileStack stack = new TileStack();

        stack.fill(config);

        ArrayList<TileData> originalTilesPicked = new ArrayList<>();

        while (stack.getNumTiles() >= 1) {
            originalTilesPicked.add(stack.remove());
        }

        stack.fill(originalTilesPicked);
        stack.shuffle();

        ArrayList<TileData> shuffledTilesPicked = new ArrayList<>();

        while (stack.getNumTiles() >= 1) {
            shuffledTilesPicked.add(stack.remove());
        }

        assertEquals(shuffledTilesPicked.size(),originalTilesPicked.size());

        int matchCount = 0;

        for (int i = 0; i < shuffledTilesPicked.size() ; i++) {
            if(shuffledTilesPicked.get(i) == originalTilesPicked.get(i)) {
                matchCount++;
            }
        }

        double matchPercentage = 100.0 * matchCount / originalTilesPicked.size();

        assertTrue(matchPercentage < 10);
    }

    @Test
    void testIsFirstTileIsStartTile() { // If the first tile is the starting tile
        TileStack stack = new TileStack();
        stack.fill(new ArrayList<>() {{
            add(new TileData(TileType.ROAD));
            add(new TileData(TileType.ROAD));
            add(new TileData(TileType.START));
            add(new TileData(TileType.ROAD));
            add(new TileData(TileType.ROAD));
        }});
        stack.shuffle();

        assertEquals(stack.remove().getType(), TileType.START);
    }

    @Test
    void testInitConfig() { // If the initialization for the config works properly
        GameConfig config = new GameConfig() {{
            TILES = new HashMap<>() {{
               put(TileType.ROAD, new TileConfig() {{
                   DECK_COUNT = 1000;
               }});
               put(TileType.ABBEY, new TileConfig() {{
                   DECK_COUNT = 1000;
               }});
            }};
        }};

        TileStack stack = new TileStack();
        stack.fill(config);

        HashMap<TileType, Integer> tileCountByType = new HashMap<>();

        while (stack.getNumTiles() >= 1) {
            TileType tileType = stack.remove().getType();

            if (tileCountByType.containsKey(tileType)) {
                tileCountByType.replace(tileType, tileCountByType.get(tileType) + 1);
            } else {
                tileCountByType.put(tileType, 1);
            }
        }

        for (Map.Entry<TileType, TileConfig> e : config.TILES.entrySet()) {
            assertEquals(e.getValue().DECK_COUNT, tileCountByType.getOrDefault(e.getKey(), 0));
        }
    }
}
