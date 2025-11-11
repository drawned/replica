package me.drawn.replica.commands;

import me.drawn.replica.Replica;
import me.drawn.replica.commands.custom.*;
import me.drawn.replica.npc.*;
import me.drawn.replica.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class MainCommands implements TabExecutor {

    public static final String divider = Utils.YELLOW_COLOR+"⁎ §8§m                                   §r "+Utils.YELLOW_COLOR+"⁎";

    private final Map<String, ICommand> commandMap = new HashMap<>();

    public MainCommands() {
        register(new InfoCommand());
        register(new ActionCommand());
        register(new SelectCommand());
        register(new CreateCommand());
        register(new ListCommand());
        register(new CloneCommand());
        register(new SkinCommand());
        register(new SaveCommand());
        register(new ScaleCommand());
        register(new TypeCommand());
        register(new ModelCommand());
        register(new HologramCommand());
        register(new TpCommand());
        register(new ForceSpawnCommand());
        register(new TphereCommand());
        register(new DeleteCommand());
        register(new LookCloseCommand());
        register(new MigrateFromCommand());
    }

    private void register(ICommand command) {
        commandMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commandMap.put(alias.toLowerCase(), command);
        }
    }

    public static NPC getNPCFromArgsOrSelected(CommandSender sender, String[] args, int index) {
        NPC npc = null;
        if(sender instanceof Player p) {
            npc = NPCManagement.getSelectedNPC(p);
        }
        if(args.length > index) {
            try {
                npc = NPCHandler.getNpcFromId(Integer.parseInt(args[index])).orElse(npc);
            } catch (NumberFormatException ignored) {}
        }
        return npc;
    }

    public static String formatLocation(Location loc) {
        return "("+loc.getWorld().getName()+") x: "+loc.getX()+", y: "+loc.getY()+", z: "+loc.getZ();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player p) {
            p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);
        }

        if (args.length == 1) {
            return commandMap.values().stream().map(ICommand::getName).distinct().toList();
        }

        ICommand sub = commandMap.get(args[0].toLowerCase());
        if(sub == null) return List.of();
        if(!sender.hasPermission("replica.command."+sub.getName())) return List.of();

        return sub.tabComplete(sender, args);
    }

    public static String npcFormat(NPC npc) {
        return Utils.YELLOW_COLOR+"("+npc.getId()+") "+npc.getName()+"§f";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(divider);

            Utils.normalMessage(sender, "Running Replica NPC v" + Replica.getInstance().getDescription().getVersion());
            for (ICommand sc : new HashSet<>(commandMap.values())) {
                sendCommandUsage(sender, label, sc);
            }

            sender.sendMessage(divider);
            return true;
        }

        ICommand sub = commandMap.get(args[0].toLowerCase());
        if(sub == null) {
            Utils.warningMessage(sender, "Unknown command. Use /"+label+" for help.");
            return true;
        }

        if(!sender.hasPermission("replica.command."+sub.getName())) {
            Utils.warningMessage(sender, "You must have the" + " permission 'replica.command."+sub.getName()+"' to use this command.");
            return true;
        }

        if (sub.playerOnly() && !(sender instanceof Player)) {
            Utils.warningMessage(sender, "You must be a player to use this command.");
            return true;
        }

        return sub.execute(sender, args);
    }

    static void sendCommandUsage(final CommandSender sender, final String mainCommand,
                                        final ICommand subCommand) {
        final String command = "/"+mainCommand+" "+subCommand.getName();

        BaseComponent baseComponent = new TextComponent(command);
        baseComponent.setColor(net.md_5.bungee.api.ChatColor.of("#ffc933"));
        baseComponent.setUnderlined(true);
        baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§fAliases: "+subCommand.getAliases())));
        baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));

        if(!subCommand.getUsage().isBlank()) {
            BaseComponent args = new TextComponent(" "+subCommand.getUsage());
            args.setColor(net.md_5.bungee.api.ChatColor.of("#ffc933"));
            args.setUnderlined(false);
            baseComponent.addExtra(args);
        }

        BaseComponent separator = new TextComponent(" - ");
        separator.setColor(ChatColor.DARK_GRAY);
        separator.setUnderlined(false);

        BaseComponent description = new TextComponent(subCommand.getDescription());
        description.setColor(ChatColor.GRAY);

        baseComponent.addExtra(separator);
        baseComponent.addExtra(description);

        sender.spigot().sendMessage(baseComponent);
    }
}
