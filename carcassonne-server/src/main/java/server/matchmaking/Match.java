package server.matchmaking;

import logic.Game;
import logic.command.ICommand;
import logic.command.MasterNextTurnDataCommand;
import logic.command.PlaceTileDrawnCommand;
import logic.command.SkipMeeplePlacementCommand;
import logic.config.GameConfig;
import logic.player.Player;
import logic.state.GameStateType;
import logic.state.turn.GameTurnPlaceTileState;
import logic.tile.Tile;
import network.message.Message;
import network.message.game.GameCommandMessage;
import network.message.game.GameDataMessage;
import network.message.game.GameResultMessage;
import server.command.SlaveCommandExecutionNotifier;
import server.listener.MatchGameListener;
import server.logger.Logger;
import server.session.ClientSession;
import stream.ByteOutputStream;

/**
 * Represents a game in the matchmaking system.
 */
public class Match {
    private final int id;
    private final ClientSession[] sessions;
    private final Game game;

    public Match(int id, ClientSession[] sessions) {
        this.id = id;
        this.sessions = sessions;
        this.game = new Game(GameConfig.loadFromResources());

        game.getCommandExecutor().setListener(new SlaveCommandExecutionNotifier(this));

        for (ClientSession session : sessions) {
            game.addPlayer(new Player(session.getUserId()));
        }
    }

    /**
     * Destroys the match.
     */
    public void destroy() {
        for (ClientSession session : sessions) {
            if (session != null) {
                session.setMatch(null);
            }
        }
    }

    /**
     * Gets the match id.
     *
     * @return the match id
     */
    public int getId() {
        return id;
    }

    /**
     * Removes the player from the connected sessions.
     *
     * @param session the session to remove
     */
    public void removePlayer(ClientSession session) {
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i] == session) {
                sessions[i] = null;
            }
        }

        if (game.isStarted() && !game.isOver()) {
            autoPlayIfCurrentPlayerIsOffline();
        }
    }

    /**
     * Gets the session by the user id.
     *
     * @param userId
     * @return
     */
    private ClientSession getSessionByUserId(int userId) {
        for (ClientSession session : sessions) {
            if (session != null && session.getUserId() == userId) {
                return session;
            }
        }
        return null;
    }

    /**
     * Sends a message to all connected clients.
     *
     * @param message
     */
    private void sendMessageToConnectedClients(Message message) {
        for (ClientSession session : sessions) {
            if (session != null) {
                session.getConnection().send(message);
            }
        }
    }

    /**
     * Executes a command in the master game and notify the connected clients if successful.
     *
     * @param userId  the executor user id
     * @param command the command to execute
     */
    public void executeCommand(int userId, ICommand command) {
        Player commandExecutor = game.getPlayerById(userId);
        Player turnExecutor = game.getTurnExecutor();

        if (commandExecutor != turnExecutor) {
            Logger.warn("Player %d tried to execute command %s but it is not his turn.", userId, command.getType());
            return;
        }

        if (game.getState().getType() != command.getRequiredState()) {
            Logger.warn("Player %d tried to execute command %s but the game state is %s.", userId, command.getType(), game.getState().getType());
            return;
        }

        if (!command.canBeExecuted(game)) {
            Logger.warn("Player %d tried to execute command %s but it is not executable.", userId, command.getType());
            return;
        }

        Logger.debug("Player %d executed command %s", userId, command.getType());

        game.getCommandExecutor().execute(command);
    }

    /**
     * Notifies the connected clients that the command has been executed.
     *
     * @param command the executed command
     */
    public void notifyCommandExecutionToConnectedClients(ICommand command) {
        sendMessageToConnectedClients(new GameCommandMessage(command));
    }

    /**
     * Auto plays the game if the current player who must play is offline.
     */
    private void autoPlayIfCurrentPlayerIsOffline() {
        Player turnExecutor = game.getTurnExecutor();

        if (getSessionByUserId(turnExecutor.getId()) == null) {
            Logger.info("Player %d left the game. Tile to draw will be placed randomly to continue the game.", turnExecutor.getId());

            GameTurnPlaceTileState placeTileState = (GameTurnPlaceTileState) game.getState();

            executeCommand(turnExecutor.getId(), new PlaceTileDrawnCommand(game.getBoard().findFreePlacesForTile(placeTileState.getTileDrawn()).get(0)));
            executeCommand(turnExecutor.getId(), new SkipMeeplePlacementCommand());

            if (game.getState().getType() == GameStateType.TURN_MOVE_DRAGON) {
                // TODO
            }
        }
    }

    public void startGame() {
        game.start();
        game.setListener(new MatchGameListener(this));

        ByteOutputStream stream = new ByteOutputStream(1000);
        game.encode(stream, false);
        sendMessageToConnectedClients(new GameDataMessage(stream.toByteArray()));
    }

    public void onGameOver() {
        ByteOutputStream stream = new ByteOutputStream(1000);
        game.encode(stream, true);
        destroy();
        sendMessageToConnectedClients(new GameResultMessage(stream.toByteArray()));
    }

    public void onGameTurnStarted(Tile tileDrawn) {
        notifyCommandExecutionToConnectedClients(new MasterNextTurnDataCommand(tileDrawn, game));
    }
}
