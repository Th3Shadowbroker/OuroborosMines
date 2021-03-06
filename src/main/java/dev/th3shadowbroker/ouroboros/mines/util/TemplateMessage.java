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
import dev.th3shadowbroker.ouroboros.mines.thirdparty.PlaceholderAPISupport;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class TemplateMessage {

    private String template;

    public TemplateMessage(String template) {
        this.template = template;
    }

    public TemplateMessage colorize() {
        template = ChatColor.translateAlternateColorCodes('&', template);
        return this;
    }

    public TemplateMessage decolorize() {
        template = ChatColor.stripColor(colorize().template);
        return this;
    }

    public TemplateMessage insert(String key, String replacement) {
        template = template.replace("%" + key + "%", replacement);
        return this;
    }

    @Override
    public String toString() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled(PlaceholderAPISupport.PLUGIN_NAME)) {
            return OuroborosMines.PREFIX + template;
        } else {
            return OuroborosMines.PREFIX + PlaceholderAPI.setPlaceholders(null, template);
        }
    }

    public String toRawString() {
        return template;
    }

    public static TemplateMessage from(String templatePath, FileConfiguration alternativeConfiguration) {
        if (alternativeConfiguration.isSet(templatePath)) {
            return new TemplateMessage(alternativeConfiguration.getString(templatePath, "&cNo message found for &f" + templatePath + "&c!"));
        }
        return TemplateMessage.from(templatePath);
    }

    public static TemplateMessage from(String templatePath) {
        return new TemplateMessage(OuroborosMines.INSTANCE.getConfig().getString(templatePath, "&cNo message found for &f" + templatePath + "&c!"));
    }

}
