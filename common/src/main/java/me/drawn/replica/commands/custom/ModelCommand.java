package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.hooks.MEGHook;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.npc.custom.ModelEngineNPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ModelCommand implements ICommand {
    @Override
    public String getName() {
        return "model";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Changes the ModelEngine model of your NPC.";
    }

    @Override
    public String getUsage() {
        return "<model> [id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 2);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        if(npc.getNPCData().npcType() != NPCType.MODEL_ENGINE) {
            Utils.warningMessage(sender,"This NPC needs to be of type "+NPCType.MODEL_ENGINE.name()+" " +
                    "before using this command.");
            return true;
        }

        if(args.length == 1) {
            Utils.warningMessage(sender, "You must provide a valid ModelEngine model name for this NPC.");
            return true;
        }

        final String model = args[1];

        ((ModelEngineNPC)npc).applyModel(model);

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 2) {
            return MEGHook.getPossibleModelsList();
        } else return List.of("[id]");
    }
}
