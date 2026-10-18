package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class aEconomy extends JavaPlugin {

    private Database database;
    private EconomyManager economy;

    @Override
    public void onEnable() {
        // Garante que a pasta plugins/aEconomy existe
        getDataFolder().mkdirs();

        database = new Database(new File(getDataFolder(), "aEconomy.db"));

        try {
            database.connect();
            economy = new EconomyManager(database, getLogger());
            economy.load();
        } catch (SQLException e) {
            getLogger().severe("Não foi possível abrir o banco de dados: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Publica a economia no "quadro de avisos" para outros plugins usarem
        getServer().getServicesManager().register(
                Economy.class, economy, this, ServicePriority.Normal);

        // Comando /saldo, com autocompletar
        SaldoCommand saldoCommand = new SaldoCommand(economy);
        getCommand("saldo").setExecutor(saldoCommand);
        getCommand("saldo").setTabCompleter(saldoCommand);

        // Só registra os placeholders se o PlaceholderAPI estiver instalado e ligado
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AEconomyExpansion(economy, getPluginMeta().getVersion()).register();
            getLogger().info("PlaceholderAPI encontrado: placeholders do aEconomy registrados.");
        }

        Bukkit.getConsoleSender().sendMessage("§2[aEconomy] §aPlugin ativado com sucesso!");
        Bukkit.getConsoleSender().sendMessage("§2[aEconomy] §aVersão do Plugin: " + getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        Bukkit.getConsoleSender().sendMessage("§4[aEconomy] §cPlugin desligado!");
    }
}