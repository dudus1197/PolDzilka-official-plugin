package pl.poldzialka.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import pl.poldzialka.config.MessageManager;
import pl.poldzialka.gui.MenuType;
import pl.poldzialka.gui.PlotInventoryHolder;
import pl.poldzialka.gui.PlotMenu;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.managers.WorldGuardManager;
import pl.poldzialka.model.PlotData;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

public class MenuClickListener implements Listener {
    private static final NamespacedKey FLAG_KEY = new NamespacedKey("poldzialka", "plot-flag-key");

    private final PlotManager plotManager;
    private final PlotMenu plotMenu;
    private final MessageManager messages;
    private final WorldGuardManager worldGuardManager;

    public MenuClickListener(PlotManager plotManager, PlotMenu plotMenu, MessageManager messages, WorldGuardManager worldGuardManager) {
        this.plotManager = plotManager;
        this.plotMenu = plotMenu;
        this.messages = messages;
        this.worldGuardManager = worldGuardManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof PlotInventoryHolder holder)) {
            return;
        }

        event.setCancelled(true);
        PlotData plot = holder.getPlot();
        if (plot == null) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        switch (holder.getType()) {
            case MAIN -> handleMainMenuClick(player, plot, clicked);
            case MEMBERS -> handleMemberActionClick(player, plot, clicked);
            case MEMBER_LIST -> handleMemberListClick(player, plot, clicked);
            case CONFIRM_DELETE -> handleConfirmDeleteClick(player, plot, clicked);
            case FLAGS -> handleFlagsMenuClick(player, plot, clicked);
            default -> {}
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof PlotInventoryHolder holder) {
            // no-op for now
        }
    }

    private void handleMainMenuClick(Player player, PlotData plot, ItemStack clicked) {
        String display = LegacyComponentSerializer.legacyAmpersand().serialize(clicked.getItemMeta().displayName());
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-manage-members")))) {
            plotMenu.openMembersMenu(player, plot);
            return;
        }
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-manage-flags"))) ||
            display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-manage-flags-locked")))) {
            if (!plot.isOwner(player.getUniqueId())) {
                player.sendMessage(messages.getComponent("plot-only-owner-flags"));
                return;
            }
            plotMenu.openFlagsMenu(player, plot);
            return;
        }
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-plot-info")))) {
            plotMenu.openInfoMenu(player, plot);
            return;
        }
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-delete-plot")))) {
            plotMenu.openDeleteConfirmMenu(player, plot);
            return;
        }
    }

    private void handleMemberActionClick(Player player, PlotData plot, ItemStack clicked) {
        String display = LegacyComponentSerializer.legacyAmpersand().serialize(clicked.getItemMeta().displayName());
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-add-member")))) {
            player.closeInventory();
            plotManager.beginMemberInput(player, plot);
            player.sendMessage(messages.getComponent("plot-add-member-prompt"));
            return;
        }
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-remove-member")))) {
            plotMenu.openMemberListMenu(player, plot);
            return;
        }
    }

    private void handleMemberListClick(Player player, PlotData plot, ItemStack clicked) {
        String display = LegacyComponentSerializer.legacyAmpersand().serialize(clicked.getItemMeta().displayName());
        for (String member : plot.getMembers()) {
            if (display.contains(member)) {
                if (plotManager.removeMember(plot, member)) {
                    player.sendMessage(messages.formatComponent("plot-removed-member", "member", member));
                    plotMenu.openMemberListMenu(player, plot);
                }
                return;
            }
        }
    }

    private void handleConfirmDeleteClick(Player player, PlotData plot, ItemStack clicked) {
        String display = LegacyComponentSerializer.legacyAmpersand().serialize(clicked.getItemMeta().displayName());
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-confirm-yes")))) {
            plotManager.removePlot(plot);
            player.getInventory().addItem(plotManager.createPlotItem(plot.getSize()));
            player.closeInventory();
            player.sendMessage(messages.getComponent("plot-deleted"));
            return;
        }
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-confirm-no")))) {
            plotMenu.openMainMenu(player, plot);
        }
    }

    private void handleFlagsMenuClick(Player player, PlotData plot, ItemStack clicked) {
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        String flagName = meta.getPersistentDataContainer().get(FLAG_KEY, PersistentDataType.STRING);
        if (flagName != null) {
            var flag = worldGuardManager.getStateFlagByName(flagName);
            if (flag == null) {
                player.sendMessage(messages.formatComponent("plot-flag-unsupported", "flag", flagName));
                return;
            }
            if (plotManager.togglePlotFlag(plot, flag)) {
                State nextState = plotManager.getFlagState(plot, flag);
                player.sendMessage(messages.formatComponent("plot-flag-updated", "flag", flag.getName(), "state", nextState != null ? nextState.name() : "UNSET"));
            } else {
                player.sendMessage(messages.getComponent("plot-flag-error"));
            }
            plotMenu.openFlagsMenu(player, plot);
            return;
        }

        String display = LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName());
        if (display.contains(LegacyComponentSerializer.legacyAmpersand().serialize(messages.getComponent("menu-back")))) {
            plotMenu.openMainMenu(player, plot);
        }
    }
}
