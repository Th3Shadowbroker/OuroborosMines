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

    private final long cooldown;

    private final long cooldownMax;

    private final double richChance;

    private final int richAmountMin;

    private final int richAmountMax;

    public MineableMaterial(Material material, Material[] replacements, long cooldown, long cooldownMax, double richChance, int richAmountMin, int richAmountMax) {
        this.material = material;
        this.replacements = replacements;
        this.cooldown = cooldown;
        this.cooldownMax = cooldownMax;
        this.richChance = richChance;
        this.richAmountMin = richAmountMin;
        this.richAmountMax = richAmountMax;
    }

    public Material getMaterial() {
        return material;
    }

    public Material[] getReplacements() {
        return replacements;
    }

    public int getDepositMin() {
        return richAmountMin;
    }

    public int getDepositMax() {
        return richAmountMax;
    }

    public boolean canBeRich() {
        return richChance > 0 && richAmountMin > 0 && richAmountMax > 0;
    }

    public long getCooldown() {
        //     Cooldowns equal         Regular cooldown  Generate a random cooldown in range of the given values
        return cooldown == cooldownMax ? cooldown * 20 : (cooldown + replRandom.nextInt((int) cooldownMax + 1)) * 20;
    }

    public int getDrawnRichness() {
        if (richChance > 0 && richAmountMin > 0) {
            double rndNumber = 1 + replRandom.nextDouble() * 100;
            if (rndNumber <= richChance) {
                return richAmountMin == richAmountMax ? richAmountMin : richAmountMin + replRandom.nextInt(richAmountMax - richAmountMin);
            }
        }
        return 0;
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
        String[] cooldownStr;
        int cooldown;
        int cooldownMax;

        try {
            cooldownStr = section.getString("cooldown").split("-");
            cooldown = Integer.parseInt(cooldownStr[0]);
            cooldownMax = cooldownStr.length > 1 ? Integer.parseInt(cooldownStr[1]) : cooldown;
        } catch (NumberFormatException ex) {
            throw new InvalidMineMaterialException( String.format("Unable to parse the cooldown of %s from string \"%s\"", material.get().name(), section.getString("cooldown")) );
        } catch (Exception ex) {
            throw new InvalidMineMaterialException( ex.getMessage() );
        }

        //Parse richness amount
        String[] richnessAmountStr;
        int richnessAmountMin = 0;
        int richnessAmountMax = 0;
        if (section.isSet("rich-amount")) {
            try {
                richnessAmountStr = section.getString("rich-amount").split("-");
                richnessAmountMin = Integer.parseInt(richnessAmountStr[0]);
                richnessAmountMax = richnessAmountStr.length > 1 ? Integer.parseInt(richnessAmountStr[1]) : richnessAmountMin;
            } catch (NumberFormatException ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the rich-amount %s from %s", material.get().name(), section.getString("rich-amount")));
            }
        }

        return new MineableMaterial(material.get(), replacementMaterials.toArray( new Material[replacementMaterials.size()] ), cooldown, cooldownMax, section.getDouble("rich-chance", 0), richnessAmountMin, richnessAmountMax);
    }

}
