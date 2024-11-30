package me.deeka.deekaVelocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Discord implements SimpleCommand {
    private static final String INVITE = "https://discord.gg/ac6rtT6B";

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        Component message = Component.text("[", NamedTextColor.DARK_AQUA)
                .append(Component.text("Discord", NamedTextColor.AQUA))
                .append(Component.text("] ", NamedTextColor.DARK_AQUA))
                .append(Component.text(INVITE, NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.openUrl(INVITE)))
                .hoverEvent(Component.text("Join our community on Discord!", TextColor.color(0x00FFAA)));

        source.sendMessage(message);
    }
}
