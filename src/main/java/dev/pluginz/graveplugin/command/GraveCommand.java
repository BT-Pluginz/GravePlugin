package dev.pluginz.graveplugin.command;


import dev.pluginz.graveplugin.GravePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GraveCommand implements CommandExecutor {
    private final GravePlugin plugin;

    public GraveCommand(GravePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO: I guess, everything?
        return false;
    }
}
