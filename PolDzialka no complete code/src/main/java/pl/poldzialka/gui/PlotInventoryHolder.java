package pl.poldzialka.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import pl.poldzialka.model.PlotData;

public class PlotInventoryHolder implements InventoryHolder {
    private final PlotData plot;
    private final MenuType type;

    public PlotInventoryHolder(PlotData plot, MenuType type) {
        this.plot = plot;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public PlotData getPlot() {
        return plot;
    }

    public MenuType getType() {
        return type;
    }
}
