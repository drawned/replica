package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.npc.NPCManagement;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.utils.Utils;
import me.drawn.replica.utils.skins.SkinHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CreateCommand implements ICommand {
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public List<String> getAliases() {
        return List.of("new");
    }

    @Override
    public String getDescription() {
        return "Creates a new NPC.";
    }

    @Override
    public String getUsage() {
        return "<name>";
    }

    @Override
    public boolean playerOnly() {return true;}

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;

        if(args.length == 1) {
            Utils.warningMessage(sender, "You must provide a valid name for your NPC.");
            return true;
        }

        final String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        NPCData npcData = new NPCData(p.getLocation(), name);

        PlayerNPC npc = new PlayerNPC(NPCHandler.getNextID(), npcData);
        npc.forceSpawn(p);

        NPCManagement.selectNPC(p, npc);

        SkinHandler.getAndApplyFromName(null, name, npc);

        Utils.normalMessage(sender, "NPC "+ MainCommands.npcFormat(npc)+" created and selected!");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("<name>");
    }
}
