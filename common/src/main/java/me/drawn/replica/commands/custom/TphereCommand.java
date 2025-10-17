package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TphereCommand implements ICommand {
    @Override
    public String getName() {
        return "tphere";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Teleports an NPC to your current position.";
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

        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 1);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        npc.teleport(p.getLocation());

        Utils.normalMessage(sender, "NPC "+MainCommands.npcFormat(npc)+" got teleported to your current position.");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]");
    }
}
