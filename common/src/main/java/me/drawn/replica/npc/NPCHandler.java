package me.drawn.replica.npc;

import me.drawn.replica.Replica;
import me.drawn.replica.events.PlayerEvents;
import me.drawn.replica.npc.custom.ModelEngineNPC;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.npc.enums.InteractionType;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.utils.DataManager;
import me.drawn.replica.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class NPCHandler {

    public static List<NPC> npcs = new ArrayList<>();
    public static Set<NPC> activeNpcs = new HashSet<>();

    public static int lastID;
    public static int getNextID() {
        int result = (lastID + 1);

        lastID = result;

        DataManager.npcData.set("last-id", lastID);

        return result;
    }

    public static void delete(NPC npc) {
        NPCManagement.unselectAll(npc);
    }

    public static void loadAll() {
        lastID = DataManager.npcData.getInt("last-id", 0);

        final ConfigurationSection section = DataManager.npcData.getConfigurationSection("npcs");

        if(section == null) {
            DataManager.npcData.createSection("npcs");
            return;
        }

        for(String configKey : section.getKeys(false)) {
            loadFromFile(configKey);
        }
    }

    public static Optional<NPC> getNpcFromId(int id) {
        return npcs.stream().filter(a -> a.getId() == id)
                .findFirst();
    }

    public static void replaceNPC(NPC oldNpc, NPC newNpc) {
        npcs.remove(oldNpc);
        npcs.add(newNpc);

        Set<UUID> keys = NPCManagement.selectedNPC.keySet();
        for(UUID u : keys) {
            NPC n = NPCManagement.selectedNPC.get(u);

            if(n == oldNpc) {
                NPCManagement.selectedNPC.remove(u);
                NPCManagement.selectedNPC.put(u, newNpc);
            }
        }
    }

    public static void allTick() {
        Map<Player, Location> globalAudience = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            globalAudience.put(player, player.getLocation());
        }

        if(globalAudience.isEmpty()) return;

        for(NPC npc : NPCHandler.npcs) {
            try {
                npc.tick(globalAudience);
            } catch(Exception ex) {
                Replica.secondLogError("An error occurred when trying to tick NPC ("+npc.getId()+ ") "+npc.getName()+": "+ex.getMessage());
            }
        }

        PlayerEvents.cooldown.clear();
    }

    public static void allActiveTick() {
        List<NPC> npcs = new ArrayList<>(NPCHandler.activeNpcs);
        for (NPC npc : npcs) {
            npc.activeTick();
        }
    }

    private static void loadFromFile(String configKey) {
        int id = -1;

        try {
            id = Integer.parseInt(configKey);
        } catch (NumberFormatException ex) {
            return;
        }

        final ConfigurationSection section = DataManager.npcData.getConfigurationSection("npcs."+id);
        if(section == null) return;

        final UUID uuid = UUID.fromString(section.getString("uuid", UUID.randomUUID().toString()));
        final String name = section.getString("name", uuid.toString());

        NPCType npcType = NPCType.valueOf(section.getString("type", "PLAYER"));

        final Location location = Utils.locationFromLocationSection(section.getConfigurationSection("location"));
        if(location == null) return;

        final ConfigurationSection hologramSection = section.getConfigurationSection("hologram");

        final boolean hologramEnabled = hologramSection.contains("enabled") ? hologramSection.getBoolean("enabled") : true;
        final @Nullable List<String> hologramLines = hologramSection.getStringList("lines");

        if(hologramLines.isEmpty())
            hologramLines.add(name);

        NPCData npcData = new NPCData(location, name)
                .withUUID(uuid)
                .withType(npcType)
                .withHologramEnabled(hologramEnabled)
                .withHologramLines(hologramLines);

        npcData.setHologramAutoScale(hologramSection.getBoolean("auto-scale", true));

        final ConfigurationSection globalSection = section.getConfigurationSection("global-options");
        npcData.setLookCloseEnabled(globalSection.getBoolean("look-close.enabled", false));
        npcData.setLookCloseRange(globalSection.getDouble("look-close.range", 5));

        final ConfigurationSection actions = section.getConfigurationSection("actions");
        if(actions != null) {
            for(String key : actions.getKeys(false)) {
                InteractionType type = InteractionType.get(actions.getString(key+".type", "RIGHT_CLICK").toUpperCase());

                NPCInteraction npcInteraction = new NPCInteraction(actions.getString(key+".command", ""), type);
                npcData.addInteraction(npcInteraction);
            }
        }

        if(globalSection.contains("scale"))
            npcData.setScale(globalSection.getDouble("scale", NPCData.DEFAULT_SCALE));

        switch(npcType) {
            case PLAYER -> {
                final ConfigurationSection playerOptions = section.getConfigurationSection("options.PLAYER");
                if(playerOptions == null) return;

                npcData.withSkinTexture(new NPCData.SkinTexture(playerOptions.getString("signature", ""),
                        playerOptions.getString("textureRaw", "")));

                new PlayerNPC(id, npcData);
            }
            case MODEL_ENGINE -> {
                if(!Replica.MEG_HOOK) {
                    Replica.l.info("An Model Engine NPC was not loaded because ModelEngine " +
                            "hook is disabled or ModelEngine is not present in the server.");
                    return;
                }

                final ConfigurationSection modelOptions = section.getConfigurationSection("options.MODEL_ENGINE");
                if(modelOptions == null) return;

                npcData.withModelEngine(modelOptions.getString("model"));

                new ModelEngineNPC(id, npcData);
            }
        }
    }

}
