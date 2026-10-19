package io.github.airtonweb.aeconomy;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopRanking {

    // Quantas posições o ranking tem
    public static final int SIZE = 10;

    // Uma linha do ranking: nome + valor.
    // (record = uma classe curta, só para guardar dados)
    public record Entry(String name, double amount) {
    }

    private final EconomyManager economy;

    // A "foto" atual do ranking: moeda -> os primeiros colocados.
    // volatile: outras threads (como a do TAB) sempre enxergam a foto mais recente.
    private volatile Map<String, List<Entry>> snapshot = Map.of();

    public TopRanking(EconomyManager economy) {
        this.economy = economy;
    }

    // Recalcula o ranking de TODAS as moedas. Roda na thread principal.
    public void refresh() {
        Map<String, List<Entry>> result = new HashMap<>();

        for (String currency : economy.getCurrencies()) {
            result.put(currency, buildTop(currency));
        }

        // Troca a foto antiga pela nova de uma vez só
        snapshot = result;
    }

    // Monta o top de uma moeda
    private List<Entry> buildTop(String currency) {
        List<Map.Entry<UUID, Double>> sorted =
                new ArrayList<>(economy.getAllBalances(currency).entrySet());

        // Do maior para o menor saldo
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Entry> top = new ArrayList<>();
        for (Map.Entry<UUID, Double> row : sorted) {
            if (top.size() >= SIZE) {
                break;
            }
            if (row.getValue() <= 0) {
                continue;   // quem tem 0 não entra no ranking
            }
            top.add(new Entry(resolveName(row.getKey()), row.getValue()));
        }

        return Collections.unmodifiableList(top);
    }

    // Descobre o nome do jogador pelo UUID (funciona para quem já entrou no servidor)
    private String resolveName(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name != null) {
            return name;
        }
        return uuid.toString().substring(0, 8);   // plano B: começo do UUID
    }

    // Todo o top de uma moeda (lista vazia se não existir)
    public List<Entry> getTop(String currency) {
        return snapshot.getOrDefault(currency, List.of());
    }

    // Uma posição específica (1 a 10). Devolve null se não houver ninguém ali.
    public Entry getPosition(String currency, int position) {
        List<Entry> top = getTop(currency);
        if (position < 1 || position > top.size()) {
            return null;
        }
        return top.get(position - 1);
    }
}