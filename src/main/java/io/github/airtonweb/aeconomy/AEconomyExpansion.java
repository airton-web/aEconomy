package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import io.github.airtonweb.aeconomy.api.MoneyFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AEconomyExpansion extends PlaceholderExpansion {

    private static final String SHORT_SUFFIX = "_short";

    // "coins_short_2" -> grupo 1 = "coins", grupo 2 = "2"
    private static final Pattern SHORT_DECIMALS = Pattern.compile("^(.+)_short_(\\d)$");

    // "top_coins_1_name" -> moeda = "coins", posição = "1", tipo = "name"
    private static final Pattern TOP = Pattern.compile("^top_(.+)_(\\d{1,2})_(name|value)$");

    // O que aparece quando não há ninguém naquela posição
    private static final String EMPTY_NAME = "-";
    private static final String EMPTY_VALUE = "0";

    private final Economy economy;
    private final TopRanking ranking;
    private final String version;

    public AEconomyExpansion(Economy economy, TopRanking ranking, String version) {
        this.economy = economy;
        this.ranking = ranking;
        this.version = version;
    }

    // O "prefixo" dos placeholders: %aeconomy_...%
    @Override
    public String getIdentifier() {
        return "aeconomy";
    }

    @Override
    public String getAuthor() {
        return "airton-web";
    }

    @Override
    public String getVersion() {
        return version;
    }

    // Não descarregar a expansão quando o PlaceholderAPI recarregar
    @Override
    public boolean persist() {
        return true;
    }

    // Chamado sempre que alguém usa %aeconomy_<params>%
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String text = params.toLowerCase(Locale.ROOT);

        // %aeconomy_top_coins_1_name% / %aeconomy_top_coins_1_value%
        // (o ranking não depende de um jogador específico)
        Matcher top = TOP.matcher(text);
        if (top.matches()) {
            return topPlaceholder(top.group(1), Integer.parseInt(top.group(2)), top.group(3));
        }

        // Daqui para baixo, o placeholder é sobre o saldo de um jogador
        if (player == null) {
            return "";
        }

        // %aeconomy_coins_short_2% -> valor abreviado com 2 casas FIXAS
        Matcher matcher = SHORT_DECIMALS.matcher(text);
        if (matcher.matches()) {
            double balance = economy.getBalance(player.getUniqueId(), matcher.group(1));
            int decimals = Integer.parseInt(matcher.group(2));
            return MoneyFormat.format(balance, decimals);
        }

        // %aeconomy_coins_short% -> valor abreviado, sem zeros sobrando
        if (text.endsWith(SHORT_SUFFIX)) {
            String currency = text.substring(0, text.length() - SHORT_SUFFIX.length());
            double balance = economy.getBalance(player.getUniqueId(), currency);
            return MoneyFormat.format(balance);
        }

        // %aeconomy_coins% -> valor completo com 2 casas
        double balance = economy.getBalance(player.getUniqueId(), text);

        // Locale.US garante ponto como separador decimal (12.50), não vírgula
        return String.format(Locale.US, "%.2f", balance);
    }

    // Nome ou valor de uma posição do ranking
    private String topPlaceholder(String currency, int position, String kind) {
        TopRanking.Entry entry = ranking.getPosition(currency, position);

        if (entry == null) {
            return kind.equals("name") ? EMPTY_NAME : EMPTY_VALUE;
        }
        return kind.equals("name") ? entry.name() : MoneyFormat.format(entry.amount());
    }
}