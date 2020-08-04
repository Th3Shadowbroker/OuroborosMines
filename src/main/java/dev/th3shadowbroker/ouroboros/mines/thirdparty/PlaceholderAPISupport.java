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
import dev.th3shadowbroker.ouroboros.mines.util.TemplateMessage;
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

        String regionWorld = fragments.length >= 2 ? fragments[1] : null;
        String regionId = fragments.length >= 3 ? fragments[2] : null;

        switch (fragments[0]) {

            // For remaining time
            case "oh":
            case "opening":
                // Abort if there are not enough fragments.
                if (fragments.length >= 3) {

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

                                long h = (remainingTime / 1000 / 60 / 60) % 24;
                                long m = (remainingTime / 1000 / 60) % 60;
                                long s = (remainingTime / 1000) % 60;

                                String result = new TemplateMessage(OuroborosMines.INSTANCE.getConfig().getString("placeholders.openingHours.format", "%h%:%m%:%s%"))
                                        .insert("h", String.format("%02d", h))
                                        .insert("m", String.format("%02d", m))
                                        .insert("s", String.format("%02d", s))
                                        .colorize().toRawString();

                                return result;
                            } else {
                                return TemplateMessage.from("placeholders.openingHours.open").colorize().toRawString();
                            }
                        }
                    } else {
                        return "Missing opening hours for " + regionId;
                    }
                } else {
                    return "Invalid placeholder!";
                }

            case "name":
            case "n":
                // Abort if there are not enough fragments.
                if (fragments.length >= 3) {
                    Optional<RegionConfiguration> regionConfiguration = OuroborosMines.INSTANCE.getMaterialManager().getMineableMaterialOverrides().stream().filter(rc -> rc.getWorld().getName().equals(regionWorld) && rc.getRegionId().equals(regionId)).findFirst();;
                    if (regionConfiguration.isPresent()) {
                        return regionConfiguration.get().getConfiguration().getString("name", regionConfiguration.get().getRegionId());
                    }
                } else {
                    return "Invalid placeholder!";
                }

            // Not specified
            default:
                return "Invalid placeholder!";
        }
    }

}
