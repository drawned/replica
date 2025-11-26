package me.drawn.replica.events;

import me.drawn.replica.npc.NPCHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        NPCHandler.activeNpcs.forEach(active -> {
            active.remove(e.getPlayer());
        });
        /*for(NPC npc : NPCHandler.npcs) {
            npc.removeFromAudienceOnly(e.getPlayer());
        }*/
    }
}
