package org.smm.permissionwhitelistpaper;

import net.luckperms.api.LuckPermsProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getCommandMap().register("pwp", new PWPCommand(this));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void beforeJoin(org.bukkit.event.player.PlayerPreLoginEvent event) {
        if(!getConfig().getBoolean("enabled")) return;
        if(event.getResult() != org.bukkit.event.player.PlayerPreLoginEvent.Result.ALLOWED) return;
        if(event.getResult() == org.bukkit.event.player.PlayerPreLoginEvent.Result.KICK_WHITELIST) return;
        if(event.getResult() == org.bukkit.event.player.PlayerPreLoginEvent.Result.KICK_FULL) return;
        if(event.getResult() == org.bukkit.event.player.PlayerPreLoginEvent.Result.KICK_BANNED) return;

        debug("Checking " + event.getName() + " (" + event.getUniqueId() + ")");
        
        // Check if the player is op
        if(getConfig().getBoolean("bypass.op")) {
            if(getServer().getOfflinePlayer(event.getUniqueId()).isOp()) {
                debug("Player is op, allowing.");
                event.allow();
                return;
            }
        }
        
        // Check if the player is whitelisted
        if(getConfig().getBoolean("bypass.whitelist")) {
            if(getServer().getWhitelistedPlayers().contains(getServer().getOfflinePlayer(event.getUniqueId()))) {
                debug("Player is whitelisted, allowing.");
                event.allow();
                return;
            }
        }
        
        // Check if the player has joined before
        if(getConfig().getBoolean("bypass.joined-before")) {
            if(getServer().getOfflinePlayer(event.getUniqueId()).hasPlayedBefore()) {
                debug("Player has joined before, allowing.");
                event.allow();
                return;
            }
        }
        
        // Check if the player has permission from luckperms
        var user = LuckPermsProvider.get().getUserManager().loadUser(event.getUniqueId()).join();
        if(user == null) {
            debug("Failed to load user data, disallowing.");
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cAn error occurred while loading your user data. Please try again later.");
            return;
        }
        var allNodes = user.resolveInheritedNodes(user.getQueryOptions());
        final String globalPermission = getConfig().getString("permissions.global");
        final String serverPermission = getConfig().getString("permissions.server");
        var hasGlobalPermission = allNodes.stream().anyMatch(node -> node.getKey().equalsIgnoreCase(globalPermission));
        var hasServerPermission = allNodes.stream().anyMatch(node -> node.getKey().equalsIgnoreCase(serverPermission.replace("%name%", getServer().getName())));
        var hasConfigServerPermission = allNodes.stream().anyMatch(node -> node.getKey().equalsIgnoreCase(serverPermission.replace("%name%", getConfig().getString("server-name"))));
        if(!hasGlobalPermission) {
            debug("Player doesn't have global permission, disallowing.");
            event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, getConfig().getString("kick-messages.no-global-permission"));
            return;
        }
        if(!hasServerPermission && !hasConfigServerPermission) {
            debug("Player doesn't have server permission, disallowing.");
            event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, getConfig().getString("kick-messages.no-server-permission"));
            return;
        }
        event.allow();
    }
    
    public void debug(String msg) {
        if(getConfig().getBoolean("debug")) getLogger().info(msg);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
