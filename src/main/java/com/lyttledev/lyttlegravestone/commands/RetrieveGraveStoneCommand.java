package com.lyttledev.lyttlegravestone.commands;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.utils.ItemSerializer;
import com.lyttledev.lyttlegravestone.utils.Memory;
import com.lyttledev.lyttlegravestone.utils.Message;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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

public class RetrieveGraveStoneCommand implements Command<CommandSourceStack> {
    // Import Economy from Vault
    private static Economy economy = null;
    private static LyttleGravestone plugin = null;

    public static void register(LyttleGravestone pl, Commands commands) {
        // Get the economy from Vault
        economy = pl.getEconomy();
        plugin = pl;

        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
            Commands.literal("retrieve-gravestone")
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("world", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("x", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("y", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("z", StringArgumentType.string())
                .executes(new RetrieveGraveStoneCommand())
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("confirm", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("price", StringArgumentType.string())
                .executes(new RetrieveGraveStoneCommand())))))));

        commands.register(
            commandBuilder.build(),
            "Retrieve a gravestone",
            List.of("rg")
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            Entity entity = source.getExecutor();

            if (!(entity instanceof Player)) { return 0; }

            Player player = (Player) entity;
            Location location = entity.getLocation();

            if (Memory.checkDelivery(player)) { return 0; }
            Memory.addDelivery(player);

            String world = context.getArgument("world", String.class);
            String x = context.getArgument("x", String.class);
            String y = context.getArgument("y", String.class);
            String z = context.getArgument("z", String.class);

            boolean confirm = false;
            try {
                String confirmArg = context.getArgument("confirm", String.class);
                confirm = confirmArg.equals("confirm");
            } catch (IllegalArgumentException ignored) { }

            // Retrieve gravestone at x, y, z
            Location gravestoneLocation = new Location(Bukkit.getWorld(world), Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));

            String[] values = null;
            try {
                values = GravestoneDatabase.getGravestone(gravestoneLocation);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (values == null) {
                String[][] replacements = {{"<COORDINATES>", x + " " + y + " " + z}};
                Message.sendMessage(player, "no_gravestone_found", replacements);
                Memory.removeDelivery(player);
                return 0;
            }

            // UUID and player related logic
            String graveOwnerString = values[0];
            Player graveOwnerPlayer = Bukkit.getPlayer(UUID.fromString(graveOwnerString));

            // Permission logic
            if (player != graveOwnerPlayer && !player.hasPermission("lyttlegravestone.staff")) {
                Message.sendMessage(player, "no_permission");
                Memory.removeDelivery(player);
                return 0;
            }

            // Calculate the distance between the player and the gravestone
            double distance = location.distance(gravestoneLocation);

            // Get cost of retrieving the gravestone every 100 blocks
            // TODO: would be a nice feature to put this in a config
            int cost100xBlocks = 3;

            // Calculate the cost of retrieving the gravestone
            int cost = (int) Math.ceil(distance / 100) * cost100xBlocks;

            // Check if the player has enough money
            if (economy != null && economy.getBalance(player) < cost || economy == null) {
                String[][] replacements = {{"<PRICE>", String.valueOf(cost)}};
                Message.sendMessage(player, "not_enough_money", replacements);
                Memory.removeDelivery(player);
                return 0;
            }

            // Check if the player has confirmed the retrieval
            // TODO, put this thing in a config
            if (!confirm) {
                String[][] replacements = {
                        {"<PRICE>", String.valueOf(cost)},
                        {"<COMMAND>", "/retrieve-gravestone " + world + " " + x + " " + y + " " + z + " confirm " + cost}
                };

                Memory.removeDelivery(player);
                Message.sendMessage(player, "retrieve_confirm", replacements);
                return 0;
            }

            int price = 0;
            try {
                String priceArg = context.getArgument("price", String.class);
                price = Integer.parseInt(priceArg);
            } catch (IllegalArgumentException ignored) {}

            if (price != cost) {
                Message.sendMessage(player,"retrieve_price_changed");
                Memory.removeDelivery(player);
                return 0;
            }

            // Inventory logic
            String DatabaseInventory = values[1];
            ItemStack[] inventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);

            // Run your async function
            runAsync(() -> {
                try {
                    // Start animation asynchronously and wait for it to complete
                    startAnimation(location).get();
                    runEnd(player, location, gravestoneLocation, inventory, cost).get();
                } catch (Exception e) {
                    runEnd(player, location, gravestoneLocation, inventory, cost);
                    e.printStackTrace();
                }
            });

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private CompletableFuture<Void> runEnd(Player player, Location location, Location gravestoneLocation, ItemStack[] inventory, int cost) {
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
                Memory.deleteGravestone(gravestoneLocation);
                Memory.removeDelivery(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Remove the gravestone block
                        gravestoneLocation.getBlock().setType(Material.AIR);
                        economy.withdrawPlayer(player, cost);
                    }
                }.runTask(plugin);
            } catch (SQLException exception) {
                exception.printStackTrace();
                System.out.println("Failed to delete the database entry! " + exception.getMessage());
            }
        });
    }

    private CompletableFuture<Void> startAnimation(Location playerLocation) {
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
                        creeper.setCustomName("Delivery");
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

    private void startAnimationTasks(Creeper creeper, Location playerLocation, CompletableFuture<Void> future) {
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

    private void explodeCreeper(Creeper creeper) {
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
    private void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}