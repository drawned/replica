package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DeleteCommand implements ICommand {
    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public List<String> getAliases() {
        return List.of("remove", "rem", "del");
    }

    @Override
    public String getDescription() {
        return "Deletes a NPC permanently.";
    }

    @Override
    public String getUsage() {
        return "[id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 1);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        npc.delete();

        Utils.normalMessage(sender, "NPC "+MainCommands.npcFormat(npc)+" deleted successfully!");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]");
    }
}
