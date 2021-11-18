package logic.command;

import logic.Game;
import logic.tile.Tile;
import logic.tile.TileStack;
import stream.ByteInputStream;
import stream.ByteOutputStream;

import java.util.ArrayList;

/**
 * Command to inform the remote client about the current turn data.
 * As the stack is not serialized to avoid cheating, the client musts insert the next tile on the stack before starting the next turn.
 */
public class MasterTurnStartedCommand implements ICommand {
    private int tileConfigIndex;

    public MasterTurnStartedCommand() {
    }

    public MasterTurnStartedCommand(Tile tileToDraw, Game game) {
        this.tileConfigIndex = game.getConfig().tiles.indexOf(tileToDraw.getConfig());
    }

    /**
     * Returns the command type.
     * @return the command type
     */
    @Override
    public CommandType getType() {
        return CommandType.MASTER_TURN_DATA;
    }

    /**
     * Encodes the command attributes to the output stream.
     * @param stream the output stream
     */
    @Override
    public void encode(ByteOutputStream stream) {
        stream.writeInt(tileConfigIndex);
    }

    /**
     * Decodes the command attributes from the input stream.
     * @param stream the input stream
     */
    @Override
    public void decode(ByteInputStream stream) {
        tileConfigIndex = stream.readInt();
    }

    /**
     * Checks if the command is valid and can be executed.
     * @return true if the command is valid
     */
    @Override
    public boolean canBeExecuted(Game game) {
        if (tileConfigIndex < 0 || tileConfigIndex >= game.getConfig().tiles.size()) {
            game.getCommandExecutor().getListener().onCommandFailed(this, "Tile config index is out of ranges.");
            return false;
        }

        if (game.isMaster()) {
            game.getCommandExecutor().getListener().onCommandFailed(this, "Only non-master game version can execute this command!");
            return false;
        }

        return true;
    }

    /**
     * Executes the command.
     * @param game the game context
     */
    @Override
    public void execute(Game game) {
        Tile tile = game.getConfig().tiles.get(tileConfigIndex).createTile();
        TileStack stack = game.getStack();

        stack.clear();
        stack.fill(new ArrayList<>() {{
            add(tile);
        }});

        if (!game.getBoard().hasFreePlaceForTile(tile)) {
            throw new IllegalStateException("The tile is not placeable on the board!");
        }

        if (!game.getTurn().playTurn()) {
            throw new IllegalStateException("Turn is not playable!");
        }
    }
}