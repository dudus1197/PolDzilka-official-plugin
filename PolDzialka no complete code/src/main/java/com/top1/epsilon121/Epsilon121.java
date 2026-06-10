package com.top1.epsilon121;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.top1.epsilon121.commands.DzialkaCommand;
import com.top1.epsilon121.listeners.MenuClickListener;
import com.top1.epsilon121.listeners.PlotBlockListener;
import com.top1.epsilon121.managers.PlotManager;
import com.top1.epsilon121.menu.PlotMenu;

public final class Epsilon121 extends JavaPlugin {
    private PlotManager plotManager;
    private PlotMenu plotMenu;

    @Override
    public void onEnable() {
        // Wyświetlanie napisu z pliku obraz.png w konsoli serwera
        printAsciiArt();

        // Sprawdzanie wymaganych zależności (WorldGuard i WorldEdit)
        if (!isDependencyPresent("WorldGuard") || !isDependencyPresent("WorldEdit")) {
            getLogger().severe("WorldGuard i WorldEdit sa wymagane do dzialania pluginu dzialek.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Inicjalizacja i zapisywanie domyślnych plików konfiguracyjnych z resources
        saveDefaultConfig();
        saveResource("messages_pl.yml", false);
        saveResource("messages_en.yml", false);

        // Inicjalizacja obiektów zarządzających
        this.plotManager = new PlotManager(this);
        this.plotMenu = new PlotMenu(this, plotManager);

        // Rejestracja listenerów (zdarzeń)
        Bukkit.getPluginManager().registerEvents(new PlotBlockListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MenuClickListener(plotManager, plotMenu), this);
        
        // Rejestracja komendy głównej
        getCommand("dzialka").setExecutor(new DzialkaCommand(plotManager, plotMenu));

        getLogger().info("Plugin poldzialka został pomyślnie włączony. Upewnij sie ze masz WorldEdit i WorldGuard LuckPerms | opcjonalnie: DecentHolograms");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin poldzialka został wyłączony.");
    }

    private boolean isDependencyPresent(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    } // <-- Tutaj był błąd, usunąłem nadmiarowe );

    // Zakładam, że ta metoda jest gdzieś niżej w Twoim kodzie, 
    // skoro wywołujesz ją w onEnable()
    private void printAsciiArt() {
        // Twoja logika wyświetlania logo/artu
    }
}