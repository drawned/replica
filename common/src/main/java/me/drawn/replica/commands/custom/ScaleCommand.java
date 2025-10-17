package me.drawn.replica.commands.custom;

import me.drawn.replica.Replica;
import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScaleCommand implements ICommand {
    @Override
    public String getName() {
        return "scale";
    }

    @Override
    public List<String> getAliases() {
        return List.of("size");
    }

    @Override
    public String getDescription() {
        return "Changes the size of an NPC. Requires MC 1.21+";
    }

    @Override
    public String getUsage() {
        return "<size> [id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 2);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        if(!NMSHandler.runningAtLeast1_21()) {
            Utils.warningMessage(sender, "The server must be running at least Minecraft 1.21 in order to use this command.");
            return true;
        }

        if(args.length == 1) {
            Utils.warningMessage(sender, "You must provide a valid number between 0 and 16.0.");
            return true;
        }

        final double scale;

        try {
            scale = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            Utils.warningMessage(sender, "You must provide a valid number between 0 and 16.0.");
            return true;
        }

        if(scale < 0.0 || scale > 16.0) {
            Utils.warningMessage(sender, "You must provide a valid number between 0 and 16.0.");
            return true;
        }

        final List<Player> oldAudience = new ArrayList<>(npc.getAudiencePlayers());
        npc.getAudience().clear();

        for(Player player : oldAudience) {
            npc.remove(player);
        }

        npc.getNPCData().setScale(scale);
        NMSHandler.get().setScale(npc.getEntity(), scale);

        if(npc.getHologram() != null)
            npc.getHologram().recalculate();

        Utils.normalMessage(sender, "Changed NPC "+MainCommands.npcFormat(npc)+
                " scale to "+Utils.YELLOW_COLOR+scale);

        Replica.getScheduler().runTaskLaterAsynchronously(() -> {
            for(Player player : oldAudience) {
                npc.spawn(player);
            }
        }, 2);

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 2) {
            return List.of("<size>");
        } else return List.of("[id]");
    }
}
