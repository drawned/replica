package me.drawn.replica.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {
    String getName();
    List<String> getAliases();
    String getDescription();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);

    default boolean playerOnly() {
        return false;
    }
}