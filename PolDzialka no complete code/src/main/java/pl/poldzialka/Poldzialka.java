package pl.poldzialka;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.poldzialka.commands.DzialkaCommand;
import pl.poldzialka.config.ConfigManager;
import pl.poldzialka.config.MessageManager;
import pl.poldzialka.gui.PlotMenu;
import pl.poldzialka.listeners.MenuClickListener;
import pl.poldzialka.listeners.PlayerChatListener;
import pl.poldzialka.listeners.PlotBreakListener;
import pl.poldzialka.listeners.PlotPlaceListener;
import pl.poldzialka.listeners.PlayerInteractListener;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.managers.WorldGuardManager;
import pl.poldzialka.storage.PlotStorage;

/**
 * Główna klasa pluginu poldzialka
 * Advanced plugin dla zarządzania działkami z integracją WorldGuard 1.21
 */
public final class Poldzialka extends JavaPlugin {
    private static Poldzialka instance;
    private PlotManager plotManager;
    private PlotMenu plotMenu;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlotStorage plotStorage;
    private WorldGuardManager worldGuardManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!isDependencyPresent("WorldGuard") || !isDependencyPresent("WorldEdit")) {
            getLogger().severe("WorldGuard i WorldEdit są wymagane do działania pluginu poldzialka.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        saveResource("messages_pl.yml", false);
        saveResource("messages_en.yml", false);
        saveResource("dzialki.yml", false);

        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this, configManager.getLanguage());
        this.worldGuardManager = new WorldGuardManager(this);
        this.plotStorage = new PlotStorage(this);
        this.plotManager = new PlotManager(this, configManager, messageManager, plotStorage, worldGuardManager);
        this.plotMenu = new PlotMenu(plotManager, worldGuardManager, messageManager);

        // Rejestruj listenery
        Bukkit.getPluginManager().registerEvents(new PlotPlaceListener(plotManager, messageManager, worldGuardManager), this);
        Bukkit.getPluginManager().registerEvents(new PlotBreakListener(plotManager, messageManager), this);
        Bukkit.getPluginManager().registerEvents(new MenuClickListener(plotManager, plotMenu, messageManager, worldGuardManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(plotManager, messageManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(plotManager, plotMenu, messageManager), this);

        // Rejestruj komendy
        DzialkaCommand dzialkaCmd = new DzialkaCommand(plotManager, plotMenu, messageManager);
        getCommand("dzialka").setExecutor(dzialkaCmd);
        getCommand("dzialka").setTabCompleter(new pl.poldzialka.commands.DzialkaTabCompleter(plotManager));

        getLogger().info("Plugin poldzialka (v1.22) został włączony!");
    }

    @Override
    public void onDisable() {
        if (plotStorage != null) {
            plotStorage.save();
        }
        getLogger().info("Plugin poldzialka został wyłączony!");
    }

    private boolean isDependencyPresent(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        return plugin != null;
    }

    public static Poldzialka getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }
}
