package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.utils.Utils;
import me.drawn.replica.utils.skins.SkinHandler;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SkinCommand implements ICommand {
    @Override
    public String getName() {
        return "skin";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Allows you to change the skin of your NPC of Player type.";
    }

    @Override
    public String getUsage() {
        return "<username | url> [id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 2);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        if(npc.getNPCData().npcType() != NPCType.PLAYER) {
            Utils.warningMessage(sender,"This NPC needs to be of type "+NPCType.PLAYER.name()+" " +
                    "before using this command.");
            return true;
        }

        if(args.length == 1) {
            Utils.warningMessage(sender,"You must provide a valid skin! You can just use the name of a " +
                    "premium Minecraft account or an image URL ending with PNG, JPEG or any other valid image format.");
            return true;
        }

        final String skin = args[1];

        if(skin.toLowerCase().contains("http")) {
            SkinHandler.getAndApplyFromURL(sender, skin, (PlayerNPC) npc);
            Utils.normalMessage(sender, "Fetching URL and applying the skin to this NPC...");
        } else {
            SkinHandler.getAndApplyFromName(sender, skin, (PlayerNPC) npc);
            Utils.normalMessage(sender, "Fetching username and applying the skin to this NPC...");
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 2) {
            return List.of("<username>", "<url>");
        } else return List.of("[id]");
    }
}
