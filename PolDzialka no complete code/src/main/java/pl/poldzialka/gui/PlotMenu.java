package pl.poldzialka.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import pl.poldzialka.config.MessageManager;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.managers.WorldGuardManager;
import pl.poldzialka.model.PlotData;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

/**
 * System menu GUI dla działek
 * Obsługuje wszystkie interfejsy zarządzania działkami
 */
public class PlotMenu {
    private static final String FLAG_KEY_NAME = "plot-flag-key";

    private final MessageManager messages;
    private final PlotManager plotManager;
    private final WorldGuardManager worldGuardManager;
    private final NamespacedKey flagKey;

    public PlotMenu(PlotManager plotManager, WorldGuardManager worldGuardManager, MessageManager messages) {
        this.plotManager = plotManager;
        this.worldGuardManager = worldGuardManager;
        this.messages = messages;
        this.flagKey = new NamespacedKey("poldzialka", FLAG_KEY_NAME);
    }

    /**
     * Otwiera główne menu działki (54 sloty, 6 rzędów)
     */
    public void openMainMenu(Player player, PlotData plot) {
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.MAIN), 54, messages.getComponent("menu-main-title"));
        
        // Przezroczyste oprawy (szklane panele)
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createGlassPane());
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createGlassPane());
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, createGlassPane());
            inventory.setItem(i + 8, createGlassPane());
        }
        
        // Slot 11 - Zarządzaj członkami
        inventory.setItem(11, createMenuIcon(Material.PLAYER_HEAD, "menu-manage-members", List.of(
            messages.getComponent("menu-manage-members-desc")
        )));
        
        // Slot 13 - Ustawienia flag
        if (plot.isOwner(player.getUniqueId())) {
            inventory.setItem(13, createMenuIcon(Material.STONE_BUTTON, "menu-manage-flags", List.of(
                messages.getComponent("menu-manage-flags-desc")
            )));
        } else {
            inventory.setItem(13, createMenuIcon(Material.BARRIER, "menu-manage-flags-locked", List.of(
                messages.getComponent("menu-manage-flags-locked-desc")
            )));
        }
        
        // Slot 15 - Informacje
        inventory.setItem(15, createMenuIcon(Material.BOOK, "menu-plot-info", List.of(
            messages.getComponent("menu-plot-info-desc")
        )));
        
        // Slot 31 - Usuń działkę
        inventory.setItem(31, createMenuIcon(Material.BARRIER, "menu-delete-plot", List.of(
            messages.getComponent("menu-delete-plot-desc")
        )));

        player.openInventory(inventory);
    }

    /**
     * Otwiera menu zarządzania flagami
     */
    public void openFlagsMenu(Player player, PlotData plot) {
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.FLAGS), 54, messages.getComponent("menu-flags-title"));
        
        // Przezroczyste oprawy
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createGlassPane());
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createGlassPane());
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, createGlassPane());
            inventory.setItem(i + 8, createGlassPane());
        }

        var flags = worldGuardManager.getAvailableStateFlags();
        int slot = 10;
        for (StateFlag flag : flags) {
            if (slot >= 44) {
                break;
            }
            inventory.setItem(slot, createStateFlagIcon(flag, plot));
            slot++;
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
        }

        inventory.setItem(49, createMenuIcon(Material.ARROW, "menu-back", List.of(
            messages.getComponent("menu-back-desc")
        )));
        player.openInventory(inventory);
    }

    /**
     * Otwiera menu zarządzania członkami
     */
    public void openMembersMenu(Player player, PlotData plot) {
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.MEMBERS), 27, messages.getComponent("menu-manage-members"));
        
        // Zielona wełna - Dodaj
        inventory.setItem(11, createMenuIcon(Material.LIME_WOOL, "menu-add-member", List.of(
            messages.getComponent("click-to-add-member")
        )));
        
        // Czerwona wełna - Usuń
        inventory.setItem(15, createMenuIcon(Material.RED_WOOL, "menu-remove-member", List.of(
            messages.getComponent("click-to-remove-member")
        )));

        player.openInventory(inventory);
    }

    /**
     * Otwiera menu listy członków do usunięcia
     */
    public void openMemberListMenu(Player player, PlotData plot) {
        int size = Math.max(27, ((plot.getMembers().size() / 9) + 1) * 9);
        if (size > 54) size = 54;
        
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.MEMBER_LIST), size, messages.getComponent("menu-member-list-title"));
        
        List<String> members = plot.getMembers();
        if (members.isEmpty()) {
            inventory.setItem(13, createMenuIcon(Material.BARRIER, "no-members-text", List.of(messages.getComponent("no-members"))));
        } else {
            for (int i = 0; i < members.size() && i < size - 9; i++) {
                String member = members.get(i);
                inventory.setItem(i, createMemberSkull(member));
            }
        }

        player.openInventory(inventory);
    }

    /**
     * Otwiera menu informacji o działce
     */
    public void openInfoMenu(Player player, PlotData plot) {
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.INFO), 27, messages.getComponent("menu-info-title"));
        
        List<Component> infoLines = new ArrayList<>();
        infoLines.add(Component.text("Nazwa działki: ", NamedTextColor.YELLOW).append(Component.text(plot.getPlotName(), NamedTextColor.WHITE)));
        infoLines.add(Component.text("Region: ", NamedTextColor.YELLOW).append(Component.text(plot.getRegionName(), NamedTextColor.WHITE)));
        infoLines.add(Component.text("Właściciel: ", NamedTextColor.YELLOW).append(Component.text(plot.getOwnerName(), NamedTextColor.WHITE)));
        infoLines.add(Component.text("Rozmiar: ", NamedTextColor.YELLOW).append(Component.text(plot.getSize() + "x" + plot.getSize(), NamedTextColor.WHITE)));
        infoLines.add(Component.text("Współrzędne: ", NamedTextColor.YELLOW).append(Component.text(plot.getCenterX() + ", " + plot.getCenterY() + ", " + plot.getCenterZ(), NamedTextColor.WHITE)));
        infoLines.add(Component.empty());
        infoLines.add(Component.text("Członkowie: " + plot.getMembers().size(), NamedTextColor.GOLD));
        
        ItemStack infoItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.getComponent("menu-info-title").decorate(TextDecoration.BOLD));
            meta.lore(infoLines);
            infoItem.setItemMeta(meta);
        }
        
        inventory.setItem(13, infoItem);
        player.openInventory(inventory);
    }

    /**
     * Otwiera menu potwierdzenia usunięcia działki
     */
    public void openDeleteConfirmMenu(Player player, PlotData plot) {
        Inventory inventory = Bukkit.createInventory(new PlotInventoryHolder(plot, MenuType.CONFIRM_DELETE), 27, messages.getComponent("menu-delete-confirm-title"));
        
        // Zielona wełna - Tak, usuń
        inventory.setItem(11, createMenuIcon(Material.LIME_WOOL, "menu-confirm-yes", List.of(
            messages.getComponent("click-to-confirm-delete")
        )));
        
        // Czerwona wełna - Nie, anuluj
        inventory.setItem(15, createMenuIcon(Material.RED_WOOL, "menu-confirm-no", List.of(
            messages.getComponent("click-to-cancel")
        )));

        player.openInventory(inventory);
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            glass.setItemMeta(meta);
        }
        return glass;
    }

    private ItemStack createMenuIcon(Material material, String nameKey, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.getComponent(nameKey).decorate(TextDecoration.BOLD));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createStateFlagIcon(StateFlag flag, PlotData plot) {
        State currentState = plotManager.getFlagState(plot, flag);
        
        // POPRAWKA: Bezpieczna walidacja wartości null zapobiegająca NullPointerException na 1.21
        Material material;
        if (currentState == null) {
            material = Material.GRAY_WOOL;
        } else {
            material = switch (currentState) {
                case ALLOW -> Material.LIME_WOOL;
                case DENY -> Material.RED_WOOL;
            };
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = flag.getName().replace('-', ' ').toUpperCase();
            List<Component> lore = new ArrayList<>();
            
            String stateName = (currentState != null) ? currentState.name() : "UNSET";
            lore.add(Component.text("Stan: " + stateName, NamedTextColor.GRAY));
            lore.add(Component.text("Kliknij aby przełączyć", NamedTextColor.YELLOW));
            
            meta.displayName(Component.text(displayName, NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(flagKey, PersistentDataType.STRING, flag.getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMemberSkull(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(playerName, NamedTextColor.GOLD));
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta.lore(List.of(
                Component.text("Kliknij aby usunąć", NamedTextColor.RED)
            ));
            skull.setItemMeta(meta);
        }
        return skull;
    }
}