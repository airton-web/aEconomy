package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class EconomyManager implements Economy {

    // moeda -> (jogador -> saldo): a "cópia rápida" na memória
    private final Map<String, Map<UUID, Double>> balances = new HashMap<>();

    private final Database database;
    private final Logger logger;

    public EconomyManager(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    // Chamado ao ligar o plugin: traz os saldos do banco para a memória
    public void load() throws SQLException {
        balances.putAll(database.loadAll());
    }

    @Override
    public double getBalance(UUID playerId, String currency) {
        Map<UUID, Double> currencyBalances = balances.get(currency);
        if (currencyBalances == null) {
            return 0.0;   // ninguém tem saldo dessa moeda ainda
        }
        return currencyBalances.getOrDefault(playerId, 0.0);
    }

    @Override
    public void addBalance(UUID playerId, String currency, double amount) {
        Map<UUID, Double> currencyBalances =
                balances.computeIfAbsent(currency, key -> new HashMap<>());

        double newAmount = currencyBalances.getOrDefault(playerId, 0.0) + amount;
        currencyBalances.put(playerId, newAmount);

        // Grava no banco
        try {
            database.save(playerId, currency, newAmount);
        } catch (SQLException e) {
            logger.severe("Erro ao salvar o saldo de " + playerId + ": " + e.getMessage());
        }
    }

    @Override
    public boolean setBalance(UUID playerId, String currency, double amount) {
        if (!isValid(amount)) {
            return false;
        }
        return store(playerId, currency, amount);
    }

    @Override
    public boolean removeBalance(UUID playerId, String currency, double amount) {
        if (!isValid(amount) || amount == 0) {
            return false;
        }

        double current = getBalance(playerId, currency);
        if (current < amount) {
            return false;   // saldo insuficiente
        }
        return store(playerId, currency, current - amount);
    }

    @Override
    public Set<String> getCurrencies() {
        // Devolve uma CÓPIA, para quem chama não mexer no Map por dentro
        return new HashSet<>(balances.keySet());
    }

    // Número válido: nem NaN, nem infinito, nem negativo
    private boolean isValid(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0;
    }

    // Grava no banco PRIMEIRO; só altera a memória se deu certo
    private boolean store(UUID playerId, String currency, double newAmount) {
        try {
            database.save(playerId, currency, newAmount);
        } catch (SQLException e) {
            logger.severe("Erro ao salvar o saldo de " + playerId + ": " + e.getMessage());
            return false;
        }

        balances.computeIfAbsent(currency, key -> new HashMap<>()).put(playerId, newAmount);
        return true;
    }
}