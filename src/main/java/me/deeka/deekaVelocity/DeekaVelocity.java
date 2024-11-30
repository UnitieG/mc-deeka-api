package me.deeka.deekaVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.lucko.spark.api.Spark;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

@Plugin(id = "deeka-velocity", name = "deeka-velocity", version = "1.0-SNAPSHOT", description = "it is a plugin.", authors = {"furrygang"})
public class DeekaVelocity {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    private Spark spark;

    private DiscordBot discordBot;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(2923), 0);
            httpServer.createContext("/", new RootHandler());
            httpServer.createContext("/players", new PlayersHandler());
            httpServer.createContext("/servers/", new ServerHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            logger.info("Web server started on port " + httpServer.getAddress().getPort());

        } catch (Exception e) {
            logger.error("Failed to start web server", e);
        }
        server.getCommandManager().register("hub", new HubCommand(server));
        server.getCommandManager().register("lobby", new HubCommand(server));
        server.getCommandManager().register("discord", new Discord());
        server.getEventManager().register(this, new StaffUtilities(server));

        try {
            discordBot = new DiscordBot(logger, spark); // Pass the spark instance
            discordBot.start();
        } catch (NoClassDefFoundError e) {
            logger.error("DiscordBot failed to start: JDA library not found", e);
        }
    }


    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if (handlePreflight(exchange)) return;

                String response = "Invalid API End Point!";
                sendTextResponse(exchange, 200, response);
            } catch (Exception e) {
                logger.error("Error handling root request", e);
            }
        }
    }

    private class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if (handlePreflight(exchange)) return;

                String response = "{\"online\": " + server.getPlayerCount() + "}";
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                logger.error("Error handling players request", e);
            }
        }
    }

    private class ServerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if (handlePreflight(exchange)) return;

                String path = exchange.getRequestURI().getPath();
                if (path.matches("^/servers/[^/]+/players$")) {
                    String serverName = path.substring(9, path.lastIndexOf('/'));
                    int online = server.getAllServers().stream()
                            .filter(s -> s.getServerInfo().getName().equals(serverName))
                            .findFirst()
                            .map(s -> s.getPlayersConnected().size())
                            .orElse(0);
                    String response = "{\"online\": " + online + "}";
                    sendJsonResponse(exchange, 200, response);
                } else {
                    String response = "Invalid API End Point!";
                    sendTextResponse(exchange, 404, response);
                }
            } catch (Exception e) {
                logger.error("Error handling server request", e);
            }
        }
    }

    private boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1); // No Content
            return true;
        }
        return false;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}