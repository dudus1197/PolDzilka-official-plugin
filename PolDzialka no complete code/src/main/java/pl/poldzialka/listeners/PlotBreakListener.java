package pl.poldzialka.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import pl.poldzialka.config.MessageManager;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.model.PlotData;

public class PlotBreakListener implements Listener {
    private final PlotManager plotManager;
    private final MessageManager messages;

    public PlotBreakListener(PlotManager plotManager, MessageManager messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        PlotData plot = plotManager.getPlotByCenter(block.getLocation());

        if (plot == null) {
            return;
        }

        if (!plot.getOwnerUniqueId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(messages.getComponent("plot-break-not-owner"));
            return;
        }

        event.setCancelled(true);
        plotManager.removePlot(plot);
        block.setType(plotManager.getPlotMaterial());
        player.getInventory().addItem(plotManager.createPlotItem(plot.getSize()));
        player.sendMessage(messages.getComponent("plot-deleted"));
    }
}
