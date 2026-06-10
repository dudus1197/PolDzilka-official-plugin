package pl.poldzialka.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pl.poldzialka.managers.PlotManager;

public class DzialkaTabCompleter implements TabCompleter {
    private final PlotManager plotManager;

    public DzialkaTabCompleter(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("version");
            completions.add("daj");
            completions.add("dajpermisje");
            completions.add("nadajpermisje");
            return completions.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("daj")) {
                // podpowiedź rozmiarów
                return plotManager.getConfig().getAllowedSizes().stream().map(Object::toString).collect(Collectors.toList());
            }
            if (sub.equals("dajpermisje") || sub.equals("nadajpermisje")) {
                List<String> opts = new ArrayList<>();
                opts.add("all");
                // lista online nicków
                opts.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                return opts.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }
        return completions;
    }
}
