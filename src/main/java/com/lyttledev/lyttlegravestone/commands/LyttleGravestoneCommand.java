package com.lyttledev.lyttlegravestone.commands;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class LyttleGravestoneCommand {
    private static LyttleGravestone plugin;

    public static void createCommand(LyttleGravestone lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        // Define the different nodes
        LiteralArgumentBuilder<CommandSourceStack> top = Commands.literal("lyttlegravestone")
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("lyttlegravestone.lyttlegravestone.reload"))
                        .executes(LyttleGravestoneCommand::reloadNode));

        // Defines root node functions
        top.requires(source -> source.getSender().hasPermission("lyttlegravestone.lyttlegravestone"));
        top.executes(LyttleGravestoneCommand::rootNode);

        // Finish the command
        commands.register(
                top.build(),
                "Admin command for the LyttleGravestone plugin"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Component version = Component.text("Plugin version: " + plugin.getDescription().getVersion());
        plugin.message.sendMessageRaw(sender, version);
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadNode(CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        plugin.config.reload();
        plugin.message.sendMessageRaw(sender, Component.text("The config has been reloaded"));
        return Command.SINGLE_SUCCESS;
    }
}
