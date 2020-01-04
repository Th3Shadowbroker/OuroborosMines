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

package dev.th3shadowbroker.ouroboros.mines.listeners;

import dev.th3shadowbroker.ouroboros.mines.events.DepositDiscoveredEvent;
import dev.th3shadowbroker.ouroboros.mines.util.TemplateMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DepositDiscoveryListener implements Listener {

    @EventHandler
    public void onDepositDiscovered(DepositDiscoveredEvent event) {
        event.getPlayer().sendMessage(TemplateMessage.from("chat.messages.depositDiscovered").insert("material", event.getMineableMaterial().getMaterial().name().toLowerCase().replace("_"," ")).colorize().toString());
    }

}
