package io.github.airtonweb.aeconomy.api;

import java.util.UUID;

public interface Economy {

    // Saldo do jogador em uma moeda (ex.: "coins")
    double getBalance(UUID playerId, String currency);

    // Soma um valor ao saldo do jogador nessa moeda
    void addBalance(UUID playerId, String currency, double amount);

    // Define o saldo (substitui o valor atual). Devolve false se não foi possível.
    boolean setBalance(UUID playerId, String currency, double amount);

    // Tira dinheiro. Devolve false, sem alterar nada, se o saldo for insuficiente.
    boolean removeBalance(UUID playerId, String currency, double amount);
}