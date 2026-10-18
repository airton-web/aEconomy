package io.github.airtonweb.aeconomy.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MoneyFormat {

    // A posição no array = quantos "grupos de mil":
    // 0 = unidades, 1 = mil (K), 2 = milhão (M), 3 = bilhão (B)...
    private static final String[] SUFFIXES = {
            "",    // unidades
            "K",   // mil          10^3
            "M",   // milhão       10^6
            "B",   // bilhão       10^9
            "T",   // trilhão      10^12
            "Q",   // quadrilhão   10^15
            "QQ",  // quintilhão   10^18
            "S",   // sextilhão    10^21
            "SS",  // septilhão    10^24
            "O",   // octilhão     10^27
            "N"    // nonilhão     10^30
    };

    // Letras digitadas (em minúsculo) -> expoente de 10. Ex.: "k" -> 3, "m" -> 6
    private static final Map<String, Integer> EXPONENTS = new HashMap<>();

    // Um número, seguido de letras opcionais: 1500, 1.5k, 2M
    private static final Pattern INPUT = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-z]*)$");

    // Roda uma vez, quando a classe é carregada: preenche os expoentes
    static {
        for (int i = 1; i < SUFFIXES.length; i++) {
            EXPONENTS.put(SUFFIXES[i].toLowerCase(Locale.ROOT), i * 3);
        }
    }

    // Classe só de métodos estáticos: ninguém precisa criar um objeto dela
    private MoneyFormat() {
    }

    // 1500 -> "1.5K", 2000000 -> "2M"
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }

        String sign = value < 0 ? "-" : "";
        double abs = Math.abs(value);

        // Divide por mil até ficar menor que 1000, contando as divisões
        int index = 0;
        while (abs >= 1000 && index < SUFFIXES.length - 1) {
            abs /= 1000;
            index++;
        }

        // Arredonda para 2 casas. Se virou 1000 (ex.: 999.999K), sobe para a próxima letra
        double rounded = Math.round(abs * 100) / 100.0;
        if (rounded >= 1000 && index < SUFFIXES.length - 1) {
            rounded /= 1000;
            index++;
        }

        return sign + trim(rounded) + SUFFIXES[index];
    }

    // "1.5k" -> 1500.0. Devolve null se o texto for inválido.
    public static Double parse(String text) {
        Matcher matcher = INPUT.matcher(text.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return null;
        }

        String suffix = matcher.group(2);
        int exponent = 0;
        if (!suffix.isEmpty()) {
            Integer found = EXPONENTS.get(suffix);
            if (found == null) {
                return null;   // letra desconhecida, como "5kk" ou "5x"
            }
            exponent = found;
        }

        // Conta exata com BigDecimal: evita erros de arredondamento do double
        BigDecimal number = new BigDecimal(matcher.group(1));
        double result = number.multiply(BigDecimal.TEN.pow(exponent)).doubleValue();

        if (Double.isInfinite(result) || Double.isNaN(result)) {
            return null;
        }
        return result;
    }

    // 12.50 -> "12.5", 12.00 -> "12"
    private static String trim(double value) {
        String text = String.format(Locale.US, "%.2f", value);
        return text.replaceAll("\\.?0+$", "");
    }
}