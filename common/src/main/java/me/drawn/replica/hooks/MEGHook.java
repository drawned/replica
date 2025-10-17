package me.drawn.replica.hooks;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import me.drawn.replica.Replica;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.npc.custom.ModelEngineNPC;
import me.drawn.replica.npc.enums.InteractionType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

import static me.drawn.replica.events.PlayerEvents.cooldown;

public class MEGHook implements Listener {

    public static List<String> getPossibleModelsList() {
        return ModelEngineAPI.getAPI().getModelRegistry().getKeys().stream().toList();
    }

    @EventHandler
    public void onInteract(BaseEntityInteractEvent e) {
        final Player p = e.getPlayer();

        InteractionType interactionType = e.getAction() == BaseEntityInteractEvent.Action.ATTACK ?
                InteractionType.LEFT_CLICK : InteractionType.RIGHT_CLICK;

        if(e.getSlot() != EquipmentSlot.HAND)
            return;

        if(cooldown.contains(p.getUniqueId())) return;
        cooldown.add(p.getUniqueId());

        for(NPC npc : NPCHandler.activeNpcs) {
            if (npc.getEntityId() == e.getBaseEntity().getEntityId()) {
                Replica.getScheduler().runTaskLater(() -> {
                    cooldown.remove(p.getUniqueId());
                }, 2);

                npc.onInteraction(p, interactionType);
                return;
            }
        }
    }
}
