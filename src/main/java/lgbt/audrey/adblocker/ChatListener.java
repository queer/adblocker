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
package lgbt.audrey.adblocker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 10/2/16.
 */
class ChatListener implements Listener {
    /**
     * Matches proper URLs.
     */
    private final Pattern urlRegex = Pattern.compile("(http(s)*://)*(([A-Za-z_0-9-]+)(\\.|,|\\((\\.|,)\\)))*([A-Za-z_0-9-]+)(\\.|,|\\((\\.|,)\\))([A-Za-z]+)(:[0-9]{1,5})*");
    /**
     * Matches IP addresses
     */
    private final Pattern ipRegex = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,| |\\((\\.|,)\\))([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,| |\\((\\.|,)\\))([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,)([01]?\\d\\d?|2[0-4]\\d|25[0-5])(:[0-9]{1,5})*");
    
    private final Adblocker plugin;

    public ChatListener(final Adblocker plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Does the magic
     *
     * @param event The event to magick
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if(event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final String message = event.getMessage().trim().toLowerCase();
        Result result = Result.NOT_FOUND;
        Matcher matcher = urlRegex.matcher(message);
        if(matcher.find()) {
            result = Result.FOUND;
        } else {
            matcher = ipRegex.matcher(message);
            if(matcher.find()) {
                result = Result.FOUND;
            }
        }
        final Map<String, String> foundMap = new ConcurrentHashMap<>();
        if(result == Result.FOUND) {
            final String match = matcher.group();
            if(matcher.group(matcher.groupCount()) != null) {
                final String IP = match.substring(0, match.lastIndexOf(':'));
                final int in_1 = matcher.start(matcher.groupCount());
                final int in_2 = matcher.end(matcher.groupCount());
                final String PORT = message.substring(in_1 + 1, in_2);
                boolean success = true;
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(PORT);
                } catch(final NumberFormatException ex) {
                    success = false;
                }
                foundMap.put(IP.replace(",", ".").replace("(", "").replace(")", ""), success ? PORT : "25565");
            } else {
                foundMap.put(match.replace(",", ".").replace("(", "").replace(")", ""), "25565");
            }
            player.sendMessage(plugin.getAdCheckMessage());
            for(final Entry<String, String> entr : foundMap.entrySet()) {
                if(plugin.getWhitelist().stream()
                        .filter(e -> e.toLowerCase().contains(entr.getKey().toLowerCase())).count() > 0) {
                    continue;
                }
                final boolean blacklist = plugin.getBlacklist().stream()
                        .filter(e -> e.toLowerCase().contains(entr.getKey().toLowerCase())).count() > 0;
                Server server = null;
                final Collection<String> matches = new ArrayList<>();
                if(!blacklist) {
                    server = new Server(entr.getKey(), entr.getValue());
                } else {
                    matches.addAll(plugin.getBlacklist().stream()
                            .filter(e -> e.toLowerCase().contains(entr.getKey().toLowerCase())).collect(Collectors.toList()));
                }
                if(blacklist || server.isOnline()) {
                    final Server fServer = server;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            () -> {
                                for(final String e : plugin.getPunishmentCommands()) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), e);
                                }
                                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("adblocker.notify") || p.isOp())
                                        .forEach(p -> {
                                            p.sendMessage(ChatColor.RED + event.getPlayer().getName() +
                                                    ChatColor.GRAY + " potentially tried to advertise: '"
                                                    + event.getMessage() + "'!");
                                            p.sendMessage(ChatColor.RED + "Info:");
                                            if(blacklist) {
                                                p.sendMessage(ChatColor.GRAY + "Blacklist: ");
                                                matches.forEach(e -> p.sendMessage(ChatColor.GRAY + " * " + ChatColor.RED + e));
                                            } else {
                                                p.sendMessage(ChatColor.GRAY + "Server: " + ChatColor.RED + fServer.getIp());
                                                p.sendMessage(ChatColor.GRAY + "Description: ");
                                                p.sendMessage(ChatColor.RED + fServer.getLastResponse().getDescription());
                                                p.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.RED +
                                                        fServer.getLastResponse().getVersion().getName());
                                                p.sendMessage(ChatColor.GRAY + "Players: " + ChatColor.RED +
                                                        fServer.getLastResponse().getPlayers().getOnline() + ChatColor.GRAY + '/' +
                                                        ChatColor.RED + fServer.getLastResponse().getPlayers().getMax());
                                            }
                                        });
                            }, 1L);
                    event.setCancelled(true);
                    return;
                }
                player.sendMessage(plugin.getNotAdMessage());
            }
        }
    }
    
    private enum Result {
        FOUND, NOT_FOUND
    }
}
