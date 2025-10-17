package me.drawn.replica.commands.custom;

import me.drawn.replica.commands.ICommand;
import me.drawn.replica.commands.MainCommands;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

public class ListCommand implements ICommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public List<String> getAliases() {
        return List.of("l", "all");
    }

    @Override
    public String getDescription() {
        return "Shows a list of all NPCs currently loaded.";
    }

    @Override
    public String getUsage() {
        return "[page] [--active]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int page = 1;

        boolean onlyActive = false;

        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }

            if(args.length >= 3) {
                if(args[2].equalsIgnoreCase("--active"))
                    onlyActive = true;
            }
        }

        List<NPC> sorted = NPCHandler.npcs.stream()
                .sorted(Comparator.comparingInt(NPC::getId))
                .toList();

        if(onlyActive) {
            sorted = sorted.stream()
                    .filter(NPC::isActive)
                    .toList();
        }

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) sorted.size() / pageSize);

        if (page < 1 || page > totalPages) {
            Utils.warningMessage(sender, "This page does not exist. You can only view the page between 1 and " + totalPages);
            return true;
        }

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, sorted.size());
        List<NPC> pageSelected = sorted.subList(fromIndex, toIndex);

        sender.sendMessage(MainCommands.divider);
        Utils.normalMessage(sender, "There are " + sorted.size() + " NPCs loaded! - Page (" + page + " / " + totalPages + ")");

        for (NPC npc : pageSelected) {
            sendNPCListInfo(sender, npc);
        }

        sender.sendMessage(MainCommands.divider);

        return false;
    }

    static void sendNPCListInfo(final CommandSender sender, final NPC npc) {
        BaseComponent npcInfo = new TextComponent("⌂ ("+npc.getId()+") "+npc.getName());
        npcInfo.setColor(npc.isActive() ? ChatColor.of("#ffc933") : ChatColor.of("#c4752b"));

        BaseComponent separator = new TextComponent(" - ");
        separator.setColor(ChatColor.DARK_GRAY);

        BaseComponent infoAction = new TextComponent(" [ⓘ]");
        infoAction.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rnpc info "+npc.getId()));
        infoAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to view NPC info.")));
        infoAction.setColor(ChatColor.WHITE);

        BaseComponent tpAction = new TextComponent(" [\uD83C\uDF0D]");
        tpAction.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rnpc tp "+npc.getId()));
        tpAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to teleport to this NPC.")));
        tpAction.setColor(ChatColor.AQUA);

        BaseComponent selectAction = new TextComponent(" [☑]");
        selectAction.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rnpc select "+npc.getId()));
        selectAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to select this NPC.")));
        selectAction.setColor(ChatColor.GRAY);

        npcInfo.addExtra(separator);
        npcInfo.addExtra(infoAction);
        npcInfo.addExtra(tpAction);
        npcInfo.addExtra(selectAction);

        sender.spigot().sendMessage(npcInfo);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 2) {
            return List.of("[page]");
        } else return List.of("[--active]");
    }
}
