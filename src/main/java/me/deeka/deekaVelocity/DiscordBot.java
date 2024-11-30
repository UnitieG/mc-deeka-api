package me.deeka.deekaVelocity;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class DiscordBot extends ListenerAdapter {

    private static final String BOT_TOKEN = "";
    private final Logger logger;
    private Spark spark;

    public DiscordBot(Logger logger, Spark spark) {
        this.spark = spark;
        this.logger = logger;
    }

    public void start() {
        try {
            spark = SparkProvider.get();
            JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(this)
                    .build();
            logger.info("Discord Bot has started!");
        } catch (Exception e) {
            logger.error("Failed to start the bot.", e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.isFromType(ChannelType.TEXT)) {
            TextChannel channel = event.getChannel().asTextChannel();

            if (event.getMessage().getContentRaw().equalsIgnoreCase("!status")) {
                if (spark == null) {
                    channel.sendMessage("Spark is not available!").queue();
                    return;
                }

                // Get the CPU Usage statistic
                DoubleStatistic<StatisticWindow.CpuUsage> cpuUsage = spark.cpuSystem();

                // Retrieve the average usage percent in the last minute
                double usageLastMin = cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1);
                //ram
                long maxMemory = Runtime.getRuntime().maxMemory();
                // Build and send the embed
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Velocity Server Status")
                        .addField("CPU Load:", String.format("%.2f%%", usageLastMin), true)
                        .addField("Ram Usage:", String.format("%.2f%%", (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double) maxMemory), true)
                        .addBlankField(true)
                        .addField("Logging in as:", System.getProperty("user.name"), true)
                        .addField("OS:", System.getProperty("os.name"), true)
                        .addField("Arch:", System.getProperty("os.arch"), true)
                        .addField("Kernal:", System.getProperty("os.version"), true)
                        .addField("Java Version:", ManagementFactory.getRuntimeMXBean().getSpecVersion(), true)
                        .setColor(Color.GREEN)
                        .setFooter("Server Monitor");

                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}