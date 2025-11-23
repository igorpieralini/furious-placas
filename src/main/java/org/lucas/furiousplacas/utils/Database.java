package org.lucas.furiousplacas.utils;

import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.*;

public class Database {
    private final String HOST;
    private final int PORT;
    private final String DATABASE;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection connection;

    public Database(String HOST, int PORT, String DATABASE, String USERNAME, String PASSWORD) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.DATABASE = DATABASE;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&allowPublicKeyRetrieval=true",
                USERNAME,
                PASSWORD);

        PreparedStatement ps = connection.prepareStatement("" +
                "CREATE TABLE IF NOT EXISTS PLACAS(\n" +
                "ID varchar(255),\n" +
                "ITEM VARCHAR(9999),\n" +
                "SIGN varchar(255)\n" +
                ");");

        ps.executeUpdate();
    }

    public void addCustomSign(String id, Sign sign, ItemStack itemStack) throws SQLException, IOException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO PLACAS (ID,ITEM,SIGN) VALUES (?,?,?);");

        ItemStack[] items = new ItemStack[1];
        items[0] = itemStack;
        String serializedItemStack = Serialization.itemStackArrayToBase64(items);

        ps.setString(1, id);
        ps.setObject(2, serializedItemStack);
        ps.setString(3, sign.getLocation().toString());
        ps.executeUpdate();
    }

    public void removeCustomSign(String id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM PLACAS WHERE ID = ?;");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getItemStack(Sign sign) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM PLACAS WHERE ID=?;");
        if (sign.getLine(3).equalsIgnoreCase("Sem item")) return null;
        ps.setString(1, sign.getLine(3).split("#")[1]);
        ResultSet resultSet = ps.executeQuery();
        resultSet.next();

        ItemStack[] items = new ItemStack[1];
        try {
            items = Serialization.itemStackArrayFromBase64(resultSet.getString("ITEM"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items[0];
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void disconnect() {
        try {
            if (isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
