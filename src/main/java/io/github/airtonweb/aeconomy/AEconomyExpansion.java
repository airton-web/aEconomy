package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import io.github.airtonweb.aeconomy.api.MoneyFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

public class AEconomyExpansion extends PlaceholderExpansion {

    private static final String SHORT_SUFFIX = "_short";

    private final Economy economy;
    private final String version;

    public AEconomyExpansion(Economy economy, String version) {
        this.economy = economy;
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
        if (player == null) {
            return "";
        }

        // %aeconomy_coins%       -> params = "coins"
        // %aeconomy_coins_short% -> params = "coins_short"
        String currency = params.toLowerCase();
        boolean shortFormat = false;

        if (currency.endsWith(SHORT_SUFFIX)) {
            shortFormat = true;
            currency = currency.substring(0, currency.length() - SHORT_SUFFIX.length());
        }

        double balance = economy.getBalance(player.getUniqueId(), currency);

        if (shortFormat) {
            return MoneyFormat.format(balance);
        }

        // Locale.US garante ponto como separador decimal (12.50), não vírgula
        return String.format(Locale.US, "%.2f", balance);
    }
}