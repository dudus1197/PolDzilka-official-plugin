package pl.poldzialka.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageManager {
    private final JavaPlugin plugin;
    private final FileConfiguration messages;

    public MessageManager(JavaPlugin plugin, String language) {
        this.plugin = plugin;
        this.messages = loadLanguageFile(language);
    }

    private FileConfiguration loadLanguageFile(String language) {
        File file = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        if (!file.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public Component getComponent(String key) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(getString(key));
    }

    public String getString(String key) {
        return messages.getString(key, key);
    }

    public List<Component> getComponentList(String key) {
        List<String> list = messages.getStringList(key);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Component> components = new ArrayList<>();
        for (String line : list) {
            components.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
        }
        return components;
    }

    public String format(String raw, Object... replacements) {
        String text = raw;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            if (replacements[i] instanceof String && replacements[i + 1] != null) {
                text = text.replace("%" + replacements[i] + "%", replacements[i + 1].toString());
            }
        }
        return text;
    }

    public Component formatComponent(String key, Object... replacements) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(format(getString(key), replacements));
    }
}
