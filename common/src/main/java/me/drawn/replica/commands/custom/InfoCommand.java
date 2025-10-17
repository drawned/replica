package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InfoCommand implements ICommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getAliases() {
        return List.of("i");
    }

    @Override
    public String getDescription() {
        return "Shows information about the selected NPC or a specific one by ID.";
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

        sender.sendMessage(MainCommands.divider);

        sender.sendMessage(Utils.YELLOW_COLOR+"ID: §7"+npc.getId());
        sender.sendMessage(Utils.YELLOW_COLOR+"Name: §7"+npc.getName());
        sender.sendMessage(Utils.YELLOW_COLOR+"Active: §7"+npc.isActive());
        sender.sendMessage(Utils.YELLOW_COLOR+"Location: §7"+MainCommands.formatLocation(npc.getLocation()));

        sender.sendMessage(MainCommands.divider);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]");
    }
}

