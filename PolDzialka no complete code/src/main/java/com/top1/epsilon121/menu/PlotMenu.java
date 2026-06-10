package com.top1.epsilon121.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.top1.epsilon121.managers.PlotManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlotMenu {
    public static final Component MAIN_MENU_TITLE = Component.text("Menu działki", NamedTextColor.DARK_GREEN);
    public static final Component MEMBER_MENU_TITLE = Component.text("Zarządzaj członkami", NamedTextColor.GREEN);
    public static final Component INFO_MENU_TITLE = Component.text("Informacje o działce", NamedTextColor.GOLD);
    public static final Component CONFIRM_MENU_TITLE = Component.text("Potwierdź usunięcie", NamedTextColor.RED);
    public static final Component MANAGE_MEMBERS_NAME = Component.text("Zarządzaj członkami", NamedTextColor.GREEN);
    public static final Component INFO_NAME = Component.text("Informacje o działce", NamedTextColor.GOLD);
    public static final Component DELETE_NAME = Component.text("Usuń działkę", NamedTextColor.RED);
    public static final Component ADD_MEMBER_NAME = Component.text("Dodaj członka", NamedTextColor.AQUA);
    public static final Component REMOVE_MEMBER_NAME = Component.text("Usuń członka", NamedTextColor.RED);
    public static final Component BACK_NAME = Component.text("Powrót", NamedTextColor.WHITE);
    public static final Component YES_NAME = Component.text("Tak, usuń działkę", NamedTextColor.GREEN);
    public static final Component NO_NAME = Component.text("Nie, wróć", NamedTextColor.RED);

    private final JavaPlugin plugin;
    private final PlotManager plotManager;
    private final NamespacedKey regionKey;

    public PlotMenu(JavaPlugin plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.regionKey = new NamespacedKey(plugin, "plot-region-id");
    }

    public void openMainMenu(Player player, ProtectedRegion region) {
        Inventory inventory = Bukkit.createInventory(null, 9, MAIN_MENU_TITLE);
        inventory.setItem(2, createMenuItem(Material.PLAYER_HEAD, MANAGE_MEMBERS_NAME, List.of(
                Component.text("Kliknij, aby zarządzać członkami działki.", NamedTextColor.GRAY),
                Component.text("Używa /rg addmember i /rg removemember.", NamedTextColor.GRAY))));
        inventory.setItem(4, createMenuItem(Material.BOOK, INFO_NAME, List.of(
                Component.text("Wyświetl informacje o swojej działce.", NamedTextColor.GRAY),
                Component.text("Środek, region i członkowie.", NamedTextColor.GRAY))));
        inventory.setItem(6, createMenuItem(Material.BARRIER, DELETE_NAME, List.of(
                Component.text("Usuń działkę i otrzymaj blok działki z powrotem.", NamedTextColor.GRAY),
                Component.text("Wymaga potwierdzenia.", NamedTextColor.DARK_GRAY))));
        player.openInventory(inventory);
    }

    public void openMemberMenu(Player player, ProtectedRegion region) {
        Inventory inventory = Bukkit.createInventory(null, 9, MEMBER_MENU_TITLE);
        inventory.setItem(2, createMenuItem(Material.PLAYER_HEAD, ADD_MEMBER_NAME, List.of(
                Component.text("Użyj komendy poniżej po kliknięciu:", NamedTextColor.GRAY),
                Component.text("/rg addmember " + region.getId() + " <nick>", NamedTextColor.AQUA))));
        inventory.setItem(6, createMenuItem(Material.BARRIER, REMOVE_MEMBER_NAME, List.of(
                Component.text("Użyj komendy poniżej po kliknięciu:", NamedTextColor.GRAY),
                Component.text("/rg removemember " + region.getId() + " <nick>", NamedTextColor.AQUA))));
        inventory.setItem(8, createMenuItem(Material.ARROW, BACK_NAME, List.of(
                Component.text("Powrót do menu działki.", NamedTextColor.GRAY))));
        player.openInventory(inventory);
    }

    public void openInfoMenu(Player player, ProtectedRegion region) {
        Inventory inventory = Bukkit.createInventory(null, 9, INFO_MENU_TITLE);
        int centerX = (region.getMinimumPoint().getX() + region.getMaximumPoint().getX()) / 2;
        int centerZ = (region.getMinimumPoint().getZ() + region.getMaximumPoint().getZ()) / 2;
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Środek: X=" + centerX + " Z=" + centerZ, NamedTextColor.WHITE));
        lore.add(Component.text("Region: " + region.getId(), NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("Członkowie:", NamedTextColor.YELLOW));

        var members = region.getMembers();
        if (members == null || members.getPlayers().isEmpty()) {
            lore.add(Component.text("Brak członków", NamedTextColor.DARK_GRAY));
        } else {
            members.getPlayers().stream().sorted().forEach(name -> lore.add(Component.text("- " + name, NamedTextColor.GRAY)));
        }

        inventory.setItem(4, createMenuItem(Material.BOOK, INFO_NAME, lore));
        inventory.setItem(8, createMenuItem(Material.ARROW, BACK_NAME, List.of(
                Component.text("Powrót do menu działki.", NamedTextColor.GRAY))));
        player.openInventory(inventory);
    }

    public void openConfirmRemoveMenu(Player player, ProtectedRegion region) {
        Inventory inventory = Bukkit.createInventory(null, 9, CONFIRM_MENU_TITLE);
        ItemStack yes = createMenuItem(Material.GREEN_WOOL, YES_NAME, List.of(
                Component.text("Usuń działkę: " + region.getId(), NamedTextColor.GRAY)));
        ItemMeta yesMeta = yes.getItemMeta();
        if (yesMeta != null) {
            yesMeta.getPersistentDataContainer().set(regionKey, PersistentDataType.STRING, region.getId());
            yes.setItemMeta(yesMeta);
        }

        inventory.setItem(3, yes);
        inventory.setItem(5, createMenuItem(Material.RED_WOOL, NO_NAME, List.of(
                Component.text("Anuluj i wróć do menu.", NamedTextColor.GRAY))));
        player.openInventory(inventory);
    }

    public Optional<String> getRegionIdFromItem(ItemStack item) {
        if (item == null) {
            return Optional.empty();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.getPersistentDataContainer().get(regionKey, PersistentDataType.STRING));
    }

    private ItemStack createMenuItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
