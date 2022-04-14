/*
 * Copyright 2022 Jens Fischer
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

import lombok.Getter;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Optional;

public class MaterialIdentifier {

    public static final String DEFAULT_NAMESPACE = "minecraft";

    @Getter
    private final String namespace;

    @Getter
    private final String name;

    public MaterialIdentifier(String name) {
        this(DEFAULT_NAMESPACE, name);
    }

    public MaterialIdentifier(String namespace, String name) {
        this.namespace = namespace.toLowerCase();
        this.name = name.toLowerCase();
    }

    public boolean isInDefaultNamespace() {
        return namespace.equals(DEFAULT_NAMESPACE);
    }

    public Optional<Material> getVanillaMaterial() {
        return Arrays.stream(Material.values())
                     .filter(material -> material.name().equalsIgnoreCase(name))
                     .findFirst();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", namespace, name);
    }

    public static MaterialIdentifier valueOf(String string) {
        String[] fragments = string.split(":");

        String namespace = fragments.length > 1 ? fragments[0] : DEFAULT_NAMESPACE;
        String name = fragments.length > 1 ? fragments[1] : fragments[0];

        return new MaterialIdentifier(namespace, name);
    }

}
