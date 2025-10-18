package me.drawn.replica.utils;

import me.drawn.replica.Replica;
import me.drawn.replica.api.NPCSpawnEvent;
import me.drawn.replica.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.Normalizer;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Utils {

    public static final Vector3f emptyVector3f = new Vector3f(0, 0, 0);
    public static final Quaternionf emptyQuaternion = new Quaternionf();

    public static String sanitize(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    public static boolean throwSyncSpawnEvent(final NPC npc, final Player player) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Executa no thread principal
        Replica.getScheduler().runTask(() -> {
            try {
                NPCSpawnEvent event = new NPCSpawnEvent(npc, player);
                Bukkit.getPluginManager().callEvent(event);
                future.complete(event.isCancelled());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        // Bloqueia até o resultado estar disponível
        try {
            return future.get(500, TimeUnit.MILLISECONDS); // pode usar .get(timeout, TimeUnit.SECONDS) se quiser limite
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    location:
      xyz: "WORLD X Y Z"
      yaw-pitch: "yaw pitch"
     */
    public static Location locationFromLocationSection(final ConfigurationSection locationSection) {
        if(locationSection == null)
            return null;

        final String[] xyz = locationSection.getString("xyz").split(" ");
        final String world = xyz[0];
        final double x = Double.parseDouble(xyz[1]);
        final double y = Double.parseDouble(xyz[2]);
        final double z = Double.parseDouble(xyz[3]);

        final String[] yawPitch = locationSection.getString("yaw-pitch").split(" ");
        final float yaw = Float.parseFloat(yawPitch[0]);
        final float pitch = Float.parseFloat(yawPitch[1]);

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void warningMessage(CommandSender sender, String message) {
        sender.sendMessage(getWarningPrefix()+Utils.c(message));
    }

    public static void normalMessage(CommandSender sender, String message) {
        sender.sendMessage(getNormalPrefix()+Utils.c(message));
    }

    public static void checkUpdates(final Consumer<String> consumer) {
        Replica.getScheduler().runTaskAsynchronously(() -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 124726 + "/~").openStream(); Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException e) {
                Replica.log("&cAn error occurred while checking for updates.");
                e.fillInStackTrace();
            }
        });
    }

    public static final String WARNING_COLOR = net.md_5.bungee.api.ChatColor.of("#c74242")+"";
    public static final String YELLOW_COLOR = net.md_5.bungee.api.ChatColor.of("#ffc933")+"";
    public static final String DARK_YELLOW_COLOR = net.md_5.bungee.api.ChatColor.of("#c4752b")+"";

    public static String getNormalPrefix() {
        return YELLOW_COLOR+"§lReplica §7→ §f";
    }

    public static String getWarningPrefix() {
        return WARNING_COLOR+"§lReplica §7→ §c";
    }


}
