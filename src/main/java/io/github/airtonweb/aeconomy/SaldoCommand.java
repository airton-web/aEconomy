package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import org.bukkit.Bukkit;
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

        // 1) /saldo dar ...: pode ser jogador OU console
        if (args.length > 0 && args[0].equalsIgnoreCase("dar")) {
            give(sender, args);
            return true;
        }

        // 2) /saldo normal: só jogadores
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem executar este comando");
            return true;
        }

        Player player = (Player) sender;
        double balance = economy.getBalance(player.getUniqueId(), "coins");
        player.sendMessage("§7Seu saldo: §a" + balance + " coins");
        return true;
    }

    // /saldo dar <jogador> <moeda> <valor>
    private void give(CommandSender sender, String[] args) {

        // 1) permissão
        if (!sender.hasPermission("aeconomy.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return;
        }

        // 2) quantidade de argumentos
        if (args.length != 4) {
            sender.sendMessage("§cUse: /saldo dar <jogador> <moeda> <valor>");
            return;
        }

        // 3) achar o jogador (devolve null se ele não estiver online)
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado ou offline: " + args[1]);
            return;
        }

        // 4) converter o valor (texto -> número)
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cO valor precisa ser um número. Use ponto para decimais: 12.5");
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            sender.sendMessage("§cO valor precisa ser maior que zero.");
            return;
        }

        // 5) dar o dinheiro e avisar os dois lados
        String currency = args[2].toLowerCase();
        economy.addBalance(target.getUniqueId(), currency, amount);

        sender.sendMessage("§7Você deu §a" + amount + " " + currency + " §7para §f" + target.getName() + "§7.");
        target.sendMessage("§7Você recebeu §a" + amount + " " + currency + "§7.");
    }
}