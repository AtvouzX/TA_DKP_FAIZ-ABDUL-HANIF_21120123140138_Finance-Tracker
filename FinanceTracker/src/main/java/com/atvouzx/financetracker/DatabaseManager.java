package com.atvouzx.financetracker;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:src/main/resources/data/finance_tracker.db";

    static {
        try (Connection connection = DriverManager.getConnection(URL)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String query = "SELECT * FROM wallets";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");
                wallets.add(new Wallet(id, name, balance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wallets;
    }
}
