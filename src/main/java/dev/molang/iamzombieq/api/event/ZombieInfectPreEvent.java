package dev.molang.iamzombieq.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Cancellable event fired BEFORE a zombie player infects/transforms another entity (design §5.a), e.g. a villager
 * -> zombie villager or a pig/piglin -> zombified piglin conversion. Cancel it to veto the infection.
 *
 * <p>The {@code attacker} and {@code victim} are live entity references — treat them as read-only within the
 * listener. {@code resultType} is the entity type the victim is converting into.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public final class ZombieInfectPreEvent implements ZombieEvent, Cancellable {

    private final ServerPlayer attacker;
    private final LivingEntity victim;
    private final EntityType<?> resultType;
    private boolean canceled;

    public ZombieInfectPreEvent(@NotNull ServerPlayer attacker, @NotNull LivingEntity victim,
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

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
