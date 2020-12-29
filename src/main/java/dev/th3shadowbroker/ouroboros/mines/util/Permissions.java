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

import org.bukkit.permissions.Permission;

public enum Permissions {

    ROOT("ouroboros.mines.*"),
    COMMAND_ROOT("ouroboors.mines.command.*"),
    COMMAND_INFO("ouroboros.mines.command.info"),
    COMMAND_CUSTOMIZE("ouroboros.mines.command.customize"),
    COMMAND_RELOAD("ouroboros.mines.command.reload"),
    COMMAND_DROP_GROUP("ouroboros.mines.command.dropgroup"),
    FEATURE_AUTO_PICKUP("ouroboros.mines.autopickup");

    public final String name;

    public final Permission permission;

    Permissions(String name) {
        this.name = name;
        this.permission = new Permission(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
