package pl.poldzialka.managers;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Menedżer integracji z WorldGuard
 * Zarządza tworzeniem, usuwaniem i ustawianiem flag regionów
 */
public class WorldGuardManager {
    private final JavaPlugin plugin;

    public WorldGuardManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isWorldGuardAvailable() {
        try {
            return org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tworzy region WorldGuard dla nowej działki
     */
    public String createPlotRegion(World world, Location center, int size, Player owner) {
        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                    .getRegionContainer().get(new BukkitWorldAdapter(world));

            if (regionManager == null) return null;

            int halfSize = size / 2;
            BlockVector3 pos1 = BlockVector3.at(
                    center.getBlockX() - halfSize,
                    plugin.getServer().getWorlds().get(0).getMinHeight(),
                    center.getBlockZ() - halfSize
            );

            BlockVector3 pos2 = BlockVector3.at(
                    center.getBlockX() + halfSize,
                    plugin.getServer().getWorlds().get(0).getMaxHeight(),
                    center.getBlockZ() + halfSize
            );

            String regionName = "dzialka_" + owner.getName() + "_" + System.currentTimeMillis();
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, pos1, pos2);
            region.getOwners().addPlayer(owner.getUniqueId());

            // Ustaw domyślne flagi
            region.setFlag(Flags.PVP, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            region.setFlag(Flags.ENTRY, com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW);
            region.setFlag(Flags.CHEST_ACCESS, com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW);
            region.setFlag(Flags.MOB_SPAWNING, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            region.setFlag(Flags.RIDE, com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW);

            regionManager.addRegion(region);
            return regionName;
        } catch (Exception e) {
            plugin.getLogger().warning("Błąd podczas tworzenia regionu WorldGuard: " + e.getMessage());
            return null;
        }
    }

    /**
     * Usuwa region WorldGuard
     */
    public void removeRegion(World world, String regionName) {
        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                    .getRegionContainer().get(new BukkitWorldAdapter(world));

            if (regionManager != null) {
                regionManager.removeRegion(regionName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Błąd podczas usuwania regionu WorldGuard: " + e.getMessage());
        }
    }

    /**
     * Sprawdza czy nowa działka koliduje z innymi
     */
    public boolean checkOverlap(World world, Location center, int size) {
        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                    .getRegionContainer().get(new BukkitWorldAdapter(world));

            if (regionManager == null) return false;

            int halfSize = size / 2;
            BlockVector3 pos1 = BlockVector3.at(
                    center.getBlockX() - halfSize - 1,
                    plugin.getServer().getWorlds().get(0).getMinHeight(),
                    center.getBlockZ() - halfSize - 1
            );

            BlockVector3 pos2 = BlockVector3.at(
                    center.getBlockX() + halfSize + 1,
                    plugin.getServer().getWorlds().get(0).getMaxHeight(),
                    center.getBlockZ() + halfSize + 1
            );

            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion("temp", pos1, pos2);
            return !regionManager.getApplicableRegions(tempRegion).getRegions().isEmpty();
        } catch (Exception e) {
            plugin.getLogger().warning("Błąd podczas sprawdzania koliduje z WorldGuard: " + e.getMessage());
            return false;
        }
    }

    public List<StateFlag> getAvailableStateFlags() {
        List<StateFlag> flags = new ArrayList<>();
        for (Field field : Flags.class.getFields()) {
            if (StateFlag.class.isAssignableFrom(field.getType())) {
                try {
                    Object value = field.get(null);
                    if (value instanceof StateFlag stateFlag) {
                        flags.add(stateFlag);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        flags.sort(Comparator.comparing(StateFlag::getName));
        return flags;
    }

    public StateFlag getStateFlagByName(String name) {
        if (name == null) {
            return null;
        }
        for (StateFlag flag : getAvailableStateFlags()) {
            if (flag.getName().equalsIgnoreCase(name)) {
                return flag;
            }
        }
        return null;
    }

    public State getFlagState(ProtectedRegion region, StateFlag flag) {
        if (region == null || flag == null) {
            return null;
        }
        return region.getFlag(flag);
    }

    public State getNextState(State current) {
        if (current == null) {
            return State.ALLOW;
        }
        return current == State.ALLOW ? State.DENY : null;
    }

    /**
     * Adapter dla Bukkit World do WorldGuard
     */
    private static class BukkitWorldAdapter extends com.sk89q.worldedit.bukkit.BukkitWorld {
        public BukkitWorldAdapter(World world) {
            super(world);
        }
    }
}
