package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.DataManager;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveCommand implements ICommand {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public List<String> getAliases() {
        return List.of("save-data");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPCHandler.npcs.forEach(NPC::setToFile);

        DataManager.saveData();

        Utils.normalMessage(sender, "NPC data saved.");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
