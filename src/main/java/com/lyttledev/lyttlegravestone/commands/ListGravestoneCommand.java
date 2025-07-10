package com.lyttledev.lyttlegravestone.commands;


import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.types.GravestoneListInventory;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListGravestoneCommand implements Command<CommandSourceStack> {
    private static LyttleGravestone plugin;

    public static void register(LyttleGravestone pl, Commands commands) {
        plugin = pl;
        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
                Commands.literal("listgravestones")
                        .requires(src -> {
                            CommandSender sender = src.getSender();
                            return sender.hasPermission("lyttlegravestone.gravestonelist");
                        }).executes(new ListGravestoneCommand());

        commands.register(
                commandBuilder.build(),
                "List the players gravestones",
                List.of("lg")
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

            GravestoneListInventory gravestoneListInventory = new GravestoneListInventory(plugin, player.getName(), locations);
            Inventory inventory = gravestoneListInventory.getInventory();
            player.openInventory(inventory);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Command.SINGLE_SUCCESS;
    }

}
