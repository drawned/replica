package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.*;
import me.drawn.replica.npc.custom.ModelEngineNPC;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CloneCommand implements ICommand {
    @Override
    public String getName() {
        return "clone";
    }

    @Override
    public List<String> getAliases() {
        return List.of("copy");
    }

    @Override
    public String getDescription() {
        return "Clones a specific NPC.";
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

        final NPCData oldData = npc.getNPCData();

        final NPCData npcData = new NPCData(p.getLocation(), oldData.name())
                .withHologramEnabled(oldData.hologramEnabled())
                .withUUID(UUID.randomUUID())
                .withType(oldData.npcType())
                .withSkinTexture(oldData.skinTexture())
                .withHologramLines(oldData.hologramLines())
                .withModelEngine(oldData.modelEngine());

        final NPCType type = npcData.npcType();

        NPC newNPC;

        final int nextID = NPCHandler.getNextID();

        //noinspection SwitchStatementWithTooFewBranches
        switch(type) {
            case MODEL_ENGINE -> newNPC = new ModelEngineNPC(nextID, npcData);
            default -> {
                newNPC = new PlayerNPC(nextID, npcData);
                ((PlayerNPC)newNPC).forceSpawn(p);
            }
        }

        NPCManagement.selectNPC(p, newNPC);

        Utils.normalMessage(sender, "Cloned NPC "+ MainCommands.npcFormat(npc)+" into new NPC "+MainCommands.npcFormat(newNPC)+" " +
                "and selected it.");

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("[id]");
    }
}
