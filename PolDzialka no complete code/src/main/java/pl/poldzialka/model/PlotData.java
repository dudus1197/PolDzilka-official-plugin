package pl.poldzialka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Model danych działki
 * Przechowuje wszystkie informacje o jednej działce
 */
public class PlotData {
    private final String regionName;
    private final UUID ownerUniqueId;
    private final String ownerName;
    private final String worldName;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final int size;
    private final String plotName;
    private final List<String> members;
    private final long createdAt;

    public PlotData(String regionName, UUID ownerUniqueId, String ownerName, String worldName, int centerX, int centerY, int centerZ, int size, String plotName, List<String> members) {
        this.regionName = regionName;
        this.ownerUniqueId = ownerUniqueId;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.size = size;
        this.plotName = plotName != null && !plotName.isBlank() ? plotName : regionName;
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getRegionName() {
        return regionName;
    }

    public UUID getOwnerUniqueId() {
        return ownerUniqueId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getSize() {
        return size;
    }

    public String getPlotName() {
        return plotName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Location getCenterLocation() {
        World world = Bukkit.getWorld(worldName);
        return world == null ? null : new Location(world, centerX, centerY, centerZ);
    }

    public List<String> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(String memberName) {
        if (!members.contains(memberName)) {
            members.add(memberName);
        }
    }

    public void removeMember(String memberName) {
        members.remove(memberName);
    }

    public boolean hasMember(String memberName) {
        return members.contains(memberName);
    }

    public boolean isMember(UUID playerId, String playerName) {
        return ownerUniqueId.equals(playerId) || members.contains(playerName);
    }

    public boolean isOwner(UUID playerId) {
        return ownerUniqueId.equals(playerId);
    }
}
