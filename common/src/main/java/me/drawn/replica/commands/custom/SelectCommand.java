package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.npc.NPCManagement;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SelectCommand implements ICommand {
    @Override
    public String getName() {
        return "select";
    }

    @Override
    public List<String> getAliases() {
        return List.of("sel");
    }

    @Override
    public String getDescription() {
        return "Selects the NPC you are looking at or selects the NPC by the ID you provided.";
    }

    @Override
    public String getUsage() {
        return "[id]";
    }

    @Override
    public boolean playerOnly() {return true;}

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;

        NPC npc = null;

        if(args.length >= 2) {
            try {
                npc = NPCHandler.getNpcFromId(Integer.parseInt(args[1])).orElse(null);
            } catch (Exception ex) {
                Utils.warningMessage(sender, "There is no NPC with this specific ID.");
                return true;
            }
        } else {
            npc = NPCManagement.getViewTargetNPC(p.getEyeLocation());

            if (npc == null)
                npc = NPCManagement.getNearbyNPC(p.getLocation());
        }

        if (npc == null) {
            Utils.warningMessage(sender, "No NPC found in your field of view, nearby you or by the ID. " +
                    "You can manually provide the valid NPC ID if you are having issues.");
            return true;
        }

        NPCManagement.selectNPC(p, npc);

        Utils.normalMessage(p, "You selected NPC "+ MainCommands.npcFormat(npc)+"!");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]");
    }
}
