package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Classe principal do plugin de economia.
 *
 * - O "package" precisa bater com as pastas:
 *   src/main/java/io/github/airtonweb/aeconomy/
 * - O nome desta classe (aEconomy) precisa ser IGUAL, letra por letra,
 *   ao final do campo "main" do plugin.yml.
 */
public final class aEconomy extends JavaPlugin {

    // CAMPO: fica na classe, fora dos métodos
    private final EconomyManager economy = new EconomyManager();

    // Chamado quando o servidor liga o plugin
    @Override
    public void onEnable() {
        // Publica a economia no "quadro de avisos" para outros plugins usarem
        getServer().getServicesManager().register(
                Economy.class, economy, this, ServicePriority.Normal);

        getCommand("saldo").setExecutor(new SaldoCommand(economy));

        Bukkit.getConsoleSender().sendMessage("§2[aEconomy] §aPlugin ativado com sucesso!");
        Bukkit.getConsoleSender().sendMessage("§2[aEconomy] §aVersão do Plugin: " + getPluginMeta().getVersion());
    }

    // Chamado quando o servidor desliga o plugin
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§4[aEconomy] §cPlugin desligado!");
    }
}