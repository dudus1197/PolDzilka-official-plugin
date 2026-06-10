package pl.poldzialka.managers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlotManager {
    private static final String PLOT_PREFIX = "dzialka_";
    private static final String PLOT_BLOCK_NAME = "Działka 11x11";
    private final JavaPlugin plugin;

    public PlotManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createPlotItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(Component.text(PLOT_BLOCK_NAME, NamedTextColor.AQUA));
        meta.lore(List.of(Component.text("Umieść na ziemi, aby utworzyć działkę.", NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isPlotBlockItem(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_BLOCK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }

        Component displayName = meta.displayName();
        if (displayName == null) {
            return false;
        }

        return displayName.equals(Component.text(PLOT_BLOCK_NAME, NamedTextColor.AQUA));
    }

    public Optional<ProtectedRegion> getPlotRegionAt(Location location) {
        RegionManager manager = getRegionManager(location.getWorld());
        if (manager == null) {
            return Optional.empty();
        }

        BlockVector3 position = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (ProtectedRegion region : manager.getRegions().values()) {
            if (!region.getId().startsWith(PLOT_PREFIX)) {
                continue;
            }
            if (region.contains(position)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    public Optional<ProtectedRegion> getPlotRegionById(World world, String regionId) {
        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            return Optional.empty();
        }
        ProtectedRegion region = manager.getRegion(regionId);
        if (region == null || !region.getId().startsWith(PLOT_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(region);
    }

    public Optional<ProtectedRegion> getPlotRegionByCenter(Block block) {
        RegionManager manager = getRegionManager(block.getWorld());
        if (manager == null) {
            return Optional.empty();
        }

        for (ProtectedRegion region : manager.getRegions().values()) {
            if (!region.getId().startsWith(PLOT_PREFIX)) {
                continue;
            }

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            // POPRAWKA 1.21: Zastąpiono getBlockX/Z przez getX/Z, żeby usunąć warningi
            int centerX = (min.getX() + max.getX()) / 2;
            int centerZ = (min.getZ() + max.getZ()) / 2;
            if (centerX == block.getX() && centerZ == block.getZ()) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    public boolean canCreatePlotAt(Location center) {
        RegionManager manager = getRegionManager(center.getWorld());
        if (manager == null) {
            return false;
        }

        BlockVector3 minBound = BlockVector3.at(center.getBlockX() - 5, 0, center.getBlockZ() - 5);
        BlockVector3 maxBound = BlockVector3.at(center.getBlockX() + 5, center.getWorld().getMaxHeight() - 1, center.getBlockZ() + 5);
        return !regionConflicts(manager, minBound, maxBound);
    }

    // Dopasowana metoda createPlot, o którą rzucał się kompilator w logach
    public Optional<ProtectedRegion> createPlot(Player player, Location center, int half, String plotId) {
        World world = center.getWorld();
        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            return Optional.empty();
        }

        int maxY = world.getMaxHeight() - 1;
        BlockVector3 min = BlockVector3.at(center.getBlockX() - half, 0, center.getBlockZ() - half);
        // POPRAWKA BŁĘDU: Zmieniono nazwę zmiennej z 'max' na 'maxVector', aby nie dublować nazwy typu prymitywnego / innego obiektu
        BlockVector3 maxVector = BlockVector3.at(center.getBlockX() + half, maxY, center.getBlockZ() + half);

        if (regionConflicts(manager, min, maxVector)) {
            return Optional.empty();
        }

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(plotId, min, maxVector);
        region.getOwners().addPlayer(player.getUniqueId());
        manager.addRegion(region);

        try {
            manager.save();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Nie udało się zapisać regionu WorldGuard", e);
        }

        return Optional.of(region);
    }

    public boolean removePlotRegion(World world, ProtectedRegion region) {
        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            return false;
        }

        if (manager.getRegion(region.getId()) == null) {
            return false;
        }

        manager.removeRegion(region.getId());
        try {
            manager.save();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Nie udało się usunąć regionu WorldGuard", e);
            return false;
        }
    }

    private boolean regionConflicts(RegionManager manager, BlockVector3 min, BlockVector3 max) {
        for (ProtectedRegion existing : manager.getRegions().values()) {
            if (!existing.getId().startsWith(PLOT_PREFIX)) {
                continue;
            }

            // POPRAWKA 1.21: Zastąpiono getBlockX/Z przez getX/Z w touchesOrOverlaps pośrednio przez wektory
            BlockVector3 existingMin = existing.getMinimumPoint();
            BlockVector3 existingMax = existing.getMaximumPoint();
            if (touchesOrOverlaps(min, max, existingMin, existingMax)) {
                return true;
            }
        }
        return false;
    }

    private boolean touchesOrOverlaps(BlockVector3 aMin, BlockVector3 aMax, BlockVector3 bMin, BlockVector3 bMax) {
        boolean xOverlap = aMin.getX() <= bMax.getX() + 1 && aMax.getX() >= bMin.getX() - 1;
        boolean zOverlap = aMin.getZ() <= bMax.getZ() + 1 && aMax.getZ() >= bMin.getZ() - 1;
        return xOverlap && zOverlap;
    }

    private String buildPlotId(Player player) {
        return PLOT_PREFIX + player.getName() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private RegionManager getRegionManager(World world) {
        if (world == null) {
            return null;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }
}