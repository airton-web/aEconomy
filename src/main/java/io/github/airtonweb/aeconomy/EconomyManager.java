package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager implements Economy {

    // moeda -> (jogador -> saldo)
    private final Map<String, Map<UUID, Double>> balances = new HashMap<>();

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
        // Se ainda não existe a tabela dessa moeda, cria uma vazia
        Map<UUID, Double> currencyBalances =
                balances.computeIfAbsent(currency, key -> new HashMap<>());

        currencyBalances.put(playerId, currencyBalances.getOrDefault(playerId, 0.0) + amount);
    }
}