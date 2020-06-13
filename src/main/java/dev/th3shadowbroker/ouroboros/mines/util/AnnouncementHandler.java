/*
 * Copyright 2020 Jens Fischer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.th3shadowbroker.ouroboros.mines.util;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementHandler implements Listener {

    private final Range openingHours;

    private final OuroborosMines plugin;

    private final Map<World, List<AnnouncementRunnable>> tasks;

    private final List<String> broadcastWorlds;

    public AnnouncementHandler() {
        this.plugin = OuroborosMines.INSTANCE;
        this.openingHours = Range.fromString(plugin.getConfig().getString("openingHours.time", "0-12000"));
        this.tasks = new HashMap<>();
        this.broadcastWorlds = plugin.getConfig().getStringList("openingHours.announcements.worlds");

        schedule(Bukkit.getWorlds().stream().filter(world -> broadcastWorlds.contains(world.getName())).toArray(World[]::new));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void schedule() {
        schedule(Bukkit.getWorlds().stream().toArray(World[]::new));
    }

    public void schedule(World... worlds) {
        for (World world : worlds) {
            schedule(world, world.getTime());
        }
    }

    public void schedule(World world, long time) {
        //System.out.println(String.format("World: %s | Time: %s", world.getName(), time));
        boolean announceOpening = plugin.getConfig().getBoolean("openingHours.announcements.opening");
        boolean announceClosing = plugin.getConfig().getBoolean("openingHours.announcements.closing");

        // Register announcement for opening
        if (announceOpening) {
            long delay = TimeUtils.getDifference(openingHours.getMin(), time);
            registerAnnouncementTask(
                    world,
                    AnnouncementRunnable.schedule(
                            delay,
                            TimeUtils.DAY_END,
                            world,
                            ChatColor.translateAlternateColorCodes('&', TemplateMessage.from("chat.messages.announcements.opening").colorize().toString())
                    )
            );
            //System.out.println(String.format("Scheduled opening announcement with a delay of %s", delay));
        }

        // Register announcement for closing
        if (announceClosing) {
            long delay = TimeUtils.getDifference(openingHours.getMax(), time);
            registerAnnouncementTask(
                    world,
                    AnnouncementRunnable.schedule(
                            delay,
                            TimeUtils.DAY_END,
                            world,
                            ChatColor.translateAlternateColorCodes('&', TemplateMessage.from("chat.messages.announcements.closing").colorize().toString())
                    )
            );
            //System.out.println(String.format("Scheduled closing announcement with a delay of %s", delay));
        }
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) { ;
        //System.out.println("Timeskip: " + event.getSkipAmount());
        if (broadcastWorlds.contains(event.getWorld().getName())) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    tasks.keySet().forEach(world -> {
                        clearTasks(world);
                        schedule(world);
                    });
                }
            }, 1);
        }
    }

    public Range getOpeningHours() {
        return openingHours;
    }

    public void registerAnnouncementTask(World world, AnnouncementRunnable runnable) {
        if (!tasks.containsKey(world)) tasks.put(world, new ArrayList<>());
        tasks.get(world).add(runnable);
    }

    public void clearTasks(World world) {
        if (tasks.containsKey(world)) {
            tasks.get(world).forEach(AnnouncementRunnable::cancel);
            tasks.get(world).clear();
        }
    }

}
