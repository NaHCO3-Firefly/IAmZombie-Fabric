package dev.molang.iamzombieq;

import dev.molang.iamzombieq.entity.HerobrineEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class IAmZombieEntities {
    public static final EntityType<HerobrineEntity> HEROBRINE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.of(IAmZombieMod.MOD_ID, "herobrine"),
            EntityType.Builder.of(HerobrineEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .noSave()
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.of(IAmZombieMod.MOD_ID, "herobrine")))
    );

    private IAmZombieEntities() {
    }

    public static void register() {
        IAmZombieMod.LOGGER.debug("Registered entities");
    }
}
