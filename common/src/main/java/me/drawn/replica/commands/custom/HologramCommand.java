package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.custom.NPCHologram;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramCommand implements ICommand {
    @Override
    public String getName() {
        return "hologram";
    }

    @Override
    public List<String> getAliases() {
        return List.of("name");
    }

    @Override
    public String getDescription() {
        return "Allows you to enable or disable the name hologram of an NPC.";
    }

    @Override
    public String getUsage() {
        return "[--autoscale | id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        NPC npc = MainCommands.getNPCFromArgsOrSelected(sender, args, 1);

        if(npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected or provide a valid NPC ID.");
            return true;
        }

        final NPCData npcData = npc.getNPCData();

        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("--autoscale")) {
                boolean current = npcData.hologramAutoScale();
                if(current) {
                    Utils.normalMessage(sender, "Toggling off NPC Hologram name auto scale.");
                    npcData.setHologramAutoScale(false);
                } else {
                    Utils.normalMessage(sender, "Toggling on NPC Hologram name auto scale.");
                    npcData.setHologramAutoScale(true);
                }
                if(npc.getHologram() != null)
                    npc.getHologram().recalculate();
                return true;
            }
        }

        if(npcData.hologramEnabled()) {
            npcData.withHologramEnabled(false);

            if(npc.getHologram() != null)
                npc.getHologram().removeAll();

            Utils.normalMessage(sender, "Hologram for NPC "+MainCommands.npcFormat(npc)+" disabled!");
        } else {
            npcData.withHologramEnabled(true);

            NPCHologram hologram = npc.getHologram();
            if(hologram == null) {
                npc.initializeHologram();
                hologram = npc.getHologram();
            }

            for(Player player : npc.getAudiencePlayers()) {
                hologram.spawn(player);
            }

            Utils.normalMessage(sender, "Hologram for NPC "+MainCommands.npcFormat(npc)+" enabled!");
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]", "--autoscale");
    }
}
