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

package dev.th3shadowbroker.ouroboros.mines.next.materials;

import dev.th3shadowbroker.ouroboros.mines.next.exceptions.NMaterialIdentifierParsingException;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Optional;

public final class NMaterialIdentifier {

    @Getter
    private final String namespace;

    @Getter
    private final String name;

    public static final String DEFAULT_NAMESPACE = "minecraft";

    public NMaterialIdentifier(String name) {
        this(DEFAULT_NAMESPACE, name);
    }

    public NMaterialIdentifier(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public boolean isCustom() {
        return !namespace.equals(DEFAULT_NAMESPACE);
    }

    public Optional<Material> getVanillaMaterial() {
        return Optional.ofNullable(Material.getMaterial(name, false));
    }

    @Override
    public String toString() {
        return String.format("%s:%s", namespace, name);
    }

    public static NMaterialIdentifier parse(String string) throws NMaterialIdentifierParsingException {
        String[] fragments = string.split(":");
        if (fragments.length > 0 && fragments.length < 3) {
            String namespace = fragments.length > 1 ? fragments[0] : DEFAULT_NAMESPACE;
            String name = fragments[fragments.length > 1 ? 1 : 0];
            return new NMaterialIdentifier(namespace.toLowerCase(), name.toLowerCase());
        } else {
            throw new NMaterialIdentifierParsingException(string);
        }
    }

}
