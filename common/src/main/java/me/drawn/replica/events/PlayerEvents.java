package me.drawn.replica.events;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import me.drawn.replica.Replica;
import me.drawn.replica.api.NPCInteractEvent;
import me.drawn.replica.api.NPCTeleportEvent;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.npc.enums.InteractionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerEvents implements Listener {

    public static Set<UUID> cooldown = new HashSet<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        NPCHandler.activeNpcs.forEach(active -> {
            active.remove(e.getPlayer());
        });
        /*for(NPC npc : NPCHandler.npcs) {
            npc.removeFromAudienceOnly(e.getPlayer());
        }*/
    }

    @EventHandler
    public void onEntityUse(PlayerUseUnknownEntityEvent e) {
        final Player p = e.getPlayer();
        final int id = e.getEntityId();

        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        if(cooldown.contains(p.getUniqueId())) return;
        cooldown.add(p.getUniqueId());

        InteractionType interactionType = (e.isAttack()) ? InteractionType.LEFT_CLICK : InteractionType.RIGHT_CLICK;

        for(NPC npc : NPCHandler.activeNpcs) {
            if(npc.getEntityId() == id) {
                NPCInteractEvent interactEvent = new NPCInteractEvent(npc, interactionType, p);
                Bukkit.getPluginManager().callEvent(interactEvent);

                if(interactEvent.isCancelled())
                    return;

                Replica.getScheduler().runTaskLater(() -> {
                    cooldown.remove(p.getUniqueId());
                }, 2);

                npc.onInteraction(p, interactionType);
                return;
            }
        }
    }
}
