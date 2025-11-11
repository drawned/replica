package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ForceSpawnCommand implements ICommand {
    @Override
    public String getName() {
        return "forcespawn";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Forces a NPC to spawn for you.";
    }

    @Override
    public String getUsage() {
        return "<id, all>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 1) {
            Utils.warningMessage(sender, "You must give a valid npc ID or leave it as 'all'.");
            return true;
        }

        if(args[1].equalsIgnoreCase("all")) {
            Utils.normalMessage(sender, "Spawning all "+NPCHandler.npcs.size()+" NPCs for you.");
            for(NPC npc : NPCHandler.npcs) {
                npc.spawn(player);
            }
            return true;
        }

        try {
            NPCHandler.getNpcFromId(Integer.parseInt(args[1])).ifPresent(npc -> {
                Utils.normalMessage(sender, "Spawning NPC "+npc.getName()+" for you.");
                npc.spawn(player);
            });
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("id", "all");
    }
}
