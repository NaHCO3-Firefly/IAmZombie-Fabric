package dev.molang.iamzombieq.api.event;

import dev.molang.iamzombieq.rules.DeathOutcome;
import dev.molang.iamzombieq.rules.core.ZombieState;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Cancellable event fired BEFORE a death-driven evolution ("向死而生") is applied (design §5.a), e.g. inside
 * {@code IZombiePlayer.evolveFromDeath}. Cancel it to veto the evolution.
 *
 * <p>The before/after {@link ZombieState}s and the {@link DeathOutcome} are immutable snapshots.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public final class ZombieEvolvePreEvent implements ZombieEvent, Cancellable {

    private final ServerPlayer player;
    private final ZombieState before;
    private final ZombieState after;
    private final DeathOutcome outcome;
    private boolean canceled;

    public ZombieEvolvePreEvent(@NotNull ServerPlayer player, @NotNull ZombieState before,
            @NotNull ZombieState after, @NotNull DeathOutcome outcome) {
        this.player = player;
        this.before = before;
        this.after = after;
        this.outcome = outcome;
    }

    @NotNull
    public ServerPlayer player() {
        return player;
    }

    @NotNull
    public ZombieState before() {
        return before;
    }

    @NotNull
    public ZombieState after() {
        return after;
    }

    @NotNull
    public DeathOutcome outcome() {
        return outcome;
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
