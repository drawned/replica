package me.drawn.replica;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.events.PaperEvents;
import me.drawn.replica.events.PlayerEvents;
import me.drawn.replica.hooks.MEGHook;
import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.DataManager;
import me.drawn.replica.utils.Metrics;
import me.drawn.replica.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Replica extends JavaPlugin {

    public static Replica getInstance() {
        return Replica.getPlugin(Replica.class);
    }
    public static TaskScheduler getScheduler() {return scheduler;}

    public static Logger l;

    public static boolean MEG_HOOK = false;

    public static boolean pluginUpdated = true;

    public static TaskScheduler scheduler;

    @Override
    public void onEnable() {
        l = this.getLogger();
        scheduler = UniversalScheduler.getScheduler(this);
        DataManager.management();

        log(Utils.YELLOW_COLOR+" ______     ______     ______   __         __     ______     ______    ");
        log(Utils.YELLOW_COLOR+"/\\  == \\   /\\  ___\\   /\\  == \\ /\\ \\       /\\ \\   /\\  ___\\   /\\  __ \\   ");
        log(Utils.YELLOW_COLOR+"\\ \\  __<   \\ \\  __\\   \\ \\  _-/ \\ \\ \\____  \\ \\ \\  \\ \\ \\____  \\ \\  __ \\  ");
        log(Utils.YELLOW_COLOR+" \\ \\_\\ \\_\\  \\ \\_____\\  \\ \\_\\    \\ \\_____\\  \\ \\_\\  \\ \\_____\\  \\ \\_\\ \\_\\ ");
        log(Utils.YELLOW_COLOR+"  \\/_/ /_/   \\/_____/   \\/_/     \\/_____/   \\/_/   \\/_____/   \\/_/\\/_/ ");
        log(Utils.YELLOW_COLOR+"&lEnabling Replica v"+this.getDescription().getVersion());
        log(Utils.YELLOW_COLOR+"&lRunning on Minecraft version "+Bukkit.getServer().getClass().getPackage().getName());
        empty();

        log("&fInitializing NMS...");
        if (!NMSHandler.setupNMS()) {
            secondLog("&cYour server version is not supported!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        empty();

        log("&fInitializing commands and events...");
        this.getCommand("replica").setExecutor(new MainCommands());
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        try {
            secondLog("Loading PaperMC exclusives...");
            Class.forName("com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent");

            getServer().getPluginManager().registerEvents(new PaperEvents(), this);
        } catch (ClassNotFoundException e) {
            secondLogError("Hey, it seems like you are not using Paper or any other Paper fork, the NPC actions system will not work.");
        }

        empty();

        log("&fInitializing integration with other plugins...");
        if(getServer().getPluginManager().isPluginEnabled("ModelEngine")) {
            secondLog("ModelEngine found! Make sure you are running the latest Replica and ModelEngine version.");
            MEG_HOOK = true;
            getServer().getPluginManager().registerEvents(new MEGHook(), this);
        }
        empty();

        log("&fInitializing all async tasks...");
        Replica.getScheduler().runTaskTimerAsynchronously(NPCHandler::allTick, 60, 20);
        Replica.getScheduler().runTaskTimerAsynchronously(NPCHandler::allActiveTick, 70, 1);
        Replica.getScheduler().runTaskTimerAsynchronously(DataManager::saveData, 70, 6000);
        empty();

        log("&fRunning metrics and plugin update checker...");
        new Metrics(this, 27191);

        Utils.checkUpdates(version -> {
            log("&fChecking for updates...");
            if (this.getDescription().getVersion().equals(version)) {
                secondLog("The plugin is updated!");
            } else {
                pluginUpdated = false;
                secondLogError("There is a new update available. Replica is constantly under development and new features are added all the time. You can downloaded the most recent version here: https://www.spigotmc.org/resources/128599/ or https://modrinth.com/plugin/replica");
            }
        });

        empty();
        log("&fLoading all NPCs...");
        NPCHandler.loadAll();
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage("[Replica] "+ Utils.c(message));
    }
    // Lime color
    public static void firstLog(String message) {
        log(Utils.YELLOW_COLOR+"| "+message);
    }
    public static void secondLog(String message) {
        log("  "+Utils.DARK_YELLOW_COLOR+"| "+message);
    }

    public static void secondLogError(String message) {
        log("  "+Utils.WARNING_COLOR+"| "+message);
    }

    public static void empty() {l.info(" ");}

    @Override
    public void onDisable() {
        NPCHandler.npcs.forEach(npc -> {
            npc.removeForAll();
            npc.setToFile();
            npc.shutdown();
        });
        DataManager.saveData();
    }
}
