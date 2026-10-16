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

        // Subcomandos de administração: podem vir de jogador OU do console
        if (args.length > 0) {
            String sub = args[0].toLowerCase();

            if (sub.equals("dar")) {
                give(sender, args);
                return true;
            }
            if (sub.equals("remover")) {
                remove(sender, args);
                return true;
            }
            if (sub.equals("setar")) {
                set(sender, args);
                return true;
            }
        }

        // /saldo normal: só jogadores
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
        Player target = prepare(sender, args, "/saldo dar <jogador> <moeda> <valor>");
        if (target == null) {
            return;
        }

        Double amount = parseAmount(sender, args[3], false);
        if (amount == null) {
            return;
        }

        String currency = args[2].toLowerCase();
        economy.addBalance(target.getUniqueId(), currency, amount);

        sender.sendMessage("§7Você deu §a" + amount + " " + currency + " §7para §f" + target.getName() + "§7.");
        target.sendMessage("§7Você recebeu §a" + amount + " " + currency + "§7.");
    }

    // /saldo remover <jogador> <moeda> <valor>
    private void remove(CommandSender sender, String[] args) {
        Player target = prepare(sender, args, "/saldo remover <jogador> <moeda> <valor>");
        if (target == null) {
            return;
        }

        Double amount = parseAmount(sender, args[3], false);
        if (amount == null) {
            return;
        }

        String currency = args[2].toLowerCase();
        if (!economy.removeBalance(target.getUniqueId(), currency, amount)) {
            sender.sendMessage("§cNão foi possível remover: saldo insuficiente de §f" + target.getName()
                    + " §cem " + currency + " (ou erro no banco).");
            return;
        }

        sender.sendMessage("§7Você removeu §c" + amount + " " + currency + " §7de §f" + target.getName() + "§7.");
        target.sendMessage("§7Foram removidos §c" + amount + " " + currency + " §7do seu saldo.");
    }

    // /saldo setar <jogador> <moeda> <valor>   (aceita 0)
    private void set(CommandSender sender, String[] args) {
        Player target = prepare(sender, args, "/saldo setar <jogador> <moeda> <valor>");
        if (target == null) {
            return;
        }

        Double amount = parseAmount(sender, args[3], true);
        if (amount == null) {
            return;
        }

        String currency = args[2].toLowerCase();
        if (!economy.setBalance(target.getUniqueId(), currency, amount)) {
            sender.sendMessage("§cNão foi possível definir o saldo (veja o console).");
            return;
        }

        sender.sendMessage("§7Saldo de §f" + target.getName() + " §7em " + currency + " definido para §a" + amount + "§7.");
        target.sendMessage("§7Seu saldo em " + currency + " agora é §a" + amount + "§7.");
    }

    // Confere permissão, quantidade de argumentos e acha o jogador.
    // Devolve o jogador, ou null (depois de avisar) se algo estiver errado.
    private Player prepare(CommandSender sender, String[] args, String usage) {
        if (!sender.hasPermission("aeconomy.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return null;
        }

        if (args.length != 4) {
            sender.sendMessage("§cUse: " + usage);
            return null;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado ou offline: " + args[1]);
            return null;
        }
        return target;
    }

    // Converte o texto em número. Devolve null (depois de avisar) se for inválido.
    private Double parseAmount(CommandSender sender, String text, boolean allowZero) {
        double amount;
        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cO valor precisa ser um número. Use ponto para decimais: 12.5");
            return null;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage("§cValor inválido.");
            return null;
        }

        if (amount < 0 || (amount == 0 && !allowZero)) {
            sender.sendMessage(allowZero
                    ? "§cO valor não pode ser negativo."
                    : "§cO valor precisa ser maior que zero.");
            return null;
        }
        return amount;
    }
}