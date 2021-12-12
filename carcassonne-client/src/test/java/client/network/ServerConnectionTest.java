package client.network;

import client.message.MessageDispatcher;
import network.Packet;
import network.ResizableByteBuffer;
import network.message.IMessage;
import network.message.connection.ClientHelloMessage;
import network.message.connection.ServerHelloMessage;
import network.message.game.GameResultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reflection.ReflectionUtils;
import stream.ByteOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ServerConnectionTest {
    private ServerConnection serverConnection;
    private ByteBuffer receiveBuffer;

    private boolean isClosedCalled;
    private int receivedMessageCount;

    @BeforeEach
    void setup() throws Exception {
        serverConnection = new ServerConnection() {
            @Override
            public void close() {
                isClosedCalled = true;
            }
        };

        ReflectionUtils.setField(serverConnection, "messageDispatcher", new MessageDispatcher() {
            @Override
            public void handle(IMessage message) {
                receivedMessageCount++;
            }
        });

        receiveBuffer = (ByteBuffer) ReflectionUtils.getField(serverConnection, "receiveBuffer");
    }

    @Test
    void testMessageHandling() {
        receiveBuffer.put(getPacketBytes(Packet.create(new ClientHelloMessage())));
        receiveBuffer.put(getPacketBytes(Packet.create(new ServerHelloMessage())));

        serverConnection.onReceive(receiveBuffer.position());

        assertEquals(2, receivedMessageCount);
    }

    @Test
    void testCloseOnBadChecksum() {
        // Put wrong checksum to check if the connection is closed
        receiveBuffer.put(getPacketBytes(Packet.create(new ClientHelloMessage())));
        receiveBuffer.array()[8] = (byte) 0xFF;
        receiveBuffer.array()[9] = (byte) 0xFF;
        receiveBuffer.array()[10] = (byte) 0xFF;
        receiveBuffer.array()[11] = (byte) 0xFF;

        serverConnection.onReceive(receiveBuffer.position());

        assertTrue(isClosedCalled);
    }

    @Test
    void testCloseOnBadHeader() {
        // Put wrong data to check if the connection is closed
        receiveBuffer.put(getPacketBytes(Packet.create(new ClientHelloMessage())));
        receiveBuffer.array()[0] = (byte) 0xFF;
        receiveBuffer.array()[1] = (byte) 0xFF;
        receiveBuffer.array()[2] = (byte) 0xFF;
        receiveBuffer.array()[3] = (byte) 0xFF;

        serverConnection.onReceive(receiveBuffer.position());

        assertTrue(isClosedCalled);
    }

    @Test
    void testCloseOnTooBigMessage() {
        // Put wrong data to check if the connection is closed
        byte[] data = getPacketBytes(Packet.create(new GameResultMessage(new byte[100000])));

        // No need to write the whole message, just write the header
        receiveBuffer.put(data, 0, 20);
        serverConnection.onReceive(20);

        assertTrue(isClosedCalled);
    }

    @Test
    void testDontHandleWhenMessageNotCompletelyReceived() {
        // Put wrong data to check if the connection is closed
        receiveBuffer.put(getPacketBytes(Packet.create(new ClientHelloMessage())));

        serverConnection.onReceive(receiveBuffer.position() - 1);

        assertEquals(0, receivedMessageCount);
        assertFalse(isClosedCalled);
    }

    @Test
    void testHandleFirstMessageWhenSecondNotCompletelyReceived() {
        // Put wrong data to check if the connection is closed
        receiveBuffer.put(getPacketBytes(Packet.create(new ClientHelloMessage())));
        receiveBuffer.put(getPacketBytes(Packet.create(new ServerHelloMessage())));

        serverConnection.onReceive(receiveBuffer.position() - 1);

        assertEquals(1, receivedMessageCount);
        assertFalse(isClosedCalled);
    }

    private static byte[] getPacketBytes(Packet packet) {
        ByteOutputStream stream = new ByteOutputStream(64);
        packet.encode(stream);
        return stream.toByteArray();
    }
}