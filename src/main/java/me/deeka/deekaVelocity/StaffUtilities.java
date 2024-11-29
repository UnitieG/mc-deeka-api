package me.deeka.deekaVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class StaffUtilities {

    private final ProxyServer proxyServer;

    public StaffUtilities(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        // Check if the joining player has the staff permission
        if (!player.hasPermission("deeka.staffmessage")) {
            return; // Do nothing if the player isn't staff
        }

        // Get the current server the player has joined
        String currentServer = event.getServer().getServerInfo().getName();

        // Get the previous server the player was connected to
        String previousServer = event.getPreviousServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("none"); // If no previous server, set as "none"

        // Build the message based on the condition
        String message;
        if (!previousServer.equals("none") && !previousServer.equals(currentServer)) {
            message = String.format("&3[&bSM&3] &b%s &3has switched from &b%s &3to &b%s",
                    player.getUsername(), previousServer, currentServer);
        } else {
            message = String.format("&3[&bSM&3] &b%s &3has joined the &b%s",
                    player.getUsername(), currentServer);
        }

        // Send the message to other staff members
        sendToStaff(message);
    }
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        // Check if the player has the staff permission
        if (!player.hasPermission("deeka.staffmessage")) {
            return; // Do nothing if the player isn't staff
        }

        String previousServer = event.getPlayer().getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("unknown");

        String message = String.format("&3[&bSM&3] &b%s &3disconnect from &b%s",
                player.getUsername(), previousServer);

        // Send the message to other staff members
        sendToStaff(message);
    }

    private void sendToStaff(String message) {
        // Convert the message with & color codes into an Adventure Component
        Component staffMessage = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(message);

        // Send the message to players with the staff permission
        proxyServer.getAllPlayers().stream()
                .filter(player -> player.hasPermission("deeka.staffmessage")) // Only players with the staff permission
                .forEach(staff -> staff.sendMessage(staffMessage));
    }
}
