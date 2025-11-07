package com.lyttledev.lyttlegravestone.commands;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.types.RetrieveState;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.utils.convertion.ItemSerializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RetrieveGraveStoneCommand {
    // Import Economy from Vault
    private static Economy economy;
    private static LyttleGravestone plugin;

    public static void createCommand(LyttleGravestone lyttlePlugin, Commands commands) {
        economy = lyttlePlugin.getEconomy();
        plugin = lyttlePlugin;

        LiteralArgumentBuilder<CommandSourceStack> retrieveGravestone =  Commands.literal("retrieve-gravestone")
            .then(Commands.argument("world", StringArgumentType.string())
                .suggests((context, builder) -> {
                  builder.suggest("world");
                  builder.suggest("world_nether");
                  builder.suggest("world_the_end");
                  return builder.buildFuture();
                })
            .then(Commands.argument("coordinates", ArgumentTypes.blockPosition())
                .executes(RetrieveGraveStoneCommand::gravestoneNode)
                    .then(Commands.literal("confirm")
                    .then(Commands.argument("price", IntegerArgumentType.integer())
                        .executes(RetrieveGraveStoneCommand::confirmedGravestoneNode)))));


        commands.register(
                retrieveGravestone.build(),
                "Retrieve a gravestone",
                List.of("rg")
        );
    }

    // LOGIC BEFORE CONFIRM
    private static int gravestoneNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean usesVault = usingVault();
        if (usesVault) { deliveryLogic(RetrieveState.notConfirmed, context); }
        else { deliveryLogic(RetrieveState.noVault, context); }
        return Command.SINGLE_SUCCESS;
    }

    // LOGIC AFTER CONFIRM
    private static int confirmedGravestoneNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean usesVault = usingVault();
        if (usesVault) { deliveryLogic(RetrieveState.confirmed, context); }
        else { deliveryLogic(RetrieveState.noVault, context); }
        return Command.SINGLE_SUCCESS;
    }

    // Main logic
    private static void deliveryLogic(RetrieveState state, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        // Get config options
        Integer configBlocks = (Integer) plugin.config.general.get("retrieve_command_blocks");
        Integer configPrice = (Integer) plugin.config.general.get("retrieve_command_price");
        Integer configWorldPrice = (Integer) plugin.config.general.get("retrieve_command_price__other_world");

        // Check for user provided config values
        if (configBlocks == null) {
            throw new RuntimeException("PLUGIN GENERAL CONFIG retrieve_command_blocks IS SET INCORRECTLY!");
        }
        if (configPrice == null) {
            throw new RuntimeException("PLUGIN GENERAL CONFIG retrieve_command_price IS SET INCORRECTLY!");
        }
        if (configWorldPrice == null) {
            throw new RuntimeException("PLUGIN GENERAL CONFIG retrieve_command_price__other_world IS SET INCORRECTLY!");
        }

        // Get basic variables
        CommandSourceStack source = context.getSource();
        Entity entity = source.getExecutor();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        UUID uuid = player.getUniqueId();
        Location location = entity.getLocation();

        // Check for an in progress delivery / add a delivery
        if (GravestoneManager.checkDelivery(uuid)) { return; }
        GravestoneManager.addDelivery(uuid);

        // Get the location data
        String worldName = context.getArgument("world", String.class);
        World world = Bukkit.getWorld(worldName);
        BlockPositionResolver blockPositionResolver = context.getArgument("coordinates", BlockPositionResolver.class);
        BlockPosition blockPosition = blockPositionResolver.resolve(context.getSource());

        double x = blockPosition.x();
        double y = blockPosition.y();
        double z = blockPosition.z();

        Location gravestoneLocation = new Location(world, x, y, z);

        // Fetch the gravestone from the database
        String[] values;
        try {
            values = GravestoneDatabase.getGravestone(gravestoneLocation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Handle it if nothing was returned by the database
        if (values == null) {
            Replacements replacements = new Replacements.Builder()
                    .add("<COORDINATES>", x + " " + y + " " + z)
                    .build();

            plugin.message.sendMessage(player, "no_gravestone_found", replacements);
            return;
        }

        // UUID and player related logic
        String graveOwnerString = values[0];
        if (graveOwnerString == null || graveOwnerString.isEmpty()) {
            GravestoneManager.removeDelivery(uuid);
            return;
        }

        Player graveOwnerPlayer = Bukkit.getPlayer(UUID.fromString(graveOwnerString));
        if (player != graveOwnerPlayer) {
            plugin.message.sendMessage(player, "no_permission");
            return;
        }

        // Logic without vault
        if (state == RetrieveState.noVault) {











            return;
        }

        // Logic with vault
        // Check if the player is in the same world
        boolean sameWorld = player.getWorld().equals(world);
        Location calulatedLocation = player.getLocation();
        if (!sameWorld) {
            calulatedLocation.setWorld(world);
        }
        // Calculate the distance between the player and the gravestone
        double distance = calulatedLocation.distance(gravestoneLocation);

        // Get cost of retrieving the gravestone every 100 blocks
        // TODO: would be a nice feature to put this in a config

        // Calculate the cost of retrieving the gravestone
        int cost = (int) Math.ceil(distance / configBlocks) * configPrice;
        // Add 100 cost if the player is not in the same world
        if (!sameWorld) {
            cost += configWorldPrice;
        }
        // Check if the player has enough money
        if (economy == null || economy.getBalance(player) < cost) {
            Replacements replacements = new Replacements.Builder()
                    .add("<PRICE>", String.valueOf(cost))
                    .build();

            plugin.message.sendMessage(player, "not_enough_money", replacements);
            GravestoneManager.removeDelivery(uuid);
            return;
        }

        // Logic for when the price is not confirmed
        if (state == RetrieveState.notConfirmed) {
            Replacements replacements = new Replacements.Builder()
                    .add("<PRICE>", String.valueOf(cost))
                    .add("<COMMAND>", "/retrieve-gravestone " + world + " " + x + " " + y + " " + z + " confirm " + cost)
                    .build();

            GravestoneManager.removeDelivery(uuid);
            plugin.message.sendMessage(player, "retrieve_confirm", replacements);
            return;
        }
        int price = context.getArgument("price", Integer.class);

        if (price != cost) {
            plugin.message.sendMessage(player, "retrieve_price_changed");
            GravestoneManager.removeDelivery(uuid);
            return;
        }

        // Send delivery message
        plugin.message.sendMessage(player, "retrieve_confirmed");

        // Inventory logic
        String DatabaseInventory = values[1];
        ItemStack[] inventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);
        startDelivery(player, location, gravestoneLocation, inventory, cost, uuid);
    }


    private static boolean usingVault() {
        Boolean usesVault = plugin.config.general.getBoolean("use_vault");
        if (usesVault == null) { throw new RuntimeException("PLUGIN GENERAL CONFIG use_vault IS SET INCORRECTLY!"); }
        return usesVault;
    }

    private static void startDelivery(Player player, Location location, Location gravestoneLocation, ItemStack[] inventory, int cost, UUID uuid) {

        new BukkitRunnable() {
            public void run() {
                try {
                    // Start animation asynchronously and wait for it to complete
                    startAnimation(location).get();
                    runEnd(player, location, gravestoneLocation, inventory, cost, uuid).get();
                } catch (Exception e) {
                    runEnd(player, location, gravestoneLocation, inventory, cost, uuid);
                    plugin.getLogger().log(Level.SEVERE, e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private static CompletableFuture<Void> runEnd(Player player, Location location, Location gravestoneLocation, ItemStack[] inventory, int cost, UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            // Run synchronous Bukkit tasks
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Drop the inventory
                    if (inventory != null) {
                        for (ItemStack item : inventory) {
                            if (item != null) {
                                location.getWorld().dropItemNaturally(location, item);
                            }
                        }
                    }
                }
            }.runTask(plugin);

            try {
                GravestoneDatabase.deleteGravestone(gravestoneLocation);
                GravestoneManager.deleteGravestone(gravestoneLocation);
                GravestoneManager.removeDelivery(uuid);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Remove the gravestone block
                        gravestoneLocation.getBlock().setType(Material.AIR);
                        economy.withdrawPlayer(player, cost);
                    }
                }.runTask(plugin);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, exception.getMessage());
                System.out.println("Failed to delete the database entry! " + exception.getMessage());
            }
        });
    }

    private static CompletableFuture<Void> startAnimation(Location playerLocation) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Schedule the entity spawning on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Spawn a creeper 100 blocks in front of the player and 10 blocks above the player location in the direction the player is facing
                        Location spawnLocation = playerLocation.clone().add(playerLocation.getDirection().multiply(100));
                        spawnLocation.setY(playerLocation.getY() + 10);

                        Creeper creeper = (Creeper) playerLocation.getWorld().spawnEntity(spawnLocation, EntityType.CREEPER);
                        creeper.customName(Component.text("Delivery"));
                        creeper.setCustomNameVisible(true);
                        creeper.setAI(false); // Disable AI to prevent it from moving on its own

                        // Start the animation tasks
                        startAnimationTasks(creeper, playerLocation, future);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return future;
    }

    private static void startAnimationTasks(Creeper creeper, Location playerLocation, CompletableFuture<Void> future) {
        // Move to location 1 block in front of the looking position of the player
        playerLocation.add(playerLocation.getDirection().multiply(1));

        int totalTicks = 20; // Animation duration in ticks (1 second = 20 ticks)
        int rangeCheckTicks = 40; // 2 seconds (40 ticks)
        int rangeCheckInterval = 2; // Check every 2 ticks (100ms)
        int rangeCheckCount = rangeCheckTicks / rangeCheckInterval;
        final boolean[] removeAfterTimeout = {false};
        final boolean[] exploded = {false};

        // Schedule the main animation task
        BukkitTask animationTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= totalTicks) { // After animation duration, stop animation and explode creeper
                    cancel();
                    explodeCreeper(creeper);
                    future.complete(null); // Complete the future when done
                    return;
                }

                double t = (double) ticks / totalTicks; // Normalize ticks to [0, 1]
                double ease = t * t * (3 - 2 * t); // Smoothstep easing function

                Location currentLocation = creeper.getLocation();
                Location targetLocation = playerLocation.clone(); // Hover 1 block above the player

                // Interpolate the position with easing
                double x = currentLocation.getX() + (targetLocation.getX() - currentLocation.getX()) * ease;
                double y = currentLocation.getY() + (targetLocation.getY() - currentLocation.getY()) * ease;
                double z = currentLocation.getZ() + (targetLocation.getZ() - currentLocation.getZ()) * ease;
                Location newLocation = new Location(playerLocation.getWorld(), x, y, z);

                // Schedule teleportation to be done synchronously
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        creeper.teleport(newLocation);
                    }
                }.runTask(plugin);

                ticks++;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L); // Run every tick (50ms)

        // Schedule range check task
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (removeAfterTimeout[0] || exploded[0] || !creeper.isValid()) {
                    cancel();
                    return;
                }

                if (creeper.getLocation().distanceSquared(playerLocation) <= 25) { // 5 blocks radius
                    ticks++;
                    if (ticks >= rangeCheckCount) {
                        removeAfterTimeout[0] = true;
                        explodeCreeper(creeper);
                        exploded[0] = true; // Prevents repeated explosions
                        animationTask.cancel(); // Cancel animation task if not already cancelled
                        future.complete(null); // Complete the future when done
                        cancel();
                    }
                } else {
                    ticks = 0; // Reset ticks if creeper moves out of range
                }
            }
        }.runTaskTimer(plugin, 0L, rangeCheckInterval); // Run every rangeCheckInterval ticks
    }

    private static void explodeCreeper(Creeper creeper) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!creeper.isDead()) {
                    creeper.getWorld().createExplosion(creeper.getLocation(), 0F, false, false); // 0F power, no fire, no block damage
                    creeper.remove();
                    // Sound effect for the explosion
                    creeper.getWorld().playSound(creeper.getLocation(), "entity.creeper.primed", 1.0F, 1.0F);
                    // Particle effect for the explosion
                    creeper.getWorld().spawnParticle(Particle.EXPLOSION, creeper.getLocation(), 1);
                }
            }
        }.runTask(plugin);
    }
}