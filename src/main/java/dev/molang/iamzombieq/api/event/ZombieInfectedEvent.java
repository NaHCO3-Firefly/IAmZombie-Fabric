package dev.molang.iamzombieq.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Observer event fired AFTER a zombie player has infected/transformed another entity (design §5.a). Not
 * cancellable; the {@code attacker}/{@code victim} are live entity references to be read, not mutated.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public final class ZombieInfectedEvent implements ZombieEvent {

    private final ServerPlayer attacker;
    private final LivingEntity victim;
    private final EntityType<?> resultType;

    public ZombieInfectedEvent(@NotNull ServerPlayer attacker, @NotNull LivingEntity victim,
            @NotNull EntityType<?> resultType) {
        this.attacker = attacker;
        this.victim = victim;
        this.resultType = resultType;
    }

    @NotNull
    public ServerPlayer attacker() {
        return attacker;
    }

    @NotNull
    public LivingEntity victim() {
        return victim;
    }

    @NotNull
    public EntityType<?> resultType() {
        return resultType;
    }
}
