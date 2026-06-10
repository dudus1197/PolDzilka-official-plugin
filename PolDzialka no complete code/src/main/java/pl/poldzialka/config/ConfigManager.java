package pl.poldzialka.config;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Menedżer konfiguracji pluginu poldzialka
 * Zarządza wszystkimi ustawieniami z pliku config.yml
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public String getLanguage() {
        String value = config.getString("language", "pl");
        return value.equalsIgnoreCase("en") ? "en" : "pl";
    }

    public Material getPlotMaterial() {
        return Material.matchMaterial(config.getString("plot-block-material", "DIAMOND_BLOCK"));
    }

    public Material getPlotItemMaterial() {
        return Material.matchMaterial(config.getString("plot-item-material", "DIAMOND_BLOCK"));
    }

    public List<Integer> getAllowedSizes() {
        return config.getIntegerList("allowed-sizes").stream()
            .filter(size -> size > 0 && size % 2 == 1)
            .collect(Collectors.toList());
    }

    public int getMinimumGap() {
        return config.getInt("minimum-gap", 1);
    }

    public int getMinWorldHeight() {
        return config.getInt("min-world-height", -64);
    }

    public int getMaxWorldHeight() {
        return config.getInt("max-world-height", 320);
    }

    public int getMaxPlotsPerPlayer() {
        return config.getInt("max-plots-per-player", 1);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<String> getDefaultFlags() {
        return config.getStringList("default-flags");
    }
}
