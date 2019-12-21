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

package dev.th3shadowbroker.ouroboros.mines.commands;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.util.Permissions;
import dev.th3shadowbroker.ouroboros.mines.util.RegionConfiguration;
import dev.th3shadowbroker.ouroboros.mines.util.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class OmCommand implements CommandExecutor {

    private final String consoleNotAllowed = "This command can only be executed ingame!";

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission(Permissions.COMMAND_INFO.permission)) {
                sender.sendMessage(OuroborosMines.PREFIX + plugin.getName() + " v" + plugin.getDescription().getVersion());
            } else {
                sender.sendMessage(cmd.getPermissionMessage());
            }
        } else {
            switch (args[0].toLowerCase())
            {
                case "customize":
                    if (sender.hasPermission(Permissions.COMMAND_CUSTOMIZE.permission)) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            Optional<ApplicableRegionSet> regionSet = WorldUtils.getPlayerRegions(player);
                            Optional<ProtectedRegion> region = regionSet.flatMap(WorldUtils::getTopRegion);
                            if (region.isPresent()) {
                                if (!RegionConfiguration.configExists(region.get().getId(), player.getWorld().getName())) {
                                    new RegionConfiguration(region.get().getId(), player.getWorld().getName());
                                    sender.sendMessage(OuroborosMines.PREFIX + "§2A new configuration file for §b" + region.get().getId() + " §2has been created!");
                                } else {
                                    sender.sendMessage(OuroborosMines.PREFIX + "§cThere's already a configuration file for this region!");
                                }
                            } else {
                                sender.sendMessage(OuroborosMines.PREFIX + "§cNo region found at your position!");
                            }
                        } else {
                            sender.sendMessage(consoleNotAllowed);
                        }
                    } else {
                        sender.sendMessage(cmd.getPermissionMessage());
                    }
                    break;

                case "reload":
                    if (sender.hasPermission(Permissions.COMMAND_RELOAD.permission)) {
                        sender.sendMessage(OuroborosMines.PREFIX + "§2Reloading configuration...");
                        plugin.reloadConfig();
                        sender.sendMessage(OuroborosMines.PREFIX + "§2Reloading region-configurations...");
                        plugin.getMaterialManager().reloadRegionConfigurations();
                        sender.sendMessage(OuroborosMines.PREFIX + "§2Loaded " + plugin.getMaterialManager().getMineableMaterialOverrides().size() + " region-specific configurations");
                    } else {
                        sender.sendMessage(cmd.getPermissionMessage());
                    }
                    break;

                default:
                    sender.sendMessage(OuroborosMines.PREFIX + "§cUnrecognized argument");
                    sender.sendMessage(cmd.getUsage());
                    break;
            }
        }
        return true;
    }
}
