package io.github.airtonweb.aeconomy;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Database {

    private final String url;
    private Connection connection;

    public Database(File file) {
        this.url = "jdbc:sqlite:" + file.getAbsolutePath();
    }

    // Abre o banco e cria a tabela, se ainda não existir
    public synchronized void connect() throws SQLException {
        connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS balances ("
                            + "uuid TEXT NOT NULL, "
                            + "currency TEXT NOT NULL, "
                            + "amount REAL NOT NULL, "
                            + "PRIMARY KEY (uuid, currency))");
        }
    }

    // Lê todos os saldos: moeda -> (jogador -> saldo)
    public synchronized Map<String, Map<UUID, Double>> loadAll() throws SQLException {
        Map<String, Map<UUID, Double>> result = new HashMap<>();

        String sql = "SELECT uuid, currency, amount FROM balances";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String currency = rs.getString("currency");
                double amount = rs.getDouble("amount");

                result.computeIfAbsent(currency, key -> new HashMap<>()).put(uuid, amount);
            }
        }
        return result;
    }

    // Salva (ou atualiza) o saldo de um jogador em uma moeda
    public synchronized void save(UUID uuid, String currency, double amount) throws SQLException {
        String sql = "INSERT INTO balances (uuid, currency, amount) VALUES (?, ?, ?) "
                + "ON CONFLICT(uuid, currency) DO UPDATE SET amount = excluded.amount";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, currency);
            statement.setDouble(3, amount);
            statement.executeUpdate();
        }
    }

    // Fecha a conexão
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ao desligar, não há mais o que fazer com o erro
            }
        }
    }
}