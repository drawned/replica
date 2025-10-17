package me.drawn.replica.nms;

import me.drawn.replica.Replica;
import org.bukkit.Bukkit;

public class NMSHandler {
    private static NMS instance;
    private static String version;

    public static boolean runningAtLeast1_21() {
        return !NMSHandler.version().startsWith("v1_20");
    }

    public static boolean setupNMS() {
        String bukkitVersion = Bukkit.getBukkitVersion();

        String[] parts = bukkitVersion.split("-")[0].split("\\.");
        if (parts.length <= 2) { // Formato 1.21, 1.22, etc.
            version = "v" + parts[0] + "_" + parts[1] + "_R1";
        } else { // Formato 1.20.1, 1.19.4, etc.
            version = "v" + parts[0] + "_" + parts[1] + "_R" + parts[2];
        }

        try {
            final String path = "me.drawn.replica.nms.NMS_" + version;

            Replica.secondLog("Detected server version "+version);

            Class<?> clazz = Class.forName(path);
            instance = (NMS) clazz.getDeclaredConstructor().newInstance();

            instance.initializeTeamManager();

            return true;
        } catch (Exception ex) {
            ex.fillInStackTrace();
            return false;
        }
    }

    public static String version() {
        return version;
    }

    public static NMS get() {
        return instance;
    }
}