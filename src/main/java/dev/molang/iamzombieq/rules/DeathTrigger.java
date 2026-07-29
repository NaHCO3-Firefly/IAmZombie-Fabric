package dev.molang.iamzombieq.rules;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public enum DeathTrigger {
    DROWNING,
    STARVATION,
    SUNLIGHT,
    LAVA,
    OTHER;

    public static DeathTrigger fromSource(DamageSource source) {
        if (source.is(DamageTypes.DROWN)) return DROWNING;
        if (source.is(DamageTypes.STARVE)) return STARVATION;
        if (source.is(DamageTypes.LAVA)) return LAVA;
        if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)) {
            // Check if it's sunlight-sourced fire
            return SUNLIGHT;
        }
        // Check custom sunlight damage type
        if (source.is(net.minecraft.resources.ResourceKey.create(
                Registries.DAMAGE_TYPE,
                dev.molang.iamzombieq.util.ModIds.id("sunlight")))) {
            return SUNLIGHT;
        }
        return OTHER;
    }
}
