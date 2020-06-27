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
import dev.th3shadowbroker.ouroboros.mines.util.TemplateMessage;
import dev.th3shadowbroker.ouroboros.mines.util.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class OmCommand implements CommandExecutor {

    private final String consoleNotAllowed = TemplateMessage.from("chat.messages.consoleOnly").colorize().toString();

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
                            if (args.length == 1) {
                                Optional<ApplicableRegionSet> regionSet = WorldUtils.getPlayerRegions(player);
                                Optional<ProtectedRegion> region = regionSet.flatMap(WorldUtils::getTopRegion);
                                if (region.isPresent()) {
                                    createRegionConfiguration(region.get(), player);
                                } else {
                                    sender.sendMessage(TemplateMessage.from("chat.messages.regionNotFound").colorize().toString());
                                }
                            } else {
                                String regionId = args[1];
                                Optional<World> world = Optional.ofNullable(args.length >= 3 ? Bukkit.getWorld(args[2]) : player.getWorld());
                                if (world.isPresent()) {
                                    Optional<ProtectedRegion> region = WorldUtils.getRegion(regionId, world.get());
                                    if (region.isPresent()) {
                                        createRegionConfiguration(region.get(), player);
                                    } else {
                                        sender.sendMessage(TemplateMessage.from("chat.messages.regionNotFound").colorize().toString());
                                    }
                                } else {
                                    sender.sendMessage(TemplateMessage.from("chat.messages.worldNotFound").insert("world", args[2]).colorize().toString());
                                }
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
                        try {
                            sender.sendMessage(TemplateMessage.from("chat.messages.reloadingConfig").colorize().toString());
                            plugin.reloadConfig();
                            sender.sendMessage(TemplateMessage.from("chat.messages.reloadingRegionConfigurations").colorize().toString());
                            plugin.getMaterialManager().reloadRegionConfigurations();
                            sender.sendMessage(TemplateMessage.from("chat.messages.reloadedRegionConfigurations").insert("count", String.valueOf(plugin.getMaterialManager().getMineableMaterialOverrides().size())).colorize().toString());
                            plugin.getAnnouncementManager().flush();
                        } catch (Exception ex) {
                            sender.sendMessage(TemplateMessage.from("chat.messages.error").colorize().insert("error", ex.getMessage()).toString());
                        }
                    } else {
                        sender.sendMessage(cmd.getPermissionMessage());
                    }
                    break;

                default:
                    sender.sendMessage(TemplateMessage.from("chat.messages.unrecognizedArgument").colorize().toString());
                    sender.sendMessage(cmd.getUsage());
                    break;
            }
        }
        return true;
    }

    private void createRegionConfiguration(ProtectedRegion region, Player player) {
        if (!RegionConfiguration.configExists(region.getId(), player.getWorld().getName())) {
            new RegionConfiguration(region.getId(), player.getWorld().getName());
            player.sendMessage(TemplateMessage.from("chat.messages.regionCustomize").insert("region", region.getId()).colorize().toString());
        } else {
            player.sendMessage(TemplateMessage.from("chat.messages.regionAlreadyCustomized").colorize().toString());
        }
    }
}
