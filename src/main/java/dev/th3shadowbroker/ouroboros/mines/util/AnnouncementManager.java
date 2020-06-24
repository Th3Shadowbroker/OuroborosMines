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

import java.util.*;

public class AnnouncementManager {

    private final Map<RegionConfiguration, List<AnnouncementRunnable>> taskMap;

    public AnnouncementManager() {
        this.taskMap = new HashMap<>();
    }

    public void createTasks() {
        OuroborosMines.INSTANCE.getMaterialManager().getMineableMaterialOverrides().forEach(this::registerRegionalAnnouncements);
    }

    private void registerRegionalAnnouncements(RegionConfiguration regionConfiguration) {
        Optional<OpeningHours> regionalOpeningHours = regionConfiguration.getOpeningHours();

        regionalOpeningHours.ifPresent(
                openingHours -> {

                    // Skip if opening-hours are disabled
                    if (!openingHours.isEnabled()) return;

                    // Opening announcement
                    if (openingHours.isAnnounceOpening()) {
                        String openingMessage = TemplateMessage.from("chat.messages.announcements.opening", regionConfiguration.getConfiguration()).colorize().toString();
                        List<Duration> realtimeRanges = openingHours.getRealtimeRange();

                        // Entry missing?
                        if (!taskMap.containsKey(regionConfiguration)) taskMap.put(regionConfiguration, new ArrayList<>());

                        long delay;
                        // Realtime should be used
                        if (!realtimeRanges.isEmpty()) {
                            for (Duration realtimeRange : realtimeRanges) {
                                // Get it ready!
                                delay = realtimeRange.getTicksUntilStart();
                                taskMap.get(regionConfiguration).add(AnnouncementRunnable.schedule(delay, Duration.DAY_SECONDS, openingMessage, openingHours.getAnnouncementWorlds()));
                            }
                        }

                        // Use game-time (in ticks)
                        else {
                            // Get it read! (But with a different time system)
                            delay = TimeUtils.getDifference(openingHours.getTickRange().getMin(), regionConfiguration.getWorld().getTime());
                            taskMap.get(regionConfiguration).add(AnnouncementRunnable.schedule(delay, TimeUtils.DAY_END, openingMessage, openingHours.getAnnouncementWorlds()));
                        }
                    }

                    // Closing announcement
                    if (openingHours.isAnnounceClosing()) {
                        String closingMessage = TemplateMessage.from("chat.messages.announcements.closing", regionConfiguration.getConfiguration()).colorize().toString();
                        List<Duration> realtimeRanges = openingHours.getRealtimeRange();

                        // Entry missing?
                        if (!taskMap.containsKey(regionConfiguration)) taskMap.put(regionConfiguration, new ArrayList<>());

                        long delay;
                        // Realtime should be used
                        if (!realtimeRanges.isEmpty()) {
                            for (Duration realtimeRange : realtimeRanges) {
                                // Get it ready!
                                delay = realtimeRange.getTicksUntilEnd();
                                taskMap.get(regionConfiguration).add(AnnouncementRunnable.schedule(delay, Duration.DAY_SECONDS * 20, closingMessage, openingHours.getAnnouncementWorlds()));
                            }
                        }

                        // Use game-time (in ticks)
                        else {
                            // Get it read! (But with a different time system)
                            delay = TimeUtils.getDifference(openingHours.getTickRange().getMax(), regionConfiguration.getWorld().getTime());
                            taskMap.get(regionConfiguration).add(AnnouncementRunnable.schedule(delay, TimeUtils.DAY_END, closingMessage, openingHours.getAnnouncementWorlds()));
                        }
                    }
                }
        );
    }

    private void clearTasks() {
        taskMap.keySet().forEach(this::clearTasks);
    }

    private void clearTasks(RegionConfiguration regionConfiguration) {
        Optional<List<AnnouncementRunnable>> announcementRunnables = Optional.ofNullable(taskMap.get(regionConfiguration));
        announcementRunnables.ifPresent(runnables -> runnables.forEach(AnnouncementRunnable::cancel));
        announcementRunnables.ifPresent(List::clear);
    }

    public void flush() {
        clearTasks();
        taskMap.clear();
        createTasks();
    }

    public boolean hasAny() {
        return taskMap.size() > 0;
    }

}
