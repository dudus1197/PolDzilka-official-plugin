package com.top1.epsilon121.listeners;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.top1.epsilon121.managers.PlotManager;

public class PlotBlockListener implements Listener {
    private final PlotManager plotManager;

    public PlotBlockListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!plotManager.isPlotBlockItem(item)) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (!plotManager.canCreatePlotAt(block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Ta działka znajduje się zbyt blisko innej działki!");
            event.setCancelled(true);
            return;
        }

        Optional<ProtectedRegion> plotRegion = plotManager.createPlotRegion(player, block);
        if (plotRegion.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nie udało się utworzyć działki. Spróbuj ponownie później.");
            event.setCancelled(true);
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Działka utworzona! Region: " + ChatColor.AQUA + plotRegion.get().getId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        Optional<ProtectedRegion> maybePlot = plotManager.getPlotRegionByCenter(block);
        if (maybePlot.isEmpty()) {
            return;
        }

        ProtectedRegion plotRegion = maybePlot.get();
        if (!plotManager.removePlotRegion(block.getWorld(), plotRegion)) {
            event.getPlayer().sendMessage(ChatColor.RED + "Nie udało się usunąć regionu działki.");
            return;
        }

        ItemStack plotItem = plotManager.createPlotItem();
        if (event.getPlayer().getInventory().addItem(plotItem).isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "Działka usunięta. Blok działki został dodany do twojego ekwipunku.");
        } else {
            event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), plotItem);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Działka usunięta. Ekwipunek był pełny, więc blok działki został upuszczony obok.");
        }
    }
}
