package me.deeka.deekaVelocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class HubCommand implements SimpleCommand {
    private final ProxyServer server;

    public HubCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command is only for players.", NamedTextColor.RED));
            return;
        }
        Player player = (Player) source;
        Optional<RegisteredServer> hub = server.getServer("hub").or(() -> server.getServer("lobby"));
        if (!hub.isPresent()) {
            source.sendMessage(Component.text("Hub server not found.", NamedTextColor.RED));
            return;
        }
        if (player.getCurrentServer().get().getServerInfo().getName().equals(hub.get().getServerInfo().getName())) {
            source.sendMessage(Component.text("You are already connected to the hub server.", NamedTextColor.YELLOW));
            return;
        }
        player.createConnectionRequest(hub.get()).connect();
        source.sendMessage(Component.text("Teleporting to the hub server.", NamedTextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}