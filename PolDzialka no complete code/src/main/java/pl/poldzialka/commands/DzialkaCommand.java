package pl.poldzialka.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import pl.poldzialka.Poldzialka;
import pl.poldzialka.config.MessageManager;
import pl.poldzialka.gui.PlotMenu;
import pl.poldzialka.managers.PlotManager;

public class DzialkaCommand implements CommandExecutor {
    private final PlotManager plotManager;
    private final PlotMenu plotMenu;
    private final MessageManager messages;

    public DzialkaCommand(PlotManager plotManager, PlotMenu plotMenu, MessageManager messages) {
        this.plotManager = plotManager;
        this.plotMenu = plotMenu;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Ta komenda jest dostępna tylko dla graczy."));
            return true;
        }

        if (args.length == 0) {
            // Otwórz menu jeśli gracz stoi na swojej działce
            var plot = plotManager.getPlotAtLocation(player.getLocation());
            if (plot == null) {
                player.sendMessage(messages.getComponent("plot-not-on-own-plot"));
                return true;
            }
            if (!plot.isMember(player.getUniqueId(), player.getName())) {
                player.sendMessage(messages.getComponent("no-permission-plot"));
                return true;
            }
            plotMenu.openMainMenu(player, plot);
            return true;
        }

        // /dzialka daj <rozmiar>
        if (args.length == 2 && args[0].equalsIgnoreCase("daj")) {
            if (!player.hasPermission("poldzialka.daj")) {
                player.sendMessage(messages.getComponent("plot-no-permission"));
                return true;
            }
            try {
                int size = Integer.parseInt(args[1]);
                if (!plotManager.isAllowedSize(size)) {
                    player.sendMessage(messages.getComponent("plot-invalid-size"));
                    return true;
                }
                player.getInventory().addItem(plotManager.createPlotItem(size));
                player.sendMessage(messages.formatComponent("plot-gave-item", "size", String.valueOf(size)));
            } catch (NumberFormatException ex) {
                player.sendMessage(messages.getComponent("plot-invalid-size"));
            }
            return true;
        }
        // /dzialka dajpermisje <all|nick>
        if (args.length == 2 && (args[0].equalsIgnoreCase("dajpermisje") || args[0].equalsIgnoreCase("dajpermisjie") || args[0].equalsIgnoreCase("nadajpermisje"))) {
            if (!player.hasPermission("poldzialka.dajpermisje")) {
                player.sendMessage(messages.getComponent("plot-no-permission"));
                return true;
            }

            String target = args[1];
            boolean hasLP = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;

            if (target.equalsIgnoreCase("all")) {
                if (!hasLP) {
                    player.sendMessage(Component.text("LuckPerms nie jest zainstalowany. Nie można ustawić uprawnień dla wszystkich."));
                    return true;
                }
                String cmd = "lp group default permission set poldzialka.use true";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                player.sendMessage(messages.formatComponent("plot-permissions-applied", "player", "all"));
                return true;
            } else {
                String nick = target;
                if (!hasLP) {
                    // Fallback: jeśli LuckPerms nie ma, spróbujemy wykonać prostą komendę "permissions" przez konsolę
                    player.sendMessage(Component.text("Brak LuckPerms. Proszę nadać uprawnienia ręcznie lub zainstalować LuckPerms."));
                    return true;
                }
                String cmd = String.format("lp user %s permission set poldzialka.use true", nick);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                player.sendMessage(messages.formatComponent("plot-permissions-applied", "player", nick));
                return true;
            }
        }

        // /dzialka help
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(Component.text("------ /dzialka help ------"));
            player.sendMessage(Component.text("/dzialka - otwórz menu działki (jeśli stoisz na swojej działce)"));
            player.sendMessage(Component.text("/dzialka daj <rozmiar> - daj przedmiot działki"));
            player.sendMessage(Component.text("/dzialka dajpermisje <all|nick> - nadaj uprawnienia (wymaga LuckPerms)"));
            player.sendMessage(Component.text("/dzialka version - pokaż wersję pluginu"));
            return true;
        }

        // /dzialka version
        if (args.length >= 1 && args[0].equalsIgnoreCase("version")) {
            String version = "unknown";
            try {
                if (Poldzialka.getInstance() != null && Poldzialka.getInstance().getDescription() != null) {
                    version = Poldzialka.getInstance().getDescription().getVersion();
                }
            } catch (Exception ignored) {}
            player.sendMessage(Component.text("poldzialka version: " + version));
            return true;
        }

        sender.sendMessage(Component.text("Użycie: /dzialka [help|version|daj <rozmiar>|dajpermisje <all|nick>]."));
        return true;
    }
}
