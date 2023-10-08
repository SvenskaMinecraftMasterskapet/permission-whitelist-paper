package org.smm.permissionwhitelistpaper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PWPCommand extends Command {
    private final Main main;

    public PWPCommand(Main main) {
        super("pwp", "Permission Whitelist Paper command", "/pwp <subcommand> [args]", List.of("permissionwhitelistpaper"));
        this.main = main;
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if(args.length == 0) {
            commandSender.sendMessage("§cUsage: /pwp <subcommand> [args]");
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")) {
            main.reloadConfig();
            commandSender.sendMessage("§aReloaded config.");
            return true;
        }
        return false;
    }
    
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return List.of("reload");
    }
    
}
