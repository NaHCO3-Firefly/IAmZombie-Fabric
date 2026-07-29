package dev.molang.iamzombieq;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class IAmZombieRegistries {
    private IAmZombieRegistries() {
    }

    public static void register() {
        IAmZombieBlocks.register();
        IAmZombieItems.register();
        IAmZombieEntities.register();
        IAmZombieAttachments.register();
        // Attribute registration
        FabricDefaultAttributeRegistry.register(
                IAmZombieEntities.HEROBRINE,
                dev.molang.iamzombieq.entity.HerobrineEntity.createAttributes().build()
        );
    }
}
