package me.drawn.replica.utils;

import me.drawn.replica.Replica;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.custom.PlayerNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CitizensConverter {

    public static void startConversion() {
        File file = new File("plugins/Citizens/saves.yml");

        if(!file.exists()) return;

        YamlConfiguration saves = YamlConfiguration.loadConfiguration(file);

        for(String key : saves.getConfigurationSection("npc").getKeys(false)) {
            final ConfigurationSection npc = saves.getConfigurationSection("npc." + key);

            Replica.log("Migrating Citizens NPC "+key);

            try {

                final ConfigurationSection locationSec = npc.getConfigurationSection("traits.location");

                final World world = Bukkit.getWorld(locationSec.getString("world", ""));
                if(world == null) {
                    Replica.secondLogError("Couldn't register this NPC because this world does not exist: "+locationSec.getString("world", ""));
                    continue;
                }

                Location location = new Location(world,
                        Double.parseDouble(locationSec.getString("x")), Double.parseDouble(locationSec.getString("y")),
                        Double.parseDouble(locationSec.getString("z")), Float.parseFloat(locationSec.getString("yaw")),
                        Float.parseFloat(locationSec.getString("pitch")));

                final String originalName = npc.getString("name", "UNKNOWN");
                final String name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', originalName));

                NPCType npcType = NPCType.valueOf(npc.getString("traits.type", "PLAYER"));

                NPCData npcData = new NPCData(location, name)
                        .withType(npcType)
                        .withUUID(npc.contains("uuid") ? UUID.fromString(npc.getString("uuid", UUID.randomUUID().toString()))
                                : UUID.randomUUID());

                if(npc.contains("traits.meg_model")) {
                    npcData.withType(NPCType.MODEL_ENGINE);
                }

                final ConfigurationSection skinSec = npc.getConfigurationSection("traits.skintrait");
                if(skinSec != null) {
                    final String textureRaw = skinSec.getString("textureRaw", "");
                    final String signature = skinSec.getString("signature", "");

                    npcData.withSkinTexture(new NPCData.SkinTexture(signature, textureRaw));
                }

                if(npc.getConfigurationSection("traits.hologramtrait.lines") != null) {
                    List<String> components = new ArrayList<>();
                    for(String lineKey : npc.getConfigurationSection("traits.hologramtrait.lines").getKeys(false)) {
                        final String text = npc.getString("traits.hologramtrait.lines."+lineKey+".text", name);
                        components.add(text);
                    }
                    npcData.withHologramLines(components);
                }

                if(npc.getConfigurationSection("traits.lookclose") != null) {
                    final boolean enabled = npc.getBoolean("traits.lookclose.enabled", false);
                    final double range = npc.getDouble("traits.lookclose.range", 5);

                    npcData.setLookCloseEnabled(enabled);
                    npcData.setLookCloseRange(range);
                }

                NPC n = new PlayerNPC(NPCHandler.getNextID(), npcData);

                n.setToFile();

                Replica.secondLog("| NPC "+key+" loaded with name "+name+" and ID "+n.getId());
            } catch (Exception ex) {
                Replica.secondLogError("| NPC "+key+" could not be created, errors below: "+ex.getMessage());
                ex.printStackTrace();
            }
        }

        DataManager.saveData();
    }

}
