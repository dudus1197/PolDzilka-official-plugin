package pl.poldzialka.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import pl.poldzialka.model.PlotData;

public class PlotStorage {
    private final JavaPlugin plugin;
    private final File dataFile;
    private final FileConfiguration configuration;
    private final Map<String, PlotData> plots = new HashMap<>();

    public PlotStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "dzialki.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("dzialki.yml", false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(dataFile);
        loadPlots();
    }

    private void loadPlots() {
        if (!configuration.contains("plots")) {
            return;
        }
        for (String regionName : configuration.getConfigurationSection("plots").getKeys(false)) {
            String path = "plots." + regionName + ".";
            UUID ownerId = UUID.fromString(configuration.getString(path + "owner"));
            String ownerName = configuration.getString(path + "owner-name");
            String worldName = configuration.getString(path + "world");
            int centerX = configuration.getInt(path + "center-x");
            int centerY = configuration.getInt(path + "center-y");
            int centerZ = configuration.getInt(path + "center-z");
            int size = configuration.getInt(path + "size");
            String plotName = configuration.getString(path + "name", regionName);
            List<String> members = configuration.getStringList(path + "members");
            plots.put(regionName, new PlotData(regionName, ownerId, ownerName, worldName, centerX, centerY, centerZ, size, plotName, members));
        }
    }

    public void save() {
        configuration.set("plots", null);
        for (PlotData plot : plots.values()) {
            String path = "plots." + plot.getRegionName() + ".";
            configuration.set(path + "owner", plot.getOwnerUniqueId().toString());
            configuration.set(path + "owner-name", plot.getOwnerName());
            configuration.set(path + "world", plot.getWorldName());
            configuration.set(path + "center-x", plot.getCenterX());
            configuration.set(path + "center-y", plot.getCenterY());
            configuration.set(path + "center-z", plot.getCenterZ());
            configuration.set(path + "size", plot.getSize());
            configuration.set(path + "name", plot.getPlotName());
            configuration.set(path + "members", plot.getMembers());
        }
        try {
            configuration.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie udało się zapisać pliku dzialki.yml: " + e.getMessage());
        }
    }

    public PlotData getPlot(String regionName) {
        return plots.get(regionName);
    }

    public PlotData getPlotByCenter(int x, int y, int z, String worldName) {
        for (PlotData plot : plots.values()) {
            if (plot.getCenterX() == x && plot.getCenterY() == y && plot.getCenterZ() == z && plot.getWorldName().equals(worldName)) {
                return plot;
            }
        }
        return null;
    }

    public void savePlot(PlotData plotData) {
        plots.put(plotData.getRegionName(), plotData);
        save();
    }

    public void removePlot(String regionName) {
        plots.remove(regionName);
        save();
    }

    public List<PlotData> getPlots() {
        return new ArrayList<>(plots.values());
    }
}
