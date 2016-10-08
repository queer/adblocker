package lgbt.audrey.adblocker;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author audrey
 * @since 10/2/16.
 */
public class Adblocker extends JavaPlugin {
    @Getter
    private List<String> punishmentCommands;
    @Getter
    private String adCheckMessage;
    @Getter
    private String notAdMessage;
    @Getter
    private List<String> blacklist;
    @Getter
    private List<String> whitelist;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getCommand("adblock").setExecutor((commandSender, command, s, args) -> {
            if(args.length != 1) {
                return false;
            } else if(!args[0].equalsIgnoreCase("reload")) {
                return false;
            } else {
                reloadConfig();
                loadConfig();
                return true;
            }
        });
    }

    private void loadConfig() {
        punishmentCommands = new ArrayList<>(getConfig().getStringList("punishment-commands"));
        adCheckMessage = getConfig().getString("ad-check-message", "&7Are you advertising? Give us a minute to check...");
        notAdMessage = getConfig().getString("not-ad-message", "&7You're not advertising. Sorry about that.");
        blacklist = getConfig().getStringList("blacklist");
        whitelist = getConfig().getStringList("whitelist");
        for(int i = 0; i < punishmentCommands.size(); i++) {
            punishmentCommands.set(i, ChatColor.translateAlternateColorCodes('&', punishmentCommands.get(i)));
        }
        adCheckMessage = ChatColor.translateAlternateColorCodes('&', adCheckMessage);
        notAdMessage = ChatColor.translateAlternateColorCodes('&', notAdMessage);
    }
}
