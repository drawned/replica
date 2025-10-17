package me.drawn.replica.npc.custom;

import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MobNPC extends NPC {

    public MobNPC(int id, NPCData npcData) {
        super(id, npcData);
    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public void spawn(Player player) {

    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void lookClose() {

    }

    @Override
    public void teleport(Location newLocation) {

    }
}
