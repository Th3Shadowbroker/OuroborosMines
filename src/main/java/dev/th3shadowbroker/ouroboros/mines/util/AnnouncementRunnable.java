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

import java.util.*;

public class AnnouncementRunnable implements Runnable {

    private final List<World> worlds;

    private final String message;

    private BukkitTask task;

    private Date executionTime;

    private long delay;

    private long period;

    public AnnouncementRunnable(Date executionTime, String message, List<World> worlds) {
        this.executionTime = executionTime;
        this.worlds = worlds;
        this.message = message;
    }

    @Override
    public void run() {
        //System.out.println(String.format("Executable ran for %s", world.getName()));
        if (worlds.size() > 0) {
            worlds.forEach(world -> world.getPlayers().forEach(player -> player.sendMessage(message)));
        } else {
            Bukkit.getServer().broadcastMessage(message);
        }
        resetExecutionTime();
    }

    private void resetExecutionTime() {
        Calendar calendar = TimeUtils.getCalendarWithTimezone();
        calendar.add(Calendar.SECOND, (int) (period == TimeConstants.INGAME_DAY_TICKS ? TimeConstants.INGAME_DAY_TICKS / 20 : TimeConstants.REALTIME_DAY_SECONDS));
        this.executionTime = calendar.getTime();
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

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getRemainingTime() {
        long r = executionTime.getTime() - TimeUtils.now().getTime();
        return r >= 0 ? r : 0;
    }

    public void cancel() {
        task.cancel();
        //System.out.println(String.format("Scheduled announcement: \"%s\" in world \"%s\" has been cancelled.", message, world.getName()));
    }

    public static AnnouncementRunnable schedule(long delay, long period, String message, List<String> worlds) {

        List<World> parsedWorlds = new ArrayList<>();
        worlds.forEach(worldName -> {
            Optional<World> world = Optional.ofNullable(Bukkit.getServer().getWorld(worldName));
            world.ifPresent(parsedWorlds::add);
        });

        Calendar timeCalendar = TimeUtils.getCalendarWithTimezone();
        timeCalendar.add(Calendar.SECOND, (int) delay / 20);

        AnnouncementRunnable runnable = new AnnouncementRunnable(timeCalendar.getTime(), message, parsedWorlds);
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimer(OuroborosMines.INSTANCE, runnable, delay, period);
        runnable.setTask(task);
        runnable.setDelay(delay);
        runnable.setPeriod(period);
        return runnable;
    }

}
