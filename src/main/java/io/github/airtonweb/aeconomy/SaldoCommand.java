package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaldoCommand implements CommandExecutor {

    private final Economy economy;

    // Construtor: recebe a economia que a classe principal criou
    public SaldoCommand(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem executar este comando");
            return true;
        }

        Player player = (Player) sender;
        double balance = economy.getBalance(player.getUniqueId(), "coins");
        player.sendMessage("§7Seu saldo: §a" + balance + " coins");
        return true;
    }
}