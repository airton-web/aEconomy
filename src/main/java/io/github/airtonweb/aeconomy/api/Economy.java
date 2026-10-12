package io.github.airtonweb.aeconomy.api;

import java.util.UUID;

public interface Economy {

    // Saldo do jogador em uma moeda (ex.: "coins")
    double getBalance(UUID playerId, String currency);

    // Soma um valor ao saldo do jogador nessa moeda
    void addBalance(UUID playerId, String currency, double amount);
}