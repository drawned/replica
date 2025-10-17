package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCManagement;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LookCloseCommand implements ICommand {
    @Override
    public String getName() {
        return "look-close";
    }

    @Override
    public List<String> getAliases() {
        return List.of("look");
    }

    @Override
    public String getDescription() {
        return "Allows you to enabled, disable or change the range of the look-close mechanic.";
    }

    @Override
    public String getUsage() {
        return "[enabled|disabled,--range]";
    }

    @Override
    public boolean playerOnly() {return true;}

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;

        NPC npc = NPCManagement.getSelectedNPC(p);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected to use this command.");
            return true;
        }

        if(args.length == 1) {
            boolean result = !npc.getNPCData().lookClose();

            Utils.normalMessage(sender, result ? "Enabling NPC look-close!" : "Disabling NPC look-close!");
            npc.getNPCData().setLookCloseEnabled(result);

            return true;
        }

        String option = args[1];

        if(option.equalsIgnoreCase("--range")) {
            if(args.length == 2) {
                Utils.warningMessage(sender, "You must provide a valid number to be the new block range " +
                        "of the look-close mechanic.");
                return true;
            }

            try {
                double range = Double.parseDouble(args[2]);

                if(range <= 0 || range > NPC.npcRenderRange) {
                    Utils.warningMessage(sender, "The block range must be a valid number " +
                            "between 0 and "+ NPC.npcRenderRange);
                    return true;
                }

                npc.getNPCData().setLookCloseRange(range);
                npc.recalculateNumericValues();

                Utils.normalMessage(sender, "Changed the new block range of look-close to "+range+" blocks!");
            } catch (NumberFormatException ex) {
                Utils.warningMessage(sender, "You must provide a valid number to be the new block range " +
                        "of the look-close mechanic.");
            }

            return true;
        }

        if(option.equalsIgnoreCase("enabled")) {
            Utils.normalMessage(sender, "Enabling NPC look-close!");
            npc.getNPCData().setLookCloseEnabled(true);
        } else if(option.equalsIgnoreCase("disabled")) {
            Utils.normalMessage(sender, "Disabling NPC look-close!");
            npc.getNPCData().setLookCloseEnabled(false);
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("enabled", "disabled", "--range");
    }
}
