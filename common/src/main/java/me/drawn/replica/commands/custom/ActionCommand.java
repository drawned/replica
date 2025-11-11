package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.NPCInteraction;
import me.drawn.replica.npc.NPCManagement;
import me.drawn.replica.npc.enums.InteractionType;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ActionCommand implements ICommand {
    @Override
    public String getName() {
        return "action";
    }

    @Override
    public List<String> getAliases() {
        return List.of("interaction", "cmd", "command", "actions");
    }

    @Override
    public String getDescription() {
        return "Allows you to add custom actions when interacting with NPCs.";
    }

    @Override
    public String getUsage() {
        return "<add, remove, list> <right_click,left_click> [command]";
    }

    @Override
    public boolean playerOnly() {return true;}

    public static String interactionFormat(final NPCData npcData, final NPCInteraction npcInteraction) {
        final int index = npcData.getInteractions().indexOf(npcInteraction);

        return Utils.YELLOW_COLOR+"("+index+") "+npcInteraction.getInteractionType()+"§7: "+npcInteraction.getCommand();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        final NPC npc = NPCManagement.getSelectedNPC(p);
        if (npc == null) {
            Utils.warningMessage(sender, "You must have an NPC selected.");
            return true;
        }

        if (args.length < 2) {
            sendUsageMessage(sender);
            return true;
        }

        final String option = args[1].toLowerCase();
        final NPCData npcData = npc.getNPCData();

        switch (option) {
            case "list" -> {
                if (npcData.getInteractions().isEmpty()) {
                    Utils.warningMessage(sender, "This NPC has no interactions yet.");
                    return true;
                }

                Utils.normalMessage(sender, "Listing actions for this NPC:");
                npcData.getInteractions().forEach(inter -> {
                    Utils.normalMessage(sender, interactionFormat(npcData, inter));
                });
                return true;
            }

            case "remove" -> {
                if (args.length < 3) {
                    Utils.warningMessage(sender, "You must provide the index (position) of the command you want to remove.");
                    return true;
                }

                int index;
                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    Utils.warningMessage(sender, "Invalid index. Please provide a number.");
                    return true;
                }

                List<NPCInteraction> interactions = npcData.getInteractions();
                if (index < 0 || index >= interactions.size()) {
                    Utils.warningMessage(sender, "No action found at index " + index + ". Remember the first action is always the number '0'.");
                    return true;
                }

                final NPCInteraction removed = interactions.remove(index);
                Utils.normalMessage(sender, "Removed action: " + interactionFormat(npcData, removed));
                return true;
            }

            case "add" -> {
                if (args.length < 4) {
                    Utils.warningMessage(sender, "Usage: /action add <right_click|left_click> <command>");
                    return true;
                }

                final String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length))
                        .replaceFirst("^/", ""); // remove barra inicial

                InteractionType type = InteractionType.get(args[2]);
                if(type == null) {
                    Utils.warningMessage(sender, "Invalid interaction trigger. Valid values: "
                            + Utils.YELLOW_COLOR + Arrays.toString(InteractionType.values()));
                    return true;
                }

                final NPCInteraction newInteraction = new NPCInteraction(command, type);
                npcData.addInteraction(newInteraction);

                Utils.normalMessage(sender, "Added new custom action: " + interactionFormat(npcData, newInteraction));
                return true;
            }

            default -> {
                sendUsageMessage(sender);
                return true;
            }
        }
    }

    private void sendUsageMessage(CommandSender sender) {
        Utils.warningMessage(sender, "Usage: /action <add|remove|list> <args>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) return null;

        if (args.length == 2)
            return List.of("add", "remove", "list");

        if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
            return (args.length == 3)
                    ? Arrays.stream(InteractionType.values()).map(Enum::name).toList()
                    : List.of("<command>");
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
            NPC npc = NPCManagement.getSelectedNPC(p);
            return (npc != null && npc.getNPCData().hasInteractions())
                    ? IntStream.range(0, npc.getNPCData().getInteractions().size())
                    .mapToObj(String::valueOf)
                    .toList()
                    : List.of();
        }

        return List.of();
    }
}
