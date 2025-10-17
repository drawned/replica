package me.drawn.replica.nms;

import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.custom.NPCHologram;
import me.drawn.replica.npc.custom.PlayerNPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface NMS {

    void initializeTeamManager();

    public void tickHologram(final NPCHologram npcHologram);
    public Object createHologram(final NPCHologram npcHologram, final Location location);

    public void spawnHologram(NPCHologram npcHologram, Player player);
    public void removeHologram(NPCHologram npcHologram, Player player);

    // PlayerNPC
    Object createPlayer(PlayerNPC playerNPC, final Location location, final String name);
    public void spawnPlayerNPC(PlayerNPC playerNPC, Player player);
    public void removePlayerNPC(PlayerNPC playerNPC, Player player);
    public void playerLookClose(PlayerNPC playerNPC, Player closest, Map<Player, Double> distanceMap);
    public void applySkin(PlayerNPC playerNPC, NPCData.SkinTexture skinTexture, UUID newUuid);
    public void initialSkinLoad(PlayerNPC playerNPC, NPCData npcData);

    public void setScale(final Object livingEntity, double scale);

    public void teleportPlayerNPC(final PlayerNPC playerNPC, final Location newLocation);

    public double getEyeHeight(final Object livingEntity);
}
