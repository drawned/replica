package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.CitizensConverter;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MigrateFromCommand implements ICommand {
    @Override
    public String getName() {
        return "migrate-from";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUsage() {
        return "<plugin>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length == 1) {
            Utils.warningMessage(sender, "You must provide a plugin to migrate from, " +
                    "currently Replica only supports Citizens.");
            return true;
        }

        if(args[1].equalsIgnoreCase("citizens")) {
            Utils.normalMessage(sender, "Converting Citizens data file, please wait...");

            CitizensConverter.startConversion();

            Utils.normalMessage(sender, "Data migrated successfully! Currently the server now have "+ NPCHandler.npcs.size()+" NPCs loaded.");
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("citizens");
    }
}
