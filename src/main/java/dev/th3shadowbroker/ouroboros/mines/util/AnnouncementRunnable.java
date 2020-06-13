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
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class AnnouncementRunnable implements Runnable {

    private final World world;

    private final String message;

    private BukkitTask task;

    private long delay;

    public AnnouncementRunnable(World world, String message) {
        this.world = world;
        this.message = message;
    }

    @Override
    public void run() {
        //System.out.println(String.format("Executable ran for %s", world.getName()));
        world.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void cancel() {
        task.cancel();
        //System.out.println(String.format("Scheduled announcement: \"%s\" in world \"%s\" has been cancelled.", message, world.getName()));
    }

    public static AnnouncementRunnable schedule(long delay, long period, World world, String message) {
        AnnouncementRunnable runnable = new AnnouncementRunnable(world, message);
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimer(OuroborosMines.INSTANCE, runnable, delay, period);
        runnable.setTask(task);
        runnable.setDelay(delay);
        return runnable;
    }

}
