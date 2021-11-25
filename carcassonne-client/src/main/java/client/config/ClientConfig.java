package client.config;

import excel.ExcelNode;
import logic.config.GameConfig;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class ClientConfig {
    private final String serverHost;
    private final int serverPort;
    private final LoggerConfig loggerConfig;
    private final MatchConfig matchConfig;

    private ClientConfig(ExcelNode node) {
        ExcelNode serverNode = node.getChild("Server");

        if (serverNode == null) {
            throw new IllegalArgumentException("Server node not found");
        }

        ExcelNode loggerNode = node.getChild("Logger");

        if (loggerNode == null) {
            throw new IllegalArgumentException("Logger node not found");
        }

        ExcelNode matchNode = node.getChild("Match");

        if (matchNode == null) {
            throw new IllegalArgumentException("Match node not found");
        }

        this.serverHost = serverNode.getRow("Host").getValue("Value");
        this.serverPort = Integer.parseInt(serverNode.getRow("Port").getValue("Value"));

        this.loggerConfig = new LoggerConfig(loggerNode);
        this.matchConfig = new MatchConfig(matchNode);
    }

    /**
     * Returns the hostname of the server.
     * @return the hostname of the server
     */
    public String getServerHost() {
        return serverHost;
    }

    /**
     * Returns the port of the server.
     * @return the port of the server
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Returns the logger configuration.
     * @return the logger configuration
     */
    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    /**
     * Returns the match configuration.
     * @return the match configuration
     */
    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public static ClientConfig loadFromResources() {
        try {
            File file = new File("config.txt");
            System.out.println("0DEBUG EXIST : " + file.exists());
            System.out.println("1DEBUG FILE : " + new File(file.getPath()));
            System.out.println("2DEBUG FILE : " + new File(file.getAbsolutePath()));
            System.out.println("3DEBUG FILE : " + file.getPath());
            System.out.println("4DEBUG FILE : " + file.getAbsolutePath());
            System.out.println("5DEBUG FILE : " + file.toPath().toString());
            String path = Path.of(ClientConfig.class.getResource(".").toURI()).toString();
            ExcelNode rootNode = ExcelNode.load(Path.of(path, "config.txt"));
            return new ClientConfig(rootNode);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
