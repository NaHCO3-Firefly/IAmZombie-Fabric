package dev.molang.iamzombieq;

import dev.molang.iamzombieq.state.IAmZombieAttachments;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class IAmZombieRegistries {
    private IAmZombieRegistries() {
    }

    public static void register() {
        IAmZombieBlocks.register();
        IAmZombieItems.register();
        IAmZombieEntities.register();
        IAmZombieAttachments.initialize();
        // Attribute registration
        FabricDefaultAttributeRegistry.register(
                IAmZombieEntities.HEROBRINE,
                dev.molang.iamzombieq.entity.HerobrineEntity.createAttributes().build()
        );
    }
}
