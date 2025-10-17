package me.drawn.replica.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NPCManagement {

    public static Map<UUID, NPC> selectedNPC = new HashMap<>();

    public static void selectNPC(Player player, @NotNull NPC npc) {
        selectedNPC.put(player.getUniqueId(), npc);
    }

    public static void unselectAll(NPC npc) {
        Set<UUID> cache = new HashSet<>(selectedNPC.keySet());
        for(UUID u : cache) {
            NPC n = selectedNPC.get(u);

            if(n != null && n == npc) {
                selectedNPC.remove(u);
            }
        }
    }

    private static final double maxDistance = 10;
    public static NPC getViewTargetNPC(final Location eyeLocation) {
        Vector direction = eyeLocation.getDirection().normalize();

        NPC target = null;
        double closest = maxDistance;

        for (NPC npc : NPCHandler.activeNpcs) {
            Location npcLoc = npc.getLocation().clone().add(0, 1, 0);
            Vector toNpc = npcLoc.toVector().subtract(eyeLocation.toVector());

            double distance = toNpc.length();
            if (distance > maxDistance) continue;

            toNpc.normalize();
            double dot = direction.dot(toNpc);

            if (dot > 0.99D) {
                if (distance < closest) {
                    closest = distance;
                    target = npc;
                }
            }
        }

        return target;
    }

    public static NPC getNearbyNPC(final Location playerLoc) {
        return NPCHandler.activeNpcs.stream()
                .filter(npc -> npc.getLocation().getWorld().equals(playerLoc.getWorld()))
                .min(Comparator.comparingDouble(npc ->
                        npc.getLocation().distance(playerLoc)))
                .orElse(null);
    }

    public static NPC getSelectedNPC(Player player) {
        return selectedNPC.getOrDefault(player.getUniqueId(), null);
    }

}
