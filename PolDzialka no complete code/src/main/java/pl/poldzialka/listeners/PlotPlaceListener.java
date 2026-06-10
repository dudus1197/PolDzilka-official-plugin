package pl.poldzialka.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import pl.poldzialka.config.MessageManager;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.managers.WorldGuardManager;
import pl.poldzialka.model.PlotData;

/**
 * Listener dla umieszczania bloków działek
 * Obsługuje tworzenie nowych działek
 */
public class PlotPlaceListener implements Listener {
    private final PlotManager plotManager;
    private final MessageManager messages;
    private final WorldGuardManager worldGuardManager;

    public PlotPlaceListener(PlotManager plotManager, MessageManager messages, WorldGuardManager worldGuardManager) {
        this.plotManager = plotManager;
        this.messages = messages;
        this.worldGuardManager = worldGuardManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        // Dodatkowe zabezpieczenie: tylko gracze z uprawnieniem mogą postawić działkę
        if (!player.hasPermission("poldzialka.place") && !player.hasPermission("poldzialka.use")) {
            player.sendMessage(messages.getComponent("plot-no-permission"));
            event.setCancelled(true);
            return;
        }

        if (!plotManager.isValidPlotItem(item) || !plotManager.isPlotBlock(block.getType())) {
            return;
        }

        int size = plotManager.getPlotSize(item);
        if (size <= 0 || !plotManager.isAllowedSize(size)) {
            player.sendMessage(messages.getComponent("plot-invalid-size"));
            event.setCancelled(true);
            return;
        }

        // Sprawdź czy blok nie unosi się w powietrzu
        if (block.getRelative(0, -1, 0).getType() == Material.AIR) {
            player.sendMessage(messages.getComponent("plot-wrong-location"));
            event.setCancelled(true);
            return;
        }

        // Sprawdź czy nie ma kolizji z innymi działkami
        if (worldGuardManager.checkOverlap(block.getWorld(), block.getLocation(), size)) {
            player.sendMessage(messages.getComponent("plot-too-close"));
            event.setCancelled(true);
            return;
        }

        // Nazwa działki może pochodzić z nazwy przedmiotu działki
        String customName = null;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            customName = LegacyComponentSerializer.legacyAmpersand().serialize(item.getItemMeta().displayName());
        }

        // Stwórz działkę
        if (!plotManager.canCreatePlot(player)) {
            player.sendMessage(messages.getComponent("plot-max-reached"));
            event.setCancelled(true);
            return;
        }

        PlotData plot = plotManager.createPlot(player, block.getLocation(), size, customName);
        if (plot == null) {
            player.sendMessage(messages.getComponent("plot-error"));
            event.setCancelled(true);
            return;
        }

        player.sendMessage(messages.formatComponent("plot-created", "region", plot.getRegionName()));
    }
}
