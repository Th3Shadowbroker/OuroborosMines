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

package dev.th3shadowbroker.ouroboros.mines.drops.types;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandDrop extends AbstractDrop {

    private final List<String> commands;

    public CommandDrop(double chance, List<String> commands) {
        super(chance);
        this.commands = commands;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void drop(Player player, Location blockLocation) {
        Server server = OuroborosMines.INSTANCE.getServer();
        Optional<Plugin> placeholderApi = Optional.ofNullable(Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
        commands.forEach(command -> {
            String injectedCommand = command.replaceAll("%player%", player.getName());
            server.dispatchCommand(server.getConsoleSender(), placeholderApi.isPresent() ? PlaceholderAPI.setPlaceholders(player, injectedCommand) : injectedCommand);
        });
    }

    public static CommandDrop fromSection(ConfigurationSection section) {
        double chance = section.getDouble("chance", 1);
        return new CommandDrop(chance, section.isString("commands") ? Collections.singletonList(section.getString("commands")) : section.getStringList("commands"));
    }

}
