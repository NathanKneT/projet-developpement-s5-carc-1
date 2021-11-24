package client.utils;

import logic.Game;
import logic.board.GameBoard;
import logic.math.Polygon;
import logic.math.Vector2;
import logic.meeple.Meeple;
import logic.tile.TileEdge;
import logic.tile.TileFlags;
import logic.tile.chunk.Chunk;
import logic.tile.chunk.ChunkArea;
import logic.tile.chunk.ChunkId;
import logic.tile.Tile;
import logic.tile.chunk.ChunkType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Class for drawing the game.
 */
public class GameDrawUtils implements ChunkPositionConstant {
    private static final int tileWidth = 160;
    private static final int tileHeight = 160;
    private static final int meepleWidth = 27;
    private static final int meepleHeight = 27;

    private static ImageDatabase tileDatabase;
    private static ImageDatabase meepleDatabase;

    private static Random rand = new Random();

    private static final HashMap<ChunkId, Vector2> meepleOffset = new HashMap<>() {{
        put(ChunkId.NORTH_LEFT, new Vector2(27, 0));
        put(ChunkId.NORTH_MIDDLE, new Vector2(67, 18));
        put(ChunkId.NORTH_RIGHT, new Vector2(106, 0));
        put(ChunkId.SOUTH_LEFT, new Vector2(27, 133));
        put(ChunkId.SOUTH_MIDDLE, new Vector2(67, 124));
        put(ChunkId.SOUTH_RIGHT, new Vector2(106, 133));
        put(ChunkId.WEST_TOP, new Vector2(0, 27));
        put(ChunkId.WEST_MIDDLE, new Vector2(18, 71));
        put(ChunkId.WEST_BOTTOM, new Vector2(0, 106));
        put(ChunkId.EAST_TOP, new Vector2(133, 27));
        put(ChunkId.EAST_MIDDLE, new Vector2(124, 71));
        put(ChunkId.EAST_BOTTOM, new Vector2(133, 106));
        put(ChunkId.CENTER_MIDDLE, new Vector2(67, 67));
    }};
    private static final HashMap<ChunkId, Polygon> chunksGeo = new HashMap<>() {{
        put(ChunkId.NORTH_LEFT, new Polygon(A, B, F)); // ABF
        put(ChunkId.NORTH_MIDDLE, new Polygon(B, C, G, F)); // BCGF
        put(ChunkId.NORTH_RIGHT, new Polygon(C, D, G)); // CDG
        put(ChunkId.SOUTH_LEFT, new Polygon(M, J, N)); // MJN
        put(ChunkId.SOUTH_MIDDLE, new Polygon(J, K, O, N)); // JKON
        put(ChunkId.SOUTH_RIGHT, new Polygon(K, P, O)); // KPO
        put(ChunkId.WEST_TOP, new Polygon(A, E, F)); // AEF
        put(ChunkId.WEST_MIDDLE, new Polygon(E, F, J, I)); // EFJI
        put(ChunkId.WEST_BOTTOM, new Polygon(I, J, M)); // IJM
        put(ChunkId.EAST_TOP, new Polygon(G, H, D)); // GHD
        put(ChunkId.EAST_MIDDLE, new Polygon(G, H, L, K)); // GHLK
        put(ChunkId.EAST_BOTTOM, new Polygon(K, L, P)); // KLP
        put(ChunkId.CENTER_MIDDLE, new Polygon(F, G, K, J)); // FGKJ
    }};
    private static final HashMap<ChunkType, Color> chunksColor = new HashMap<>() {{
        put(ChunkType.ROAD, Color.gray);
        put(ChunkType.ROAD_END, Color.darkGray);
        put(ChunkType.FIELD, Color.green);
        put(ChunkType.TOWN, Color.ORANGE);
        put(ChunkType.ABBEY, Color.CYAN);
        put(ChunkType.RIVER, Color.blue);
    }};

    /**
     * Loads the images for the rendering and stores them in the image database.
     */
    private static void loadImageDatabaseIfNeeded() {
        if (tileDatabase == null) {
            tileDatabase = new ImageDatabase(tileWidth, tileHeight);

            for (File file : new File("models/tiles").listFiles()) {
                if (file.isFile()) {
                    try {
                        tileDatabase.cache(file.getName().replace(".jpg", ""), ImageIO.read(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (meepleDatabase == null) {
            meepleDatabase = new ImageDatabase(meepleWidth, meepleHeight);

            for (File file : new File("models/meeples").listFiles()) {
                if (file.isFile()) {
                    try {
                        meepleDatabase.cache(file.getName().replace(".png", ""), ImageIO.read(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Calculates the board bounds.
     *
     * @param board The board to calculate the bounds for.
     * @return The bounds of the board.
     */
    public static Bounds calculateBoardBounds(GameBoard board) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;

        for (Tile tile : board.getTiles()) {
            int x = tile.getPosition().getX();
            int y = tile.getPosition().getY();

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

    /**
     * Gets the position where the tile should be drawn.
     *
     * @param tile The tile to get the position for.
     * @return The position where the tile should be drawn.
     */
    private static Vector2 getTilePosition(Tile tile) {
        return new Vector2(tile.getPosition().getX() * tileWidth, tile.getPosition().getY() * tileHeight);
    }

    /**
     * Gets the position where the meeple should be drawn.
     *
     * @param tilePosition The tile position to add the meeple position.
     * @param chunkId      The chunk id to get the position for.
     * @return The position where the meeple should be drawn.
     */
    private static Vector2 getMeeplePosition(Vector2 tilePosition, ChunkId chunkId) {
        return meepleOffset.get(chunkId).add(tilePosition);
    }

    /**
     * Gets the sprite model to use for the specified player.
     *
     * @param index The index of the player in the game.
     * @return The sprite model to use for the specified player's meeple.
     */
    private static String getOwnMeepleSpriteModel(int index) {
        return Integer.toString(index);
    }

    /**
     * Draw a chunk
     *
     * @param g            The graphics to draw into
     * @param tilePosition The position of the tile associated with the chunk
     * @param chunkType    The type of the chunk to draw
     * @param chunkId      The id of the chunk to draw
     */
    private static void drawChunk(Graphics g, Vector2 tilePosition, ChunkType chunkType, ChunkId chunkId) {
        g.setColor(chunksColor.get(chunkType));
        Polygon polygon = chunksGeo.get(chunkId);
        g.fillPolygon(polygon.getXs(tilePosition.getX()), polygon.getYs(tilePosition.getY()), polygon.getVectorCount());
    }

    /**
     * Draw zone with a unique color
     *
     * @param g The graphics to draw into
     * @param tilePosition The position of the tile associated with the chunk
     * @param chunk The chunk of the chunk to draw
     * @param colorZone The colors of all zone
     */
    private static void drawZone(Graphics g, Vector2 tilePosition, Chunk chunk, HashMap<ChunkArea, Color> colorZone) {
        ChunkArea chunkArea = chunk.getArea();
        if (!colorZone.containsKey(chunkArea))
            colorZone.put(chunkArea, generateColor(1));

        g.setColor(colorZone.get(chunkArea));
        Polygon polygon = chunksGeo.get(chunk.getCurrentId());
        g.fillPolygon(polygon.getXs(tilePosition.getX()), polygon.getYs(tilePosition.getY()), polygon.getVectorCount());
    }

    /**
     * Generate a random color
     *
     * @param opacity The opacity wanted
     * @return a random color
     */
    public static Color generateColor(float opacity) {
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        return new Color(r, g, b, opacity);
    }

    /**
     * Draw tile border
     *
     * @param g            The graphics to draw into
     * @param tilePosition The position of the tile associated with the chunk
     */
    private static void drawTileBorder(Graphics g, Vector2 tilePosition) {
        g.setColor(Color.black);
        g.drawLine(tilePosition.getX(), tilePosition.getY(), tilePosition.getX() + tileWidth, tilePosition.getY());
        g.drawLine(tilePosition.getX(), tilePosition.getY() + tileHeight - 1, tilePosition.getX() + tileWidth, tilePosition.getY() + tileHeight - 1);
        g.drawLine(tilePosition.getX(), tilePosition.getY(), tilePosition.getX(), tilePosition.getY() + tileHeight);
        g.drawLine(tilePosition.getX() + tileWidth - 1, tilePosition.getY(), tilePosition.getX() + tileWidth - 1, tilePosition.getY() + tileHeight);
    }

    /**
     * Creates a new image layer representing the specified game instance.
     *
     * @param game The game instance to create the image layer for.
     * @return The image layer representing the specified game instance.
     */
    public static BufferedImage createLayer(Game game) {
        return createLayer(game, calculateBoardBounds(game.getBoard()));
    }

    /**
     * Creates a new image layer representing the specified game instance.
     *
     * @param game        The game instance to create the image layer for.
     * @param boardBounds The bounds of the board.
     * @return The image layer representing the specified game instance.
     */
    public static BufferedImage createLayer(Game game, Bounds boardBounds) {
        Bounds layerBounds = boardBounds.scale(tileWidth, tileHeight).reverseY();
        BufferedImage layer = new BufferedImage(layerBounds.getWidth(), layerBounds.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D layerGraphics = layer.createGraphics();
        render(game, layerGraphics, layerBounds);
        layerGraphics.dispose();

        return layer;
    }

    /**
     * Renders the specified game instance to the specified graphics context.
     *
     * @param game        The game instance to render.
     * @param graphics    The graphics context to render to.
     * @param layerBounds The bounds of the layer to render.
     */
    public static synchronized void render(Game game, Graphics graphics, Bounds layerBounds) {
        loadImageDatabaseIfNeeded();
        HashMap<ChunkArea, Color> colorZone = new HashMap<>();

        graphics.setFont(new Font("Courier New", Font.CENTER_BASELINE | Font.BOLD, 25));
        graphics.setColor(Color.blue);

        for (Tile tile : game.getBoard().getTiles()) {
            Vector2 tileImagePosition = getTilePosition(tile).reverseY().subtract(layerBounds.start);
            BufferedImage tileImage = tileDatabase.get(tile.getConfig().model);

            assert tileImagePosition.getX() >= 0 && tileImagePosition.getY() >= 0;
            assert tileImagePosition.getX() + tileWidth <= layerBounds.getWidth() && tileImagePosition.getY() + tileHeight <= layerBounds.getHeight();

            graphics.drawImage(tileImage, tileImagePosition.getX(), tileImagePosition.getY(), null);
            graphics.drawString(tile.getConfig().model + " " + tile.getPosition().getX() + " " + tile.getPosition().getY(), tileImagePosition.getX() + tileWidth / 4, tileImagePosition.getY() + tileHeight / 2);

            for (ChunkId chunkId : ChunkId.values()) {
                Chunk chunk = tile.getChunk(chunkId);

                // Show chunk
                //drawChunk(graphics, tileImagePosition, chunk.getType(), chunkId);

                // Show zones
                drawZone(graphics, tileImagePosition, chunk, colorZone);

                // Show tile border
                //drawTileBorder(graphics, tileImagePosition);

                if (chunk.hasMeeple()) {
                    Meeple meeple = chunk.getMeeple();
                    Vector2 meepleImagePosition = getMeeplePosition(tileImagePosition, chunkId);
                    BufferedImage meepleImage = meepleDatabase.get(getOwnMeepleSpriteModel(game.getPlayerIndex(meeple.getOwner())));
                    graphics.setColor(Color.blue);
                    graphics.drawImage(meepleImage, meepleImagePosition.getX(), meepleImagePosition.getY(), null);
                    //graphics.drawString(chunkId.name(), tileImagePosition.getX(), tileImagePosition.getY());
                }
            }
        }

        for (int i = 0; i < game.getPlayers().size(); i++) {
            game.getPlayers().get(i);
        }
    }
}
