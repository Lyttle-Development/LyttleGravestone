package com.lyttledev.lyttlegravestone.commands;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.List;

public class LyttleGravestoneCommand implements Command<CommandSourceStack> {
    private static LyttleGravestone plugin;

    public static void register(LyttleGravestone pl, Commands commands) {
        plugin = pl;

        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
            Commands.literal("lyttlegravestone")
                .requires(src -> {
                    CommandSender sender = src.getSender();
                    return sender.hasPermission("lyttlegravestone.lyttlegravestone")
                        || sender.hasPermission("mc.admin");
                })
                // /lyttlegravestone reload
                .then(Commands.literal("reload")
                    .executes(new LyttleGravestoneCommand())
                );

        commands.register(
            commandBuilder.build(),
            "Lyttle Gravestone command",
            List.of("lgv", "lg")
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CommandSender sender = source.getSender();

        // Execute reload subcommand
        plugin.config.reload();
        plugin.message.sendMessageRaw(sender, "The config has been reloaded");

        return Command.SINGLE_SUCCESS;
    }
}
