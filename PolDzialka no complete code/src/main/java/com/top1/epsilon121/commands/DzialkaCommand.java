package com.top1.epsilon121.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.top1.epsilon121.managers.PlotManager;
import com.top1.epsilon121.menu.PlotMenu;

public class DzialkaCommand implements CommandExecutor {
    private final PlotManager plotManager;
    private final PlotMenu plotMenu;

    public DzialkaCommand(PlotManager plotManager, PlotMenu plotMenu) {
        this.plotManager = plotManager;
        this.plotMenu = plotMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("daj")) {
            if (!sender.hasPermission("dzialki.admin")) {
                sender.sendMessage(ChatColor.RED + "Nie masz uprawnień do tej komendy.");
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Komenda może być użyta tylko przez gracza.");
                return true;
            }
            player.getInventory().addItem(plotManager.createPlotItem());
            player.sendMessage(ChatColor.GREEN + "Otrzymałeś blok działki 11x11.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Komenda może być użyta tylko przez gracza.");
            return true;
        }

        var maybeRegion = plotManager.getPlotRegionAt(player.getLocation());
        if (maybeRegion.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nie jesteś na swojej działce. Stań na środku działki i spróbuj ponownie.");
            return true;
        }

        plotMenu.openMainMenu(player, maybeRegion.get());
        return true;
    }
}
