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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.Optional;

public class TriggeredEffect {

    private final Sound sound;
    private final float soundVolume;
    private final float soundPitch;

    private final Particle particle;
    private final int particleCount;
    private final float particleSize;
    private final Color particleColor;
    private final Pattern particlePattern;

    private final Trigger trigger;

    public TriggeredEffect(Sound sound, float soundVolume, float soundPitch, Particle particle, int particleCount, float particleSize, Color particleColor, Pattern particlePattern, Trigger trigger) {
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleSize = particleSize;
        this.particleColor = particleColor;
        this.particlePattern = particlePattern;
        this.trigger = trigger;
    }

    public Optional<Sound> getSound() {
        return Optional.ofNullable(sound);
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public Optional<Particle> getParticle() {
        return Optional.ofNullable(particle);
    }

    public int getParticleCount() {
        return particleCount;
    }

    public float getParticleSize() {
        return particleSize;
    }

    public Color getParticleColor() {
        return particleColor;
    }

    public Pattern getParticlePattern() {
        return particlePattern;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void playAt(Location location) {
        getSound().ifPresent( sound -> Optional.ofNullable(location.getWorld()).ifPresent(world -> world.playSound(location, sound, soundVolume, soundPitch)) );

        getParticle().ifPresent( particle -> Optional.ofNullable(location.getWorld()).ifPresent( world -> {
            Location blockCenter = location.add(location.getX() > 0 ? 0.5:-0.5,0.5,location.getZ() > 0 ? 0.5:-0.5);
            switch (particlePattern) {
                default:
                case CENTER:
                    world.spawnParticle(particle, blockCenter, particleCount, particle == Particle.REDSTONE ? new Particle.DustOptions(particleColor, particleSize) : null);
                    break;
            }
        }));
    }

    public static TriggeredEffect fromSection(ConfigurationSection section) {
        TriggeredEffect.Trigger trigger = TriggeredEffect.Trigger.valueOf(section.getName().toUpperCase());

        Sound sound = section.isSet("sound") ? Sound.valueOf(section.getString("sound").toUpperCase()) : null;
        float soundVolume = (float) section.getDouble("sound-volume", 1);
        float soundPitch = (float) section.getDouble("sound-pitch", 0);

        Particle particle = section.isSet("particle") ? Particle.valueOf(section.getString("particle").toUpperCase()) : null;
        int particleCount = particle == null ? 0 : section.getInt("particle-amount", 5);
        Color particleColor = Color.fromRGB(section.getInt("particle-color.r", 0), section.getInt("particle-color.g", 0), section.getInt("particle-color.b", 0));
        float particleSize = (float) section.getDouble("particle-size", 1);
        Pattern particlePattern = Pattern.valueOf(section.getString("particle-pattern", "center").toUpperCase());

        return new TriggeredEffect(sound, soundVolume, soundPitch, particle, particleCount, particleSize, particleColor, particlePattern, trigger);
    }

    public static enum Trigger {
        DEPOSIT_DISCOVERED
    }

    public static enum Pattern {
        CENTER,
        BORDERS,
        COVER
    }

}
