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

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MineableMaterial {

    private final Material material;

    private final Material[] replacements;

    private final long cooldown;

    public MineableMaterial(Material material, Material[] replacements, long cooldown) {
        this.material = material;
        this.replacements = replacements;
        this.cooldown = cooldown;
    }

    public Material getMaterial() {
        return material;
    }

    public Material[] getReplacements() {
        return replacements;
    }

    public long getCooldown() {
        return cooldown * 20;
    }

    public Material getReplacement() {
        if (replacements.length == 1) return replacements[0];

        Random rnd = new Random();
        return replacements[rnd.nextInt(replacements.length)];
    }

    public static MineableMaterial fromSection(ConfigurationSection section) throws InvalidMineMaterialException {
        //Parse material
        Optional<Material> material = Optional.ofNullable(Material.getMaterial(section.getName().toUpperCase()));
        if (!material.isPresent()) { throw new InvalidMineMaterialException( String.format("The mine-material %s failed to parse.", section.getName()) ); }

        //Parse replacement-materials
        List<Material> replacementMaterials = new ArrayList<>();
        if (!section.isSet("replacements")) { throw new InvalidMineMaterialException( String.format("The mine-material %s defines not replacements. Skipping.", section.getName()) ); }
        for (String materialName : section.getStringList("replacements"))
        {
            Optional<Material> parsedMaterial = Optional.ofNullable( Material.getMaterial(materialName.toUpperCase()) );
            if (parsedMaterial.isPresent())
            {
                replacementMaterials.add(parsedMaterial.get());
            }
            else
            {
                OuroborosMines.INSTANCE.getLogger().warning( String.format("The mine-material %s defines an invalid replacement-material: %s", section.getName(), materialName) );
            }
        }

        //Fail when replacements materials failed to parse
        if (replacementMaterials.isEmpty()) { throw new InvalidMineMaterialException( String.format("All materials of the mine-material %s failed to parse.", section.getName()) ); }

        return new MineableMaterial(material.get(), replacementMaterials.toArray( new Material[replacementMaterials.size()] ), section.getInt("cooldown"));
    }
}
