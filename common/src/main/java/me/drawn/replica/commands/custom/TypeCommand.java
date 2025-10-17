package me.drawn.replica.commands.custom;

import me.drawn.replica.Replica;
import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class TypeCommand implements ICommand {
    @Override
    public String getName() {
        return "type";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Changes the type of your NPC.";
    }

    @Override
    public String getUsage() {
        return "<type> [id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 2);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        if(args.length == 1) {
            Utils.warningMessage(sender, "You must provide a valid NPC type, the possible " +
                    "values are: "+Utils.YELLOW_COLOR+ Arrays.toString(NPCType.values()));
            return true;
        }

        NPCType npcType;
        try {
            npcType = NPCType.valueOf(args[1].toUpperCase());

            if(npcType == NPCType.MODEL_ENGINE && !Replica.MEG_HOOK) {
                Utils.warningMessage(sender, "You must have Model Engine enabled on the server in order " +
                        "to use this type of NPC.");
                return true;
            }

            npc.changeNPCType(npcType);
        } catch (IllegalArgumentException ex) {
            Utils.warningMessage(sender, "You must provide a valid NPC type, the possible " +
                    "values are: "+Utils.YELLOW_COLOR+Arrays.toString(NPCType.values()));
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 2) {
            return Arrays.stream(NPCType.values()).map(Enum::toString).toList();
        } else return List.of("[id]");
    }
}
