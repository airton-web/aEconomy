package io.github.airtonweb.aeconomy;

import io.github.airtonweb.aeconomy.api.Economy;
import io.github.airtonweb.aeconomy.api.MoneyFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TopCommand implements CommandExecutor, TabCompleter {

    private final TopRanking ranking;
    private final Economy economy;

    public TopCommand(TopRanking ranking, Economy economy) {
        this.ranking = ranking;
        this.economy = economy;
    }

    // /moneytop [moeda]
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String currency = args.length > 0 ? args[0].toLowerCase() : "coins";

        List<TopRanking.Entry> top = ranking.getTop(currency);

        sender.sendMessage("§6§lTOP " + TopRanking.SIZE + " §7- §f" + currency);

        if (top.isEmpty()) {
            sender.sendMessage("§7Ninguém no ranking ainda.");
            return true;
        }

        for (int i = 0; i < top.size(); i++) {
            TopRanking.Entry entry = top.get(i);
            sender.sendMessage("§e" + (i + 1) + "º §f" + entry.name()
                    + " §7- §a" + MoneyFormat.format(entry.amount()));
        }
        return true;
    }

    // Tab: sugere as moedas
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        Set<String> options = new TreeSet<>();
        options.add("coins");
        options.addAll(economy.getCurrencies());

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], options, completions);
        return completions;
    }
}