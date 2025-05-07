package com.lyttledev.lyttlegravestone.database;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import com.lyttledev.lyttlegravestone.utils.StringLocationConvertor;
import com.lyttledev.lyttleutils.utils.convertion.ItemSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;

public class GravestoneDatabase {
    private static Connection connection;

    public GravestoneDatabase(LyttleGravestone plugin) throws SQLException {
        String path = plugin.getDataFolder().getAbsolutePath() + "/gravestone.db";
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                        CREATE TABLE IF NOT EXISTS graves (
                        location TEXT PRIMARY KEY,
                        username TEXT,
                        uuid TEXT,
                        inventoryContents TEXT);
                    """);
        }
    }

    public static void addGravestone(Location location, Player player, ItemStack[] gravestoneInventory) throws SQLException {
        String locationString = StringLocationConvertor.locationToString(location);
        String playerName = player.getName();
        String UUID = String.valueOf(player.getUniqueId());
        String inventory = ItemSerializer.serializeInventory(gravestoneInventory);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO graves (location, username, uuid, inventoryContents) VALUES (?, ?, ?, ?)"
        )) {
            preparedStatement.setString(1, locationString);
            preparedStatement.setString(2, playerName);
            preparedStatement.setString(3, UUID);
            preparedStatement.setString(4, inventory);
            preparedStatement.executeUpdate();
        }
    }

    public static void updateGravestone(ItemStack[] gravestoneInventory, Location location) throws SQLException {

        String locationString = StringLocationConvertor.locationToString(location);
        String inventory = ItemSerializer.serializeInventory(gravestoneInventory);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE graves SET inventoryContents = ? WHERE location = ?;"
        )) {
            preparedStatement.setString(1, inventory);
            preparedStatement.setString(2, locationString);
            preparedStatement.executeUpdate();
        }
    }

    public static String[] getGravestone(Location location) throws SQLException {
        String locationString = StringLocationConvertor.locationToString(location);

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM graves WHERE location = ?")) {
            preparedStatement.setString(1, locationString);
            ResultSet resultSet = preparedStatement.executeQuery();
            return new String[]{resultSet.getString("uuid"), resultSet.getString("inventoryContents")};
        }
    }

    public static void deleteGravestone(Location location) throws SQLException {
        String locationString = StringLocationConvertor.locationToString(location);

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM graves WHERE location = ?")) {
            preparedStatement.setString(1, locationString);
            preparedStatement.executeUpdate();
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void initGravestonesCache() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM graves");
            while (resultSet.next()) {
                String locationString = resultSet.getString("location");
                Location location = StringLocationConvertor.stringToLocation(locationString);
                GravestoneManager.addGravestone(location);
            }
        }
    }
}