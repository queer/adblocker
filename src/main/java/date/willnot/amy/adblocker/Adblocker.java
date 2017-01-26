/**
 * Copyright 2016-eternity audrey
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package date.willnot.amy.adblocker;

import lombok.Getter;
import org.bukkit.ChatColor;
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
