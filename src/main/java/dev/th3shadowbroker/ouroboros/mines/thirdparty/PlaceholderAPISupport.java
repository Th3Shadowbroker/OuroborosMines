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

package dev.th3shadowbroker.ouroboros.mines.thirdparty;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.util.AnnouncementRunnable;
import dev.th3shadowbroker.ouroboros.mines.util.RegionConfiguration;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class PlaceholderAPISupport extends PlaceholderExpansion {

    public static final String PLUGIN_NAME = "PlaceholderAPI";

    @Override
    public String getIdentifier() {
        return "ouroborosmines";
    }

    @Override
    public String getAuthor() {
        return OuroborosMines.INSTANCE.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return OuroborosMines.INSTANCE.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        String[] fragments = identifier.split("_");
        if (fragments.length == 0) return "";

        switch (fragments[0]) {

            // For remaining time
            case "oh":
            case "opening":
                String regionWorld = fragments[1];
                String regionId = fragments[2];

                // Check remaining time based on runnables that match in world and region.
                long remainingTime = 0;
                List<AnnouncementRunnable> matchingRunnables = OuroborosMines.INSTANCE.getAnnouncementManager().getRunnablesForRegion(regionWorld, regionId);
                if (!matchingRunnables.isEmpty()) {

                    // Get regional configuration
                    Optional<RegionConfiguration> regionConfiguration = OuroborosMines.INSTANCE.getMaterialManager().getMineableMaterialOverrides().stream().filter(rc -> rc.getWorld().getName().equals(regionWorld) && rc.getRegionId().equals(regionId)).findFirst();
                    if (regionConfiguration.isPresent()) {

                        // Mines aren't already opened
                        if (!regionConfiguration.get().minesAreOpen()) {
                            // Find closest timer
                            for (AnnouncementRunnable runnable : matchingRunnables) {
                                if (remainingTime == 0 || runnable.getRemainingTime() < remainingTime) {
                                    remainingTime = runnable.getRemainingTime();
                                }
                            }
                            //                      ms -> s s->m m->h
                            long h = (remainingTime / 1000  / 60 / 60) % 24;
                            long m = (remainingTime / 1000 / 60) % 60;
                            long s = (remainingTime / 1000) % 60;

                            //@TODO Testing not finished. Remaining cases:
                            // - Single opening hour

                            return String.format("%02d:%02d:%02d", h , m, s);
                        } else {
                            return "Now!";
                        }
                    }
                }

            // Not specified
            default:
                return "";
        }
    }

}
