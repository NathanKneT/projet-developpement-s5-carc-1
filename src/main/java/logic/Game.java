package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.GameDrawUtils;
import logger.Logger;
import logic.board.GameBoard;
import logic.config.GameConfig;
import logic.exception.NotEnoughPlayerException;
import logic.exception.TooManyPlayerException;
import logic.player.PlayerBase;
import logic.tile.TileStack;

public class Game {
    private final GameConfig config;
    private final GameBoard board;
    private final GameTurn turn;
    private final TileStack stack;
    private final ArrayList<PlayerBase> players;

    private boolean started;

    public Game(GameConfig config) {
        this.config = config;
        this.board = new GameBoard();
        this.turn = new GameTurn(this);
        this.stack = new TileStack();
        this.players = new ArrayList<>(config.MAX_PLAYERS);
    }

    public void start() {
        if (getPlayerCount() < config.MIN_PLAYERS) {
            throw new NotEnoughPlayerException(getPlayerCount(), config.MIN_PLAYERS);
        }

        if (started) {
            board.clear();
            turn.reset();
        }

        for (PlayerBase player : players) {
            player.init();
        }

        stack.fill(config);
        stack.shuffle();
        started = true;
    }

    public void update() {
        if (!started) {
            throw new IllegalStateException("Game should be started at this point.");
        }

        turn.playTurn();

        if (isFinished()) {
            onEnd();
        }
    }

    public void updateToEnd() {
        while (!isFinished()) {
            update();
        }
    }

    public void onEnd() {
        Logger.info("[GAME] Game over");
        Logger.info("[STATS] Winner is Player %d !", getWinner().getId());
    }

    public boolean isFinished() {
        for (PlayerBase player : players) {
            if (player.getScore() >= 279) {
                return true;
            }
        }

        return this.stack.getNumTiles() == 0;
    }

    public void addPlayer(PlayerBase player) {
        if (getPlayerCount() >= config.MAX_PLAYERS) {
            throw new TooManyPlayerException();
        }

        players.add(player);
        player.setGame(this);
    }

    public PlayerBase getWinner() {
        if (!isFinished()) {
            throw new IllegalStateException("getWinner() must be called if the game is finished.");
        }

        PlayerBase winner = null;
        int winnerScore = -1;

        for (PlayerBase p : players) {
            if (p.getScore() > winnerScore) {
                winnerScore = p.getScore();
                winner = p;
            }
        }

        return winner;
    }

    public GameConfig getConfig() {
        return config;
    }

    public GameBoard getBoard() {
        return board;
    }

    public TileStack getStack() {
        return stack;
    }

    public PlayerBase getPlayer(int player) {
        return players.get(player);
    }

    public int getPlayerCount() {
        return players.size();
    }
}
