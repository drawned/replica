package me.drawn.replica.utils;

import me.drawn.replica.Replica;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataManager {

    public static YamlConfiguration npcData;
    private static final File npcFile = new File(Replica.getInstance().getDataFolder(), "npc-data.yml");

    public static void management() {
        Replica.getInstance().getDataFolder().mkdirs();
        if(!npcFile.exists()) {
            try {
                npcFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        npcData = YamlConfiguration.loadConfiguration(npcFile);
    }

    public static void saveData() {
        try {
            npcData.save(npcFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        npcData = YamlConfiguration.loadConfiguration(npcFile);
    }
}
