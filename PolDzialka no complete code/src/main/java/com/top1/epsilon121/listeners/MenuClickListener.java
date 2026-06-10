package com.top1.epsilon121.listeners;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.top1.epsilon121.managers.PlotManager;
import com.top1.epsilon121.menu.PlotMenu;

import net.kyori.adventure.text.Component;

public class MenuClickListener implements Listener {
    private final PlotManager plotManager;
    private final PlotMenu plotMenu;

    public MenuClickListener(PlotManager plotManager, PlotMenu plotMenu) {
        this.plotManager = plotManager;
        this.plotMenu = plotMenu;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        Component title = event.getView().title();
        if (title == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        if (current == null || !current.hasItemMeta()) {
            return;
        }

        Component displayName = current.getItemMeta().displayName();
        if (displayName == null) {
            return;
        }

        if (title.equals(PlotMenu.MAIN_MENU_TITLE)) {
            event.setCancelled(true);
            handleMainMenuClick(player, displayName);
            return;
        }

        if (title.equals(PlotMenu.MEMBER_MENU_TITLE)) {
            event.setCancelled(true);
            handleMemberMenuClick(player, displayName);
            return;
        }

        if (title.equals(PlotMenu.INFO_MENU_TITLE)) {
            event.setCancelled(true);
            handleInfoMenuClick(player, displayName);
            return;
        }

        if (title.equals(PlotMenu.CONFIRM_MENU_TITLE)) {
            event.setCancelled(true);
            handleConfirmMenuClick(player, current);
        }
    }

    private void handleMainMenuClick(Player player, Component displayName) {
        Optional<ProtectedRegion> maybeRegion = plotManager.getPlotRegionAt(player.getLocation());
        if (maybeRegion.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono twojej działki. Stań na działce i otwórz menu ponownie.");
            player.closeInventory();
            return;
        }

        ProtectedRegion region = maybeRegion.get();
        if (displayName.equals(PlotMenu.MANAGE_MEMBERS_NAME)) {
            plotMenu.openMemberMenu(player, region);
            return;
        }

        if (displayName.equals(PlotMenu.INFO_NAME)) {
            plotMenu.openInfoMenu(player, region);
            return;
        }

        if (displayName.equals(PlotMenu.DELETE_NAME)) {
            plotMenu.openConfirmRemoveMenu(player, region);
        }
    }

    private void handleMemberMenuClick(Player player, Component displayName) {
        if (displayName.equals(PlotMenu.ADD_MEMBER_NAME)) {
            player.sendMessage(ChatColor.GREEN + "Użyj teraz: /rg addmember <region> <nick>");
            player.sendMessage(ChatColor.GRAY + "Region zostanie wyświetlony w opisie, gdy otworzysz ponownie menu." );
            return;
        }

        if (displayName.equals(PlotMenu.REMOVE_MEMBER_NAME)) {
            player.sendMessage(ChatColor.GREEN + "Użyj teraz: /rg removemember <region> <nick>");
            player.sendMessage(ChatColor.GRAY + "Region zostanie wyświetlony w opisie, gdy otworzysz ponownie menu.");
            return;
        }

        if (displayName.equals(PlotMenu.BACK_NAME)) {
            Optional<ProtectedRegion> maybeRegion = plotManager.getPlotRegionAt(player.getLocation());
            if (maybeRegion.isPresent()) {
                plotMenu.openMainMenu(player, maybeRegion.get());
                return;
            }
            player.closeInventory();
        }
    }

    private void handleInfoMenuClick(Player player, Component displayName) {
        if (displayName.equals(PlotMenu.BACK_NAME)) {
            Optional<ProtectedRegion> maybeRegion = plotManager.getPlotRegionAt(player.getLocation());
            if (maybeRegion.isPresent()) {
                plotMenu.openMainMenu(player, maybeRegion.get());
            } else {
                player.closeInventory();
            }
        }
    }

    private void handleConfirmMenuClick(Player player, ItemStack clickedItem) {
        Optional<String> maybeRegionId = plotMenu.getRegionIdFromItem(clickedItem);
        if (maybeRegionId.isEmpty()) {
            if (clickedItem.getItemMeta() != null
                    && clickedItem.getItemMeta().displayName() != null
                    && clickedItem.getItemMeta().displayName().equals(PlotMenu.NO_NAME)) {
                Optional<ProtectedRegion> maybeRegion = plotManager.getPlotRegionAt(player.getLocation());
                maybeRegion.ifPresent(region -> plotMenu.openMainMenu(player, region));
            }
            return;
        }

        String regionId = maybeRegionId.get();
        Optional<ProtectedRegion> maybeRegion = plotManager.getPlotRegionById(player.getWorld(), regionId);
        if (maybeRegion.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono regionu działki. Możliwe, że już został usunięty.");
            player.closeInventory();
            return;
        }

        ProtectedRegion region = maybeRegion.get();
        if (!plotManager.removePlotRegion(player.getWorld(), region)) {
            player.sendMessage(ChatColor.RED + "Nie udało się usunąć działki.");
            player.closeInventory();
            return;
        }

        if (player.getInventory().addItem(plotManager.createPlotItem()).isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "Działka została usunięta. Otrzymałeś blok działki z powrotem.");
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), plotManager.createPlotItem());
            player.sendMessage(ChatColor.GREEN + "Działka została usunięta. Ekwipunek był pełny, więc blok działki został upuszczony obok.");
        }
        player.closeInventory();
    }
}
