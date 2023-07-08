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
import dev.th3shadowbroker.ouroboros.mines.drops.DropGroup;
import dev.th3shadowbroker.ouroboros.mines.events.ValidateMaterialIdentifierEvent;
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MineableMaterial {

    private final MaterialIdentifier materialIdentifier;

    private final MaterialIdentifier[] replacements;

    private final Random replRandom = new Random();

    private final Range cooldown;

    private final double richChance;

    private final Range richAmount;

    private final Range experience;

    private final Range depositExperience;

    private final String dropGroup;

    private final Map<String, Object> properties;

    public MineableMaterial(MaterialIdentifier materialIdentifier, MaterialIdentifier[] replacements, Range cooldown, double richChance, Range richAmount, Range experience, Range depositExperience, String dropGroup, Map<String, Object> properties) {
        this.materialIdentifier = materialIdentifier;
        this.replacements = replacements;
        this.cooldown = cooldown;
        this.richChance = richChance;
        this.richAmount = richAmount;
        this.experience = experience;
        this.depositExperience = depositExperience;
        this.dropGroup = dropGroup;
        this.properties = properties;
    }

    public Material getMaterial() {
        return materialIdentifier.getVanillaMaterial().orElse(Material.STONE);
    }

    public MaterialIdentifier getMaterialIdentifier() {
        return materialIdentifier;
    }

    public MaterialIdentifier[] getReplacements() {
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

    public MaterialIdentifier getReplacement() {
        if (replacements.length == 1) return replacements[0];
        return replacements[replRandom.nextInt(replacements.length)];
    }

    public Optional<DropGroup> getDropGroup() {
        return dropGroup == null ? Optional.empty() : OuroborosMines.INSTANCE.getDropManager().getDropGroup(dropGroup);
    }

    public Optional<String> getDropGroupName() {
        return Optional.ofNullable(dropGroup);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public static MineableMaterial fromSection(ConfigurationSection section) throws InvalidMineMaterialException {
        //Parse material
        MaterialIdentifier materialIdentifier = MaterialIdentifier.valueOf(section.getName());

        //Parse replacement-materials
        List<MaterialIdentifier> replacementMaterials = new ArrayList<>();
        if (!section.isSet("replacements")) { throw new InvalidMineMaterialException( String.format("The mine-material %s defines not replacements. Skipping.", section.getName()) ); }
        for (String materialName : section.getStringList("replacements"))
        {
            var identifier = MaterialIdentifier.valueOf(materialName);
            var isCustom = !identifier.isInDefaultNamespace();

            // Vanilla materials
            if (!isCustom) {
                var material = identifier.getVanillaMaterial();
                if (material.isPresent())
                    replacementMaterials.add(identifier);
                else
                    OuroborosMines.INSTANCE.getLogger().warning( String.format("The mine-material %s defines an invalid replacement-material: %s", section.getName(), materialName) );

            // Custom materials
            } else {
                var validationEvent = new ValidateMaterialIdentifierEvent(identifier);
                Bukkit.getPluginManager().callEvent(validationEvent);
                replacementMaterials.add(identifier);
                //if (validationEvent.isValid())
                  //  replacementMaterials.add(identifier);
                //else
                  //  OuroborosMines.INSTANCE.getLogger().warning( String.format("The custom mine-material %s defines an invalid replacement-material: %s", section.getName(), materialName) );
            }
        }

        //Fail when replacements materials failed to parse
        if (replacementMaterials.isEmpty()) { throw new InvalidMineMaterialException( String.format("All materials of the mine-material %s failed to parse.", section.getName()) ); }

        //Parse cooldown to allow random cooldowns in given range
        Range cooldown;
        try {
            cooldown = Range.fromString(section.getString("cooldown"));
        } catch (NumberFormatException ex) {
            throw new InvalidMineMaterialException( String.format("Unable to parse the cooldown of %s from string \"%s\"", materialIdentifier.getName(), section.getString("cooldown")) );
        } catch (Exception ex) {
            throw new InvalidMineMaterialException( ex.getMessage() );
        }

        //Parse richness amount
        Range richAmount = null;
        if (section.isSet("rich-amount")) {
            try {
                richAmount = Range.fromString(section.getString("rich-amount"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the rich-amount %s from %s", materialIdentifier.getName(), section.getString("rich-amount")));
            }
        }

        //Parse experience for the material itself
        Range experience = null;
        if (section.isSet("experience")) {
            try {
                experience = Range.fromString(section.getString("experience"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the the experience for %s from %s", materialIdentifier.getName(), section.getString("experience")));
            }
        }

        //Parse experience for deposits of the material
        Range depositExperience = null;
        if (section.isSet("rich-experience")) {
            try {
                depositExperience = Range.fromString(section.getString("rich-experience"));
            } catch (Exception ex) {
                throw new InvalidMineMaterialException(String.format("Unable to parse the the experience for %s from %s", materialIdentifier.getName(), section.getString("rich-experience")));
            }
        }

        //Parse drop group
        String dropGroup = section.getString("dropGroup", null);

        return new MineableMaterial(

                //Material and replacements
                materialIdentifier,
                replacementMaterials.toArray(MaterialIdentifier[]::new),

                cooldown,
                section.getDouble("rich-chance", 0),

                //Add range-values
                richAmount == null ? Range.zero() : richAmount,
                experience == null ? Range.zero() : experience,
                depositExperience == null ? Range.zero() : depositExperience,

                //Drop group
                dropGroup,

                //Properties
                section.isConfigurationSection("properties") ? section.getConfigurationSection("properties").getValues(false) : new HashMap<>()
        );
    }
/*
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> values = new HashMap<>();

        values.put("replacements", Arrays.asList(replacements));
        values.put("cooldown", cooldown.toString());

        if (richChance > 0) values.put("rich-chance", richChance);
        if (!richAmount.isZero()) values.put("rich-amount", richAmount);
        if (!experience.isZero()) values.put("experience", experience.toString());
        if (!depositExperience.isZero()) values.put("depositExperience", depositExperience);
        if (dropGroup != null) values.put("dropGroup", dropGroup);
        if (!properties.isEmpty()) values.put("properties", properties);

        return values;
    }

    public static MineableMaterial deserialize(Map<String, Object> values) {

    }
*/
}
