package pl.poldzialka.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pl.poldzialka.config.MessageManager;
import pl.poldzialka.gui.PlotMenu;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.model.PlotData;

/**
 * Listener dla interakcji gracza z blokami działki
 * Obsługuje prawy klik na blok środkowy działki
 */
public class PlayerInteractListener implements Listener {
    private final PlotManager plotManager;
    private final PlotMenu plotMenu;
    private final MessageManager messageManager;

    public PlayerInteractListener(PlotManager plotManager, PlotMenu plotMenu, MessageManager messageManager) {
        this.plotManager = plotManager;
        this.plotMenu = plotMenu;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Sprawdź czy to blok działki
        PlotData plot = plotManager.getPlotAtLocation(block.getLocation());
        if (plot == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Sprawdź uprawnienia
        if (!plot.isMember(player.getUniqueId(), player.getName())) {
            player.sendMessage(messageManager.getComponent("no-permission-plot"));
            return;
        }

        // Otwórz GUI
        plotMenu.openMainMenu(player, plot);
    }
}
