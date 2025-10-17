package me.drawn.replica.npc.custom;

import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerNPC extends NPC {

    public PlayerNPC(int id, NPCData npcData) {
        super(id, npcData);

        this.entity = NMSHandler.get().createPlayer(this, this.location, name);

        NMSHandler.get().initialSkinLoad(this, npcData);

        initializeHologram();
    }

    @Override
    public void teleport(final Location newLocation) {
        this.location = newLocation;

        NMSHandler.get().teleportPlayerNPC(this, newLocation);

        if(this.hologram != null)
            this.hologram.tick();

        for(Player player : getAudiencePlayers()) {
            remove(player);
        }

        for(Player player : getAudiencePlayers()) {
            spawn(player);
        }
    }

    public void applySkin(NPCData.SkinTexture skinTexture, UUID uuid) {
        NMSHandler.get().applySkin(this, skinTexture, uuid);
    }

    @Override
    public void lookClose() {
        if(!npcData.lookClose()) return;

        final double x = location.getX();
        final double z = location.getZ();

        Map<Player, Double> distanceMap = new HashMap<>();

        for(Player player : getAudiencePlayers()) {
            final Location playerLoc = player.getLocation();

            double dx = playerLoc.getX() - x;
            double dz = playerLoc.getZ() - z;
            double distanceSquared = dx*dx + dz*dz;

            distanceMap.put(player, distanceSquared);
        }

        if (distanceMap.isEmpty()) return;

        Player closest = distanceMap.entrySet()
                .stream()
                .filter(e -> e.getValue() <= lookCloseMinDistanceSquared) // filter players that are only close to the mob
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (closest == null) return;

        if(closest.isInvisible() || closest.getGameMode() == GameMode.SPECTATOR) return;

        NMSHandler.get().playerLookClose(this, closest, distanceMap);
    }

    @Override
    public void spawn(Player player) {
        if(audience.contains(player.getUniqueId())) return;

        NMSHandler.get().spawnPlayerNPC(this, player);

        if(this.hologram != null)
            this.hologram.spawn(player);
    }

    public void forceSpawn(Player player) {
        audience.add(player.getUniqueId());

        NMSHandler.get().spawnPlayerNPC(this, player);

        if(this.hologram != null)
            this.hologram.spawn(player);
    }

    @Override
    public void remove(Player player) {
        NMSHandler.get().removePlayerNPC(this, player);

        audience.remove(player.getUniqueId());

        if(this.hologram != null)
            this.hologram.remove(player);
    }

    @Override
    public void shutdown() {}

}
