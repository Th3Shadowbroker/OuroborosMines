/*
 * Copyright 2019 Jens Fischer
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
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TaskManager {

    private final List<ReplacementTask> tasks = Collections.synchronizedList(new ArrayList<>());

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;


    public void register(ReplacementTask task) {
        tasks.add(task);
    }

    public void unregister(ReplacementTask task) {
        tasks.remove(task);
    }

    public boolean hasPendingReplacementTask(Block block) {
        return tasks.stream().anyMatch(replacementTask -> WorldUtils.compareLocations(replacementTask.getBlock().getLocation(), block.getLocation()));
    }

    public void flush() {
        List<ReplacementTask> tasks = new ArrayList<>(this.tasks);

        plugin.getLogger().info(String.format("Executing %s pending tasks...", tasks.size()));
        for (int i = 0; i < tasks.size(); i++) {
            ReplacementTask replacementTask = tasks.get(i);
            replacementTask.getTask().cancel();
            plugin.getLogger().info(String.format("Executing task (Id: %s) [%s/%s]", replacementTask.getTask().getTaskId(),i + 1, tasks.size() ));
            replacementTask.run();
        }
    }

}
