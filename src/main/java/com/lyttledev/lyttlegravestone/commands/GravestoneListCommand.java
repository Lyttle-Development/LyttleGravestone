package com.lyttledev.lyttlegravestone.commands;


import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GravestoneListCommand implements Command<CommandSourceStack> {
    private static LyttleGravestone plugin;

    public static void register(LyttleGravestone pl, Commands commands) {
        plugin = pl;

        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
                Commands.literal("gravestonelist")
                        .requires(src -> {
                            CommandSender sender = src.getSender();
                            return sender.hasPermission("lyttlegravestone.gravestonelist");
                        }).executes(new GravestoneListCommand());

        commands.register(
                commandBuilder.build(),
                "List the players gravestones",
                List.of("gl", "gravestonelist")
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player)) { return 0; }
        Player player = (Player) sender;

        try {
            List<String[]> values = GravestoneDatabase.getGravestones(player);
            
            StringBuilder gravesStones = new StringBuilder();
            gravesStones.append("\n");

            List<String> locations = new ArrayList<>();

            for (String[] gravestone : values) {
                gravesStones.append(gravestone[0]).append("\n");
                gravesStones.append(gravestone[1]).append("\n");

                locations.add(gravestone[1]);

            }

            plugin.message.sendMessageRaw(player, Component.text(gravesStones.toString()));

            plugin.listHandler.openGui(player, locations);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Command.SINGLE_SUCCESS;
    }

}
