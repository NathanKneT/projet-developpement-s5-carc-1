package network.message;

import network.message.connection.ClientHelloMessage;
import network.message.connection.ServerHelloMessage;
import network.message.game.GameCommandMessage;
import network.message.game.GameCommandRequestMessage;
import network.message.game.GameDataMessage;
import network.message.game.GameResultMessage;
import network.message.matchmaking.*;

/**
 * MessageType is an enumeration of all the message types.
 * MessageType with a value <= to 199 are reserved for the client.
 * MessageType with a value >= to 200 are reserved for the server.
 */
public enum MessageType {
    CLIENT_HELLO(100, ClientHelloMessage.class),
    SERVER_HELLO(200, ServerHelloMessage.class),

    JOIN_MATCHMAKING(110, JoinMatchmakingMessage.class),
    MATCHMAKING_DATA(210, MatchmakingDataMessage.class),
    LEAVE_MATCHMAKING(111, LeaveMatchmakingMessage.class),
    MATCHMAKING_LEFT(211, MatchmakingLeftMessage.class),
    MATCHMAKING_FAILED(212, MatchmakingFailedMessage.class),

    GAME_DATA(220, GameDataMessage.class),
    GAME_COMMAND_REQUEST(121, GameCommandRequestMessage.class),
    GAME_COMMAND(221, GameCommandMessage.class),
    GAME_RESULT(222, GameResultMessage.class);

    /**
     * The message type value.
     */
    private final int type;

    /**
     * The message class.
     */
    private final Class<? extends Message> messageClass;

    MessageType(int id, Class<? extends Message> messageClass) {
        this.type = id;
        this.messageClass = messageClass;
    }

    /**
     * Gets the message type value.
     * @return the message type value.
     */
    public int getValue() {
        return type;
    }

    /**
     * Returns the message class.
     * @return
     */
    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }

    public static MessageType getByType(int type) {
        for (MessageType messageId : MessageType.values()) {
            if (messageId.getValue() == type) {
                return messageId;
            }
        }

        return null;
    }
}
