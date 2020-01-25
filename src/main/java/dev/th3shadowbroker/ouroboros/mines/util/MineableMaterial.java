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

    private final Random replRandom = new Random();

    private final Range cooldown;

    private final double richChance;

    private final Range richAmount;

    private final Range experience;

    private final Range depositExperience;

    public MineableMaterial(Material material, Material[] replacements, Range cooldown, double richChance, Range richAmount, Range experience, Range depositExperience) {
        this.material = material;
        this.replacements = replacements;
        this.cooldown = cooldown;
        this.richChance = richChance;
        this.richAmount = richAmount;
        this.experience = experience;
        this.depositExperience = depositExperience;
    }

    public Material getMaterial() {
        return material;
    }

    public Material[] getReplacements() {
        return replacements;
    }

    public int getDepositMin() {
        return richAmount.getMin();
    }

    public int getDepositMax() {
        return richAmount.getMax();
    }

    public Range getRichAmount() {
        return richAmount;
    }

    public boolean canBeRich() {
        return richChance > 0 && !richAmount.isZero();
    }

    public long getCooldown() {
        //     Cooldowns equal         Regular cooldown  Generate a random cooldown in range of the given values
        return !cooldown.isRange() ? cooldown.getMin() * 20 : cooldown.getRandomWithin() * 20;
    }

    public int getDrawnRichness() {
        if (richChance > 0 && !richAmount.isZero()) {
            double rndNumber = 1 + replRandom.nextDouble() * 100;
            if (rndNumber <= richChance) {
                return !richAmount.isRange() ? richAmount.getMin() : richAmount.getRandomWithin();
            }
        }
        return 0;
    }

    public Range getExperience() {
        return experience;
    }

    public Range getDepositExperience() {
        return depositExperience;
    }

    public Material getReplacement() {
        if (replacements.length == 1) return replacements[0];
        return replacements[replRandom.nextInt(replacements.length)];
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

        //Parse cooldown to allow random cooldowns in given range
        Range cooldown;
        try {
            cooldown = Range.fromString(section.getString("cooldown"));
        } catch (NumberFormatException ex) {
            throw new InvalidMineMaterialException( String.format("Unable to parse the cooldown of %s from string \"%s\"", material.get().name(), section.getString("cooldown")) );
        } catch (Exception ex) {
            throw new InvalidMineMaterialException( ex.getMessage() );
        }

        //Parse richness amount
        Range richAmount = null;
        if (section.isSet("rich-amount")) {
            try {
                richAmount = Range.fromString(section.getString("rich-amount"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the rich-amount %s from %s", material.get().name(), section.getString("rich-amount")));
            }
        }

        //Parse experience for the material itself
        Range experience = null;
        if (section.isSet("experience")) {
            try {
                experience = Range.fromString(section.getString("experience"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the the experience for %s from %s", material.get().name(), section.getString("experience")));
            }
        }

        //Parse experience for deposits of the material
        Range depositExperience = null;
        if (section.isSet("rich-experience")) {
            try {
                depositExperience = Range.fromString(section.getString("rich-experience"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the the experience for %s from %s", material.get().name(), section.getString("rich-experience")));
            }
        }

        return new MineableMaterial(

                //Material and replacements
                material.get(),
                replacementMaterials.toArray( new Material[replacementMaterials.size()] ),

                cooldown,
                section.getDouble("rich-chance", 0),

                //Add range-values
                richAmount == null ? Range.zero() : richAmount,
                experience == null ? Range.zero() : experience,
                depositExperience == null ? Range.zero() : depositExperience

        );
    }

}
