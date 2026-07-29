package dev.molang.iamzombieq.platform;

import org.jetbrains.annotations.ApiStatus;

/**
 * Portability seam for reading, writing, and network-syncing player attachment data. The Fabric implementation
 * delegates to Cardinal Components API; abstracting it here lets the internal facade
 * ({@code internal.core.ServerZombiePlayer}) stay loader-agnostic.
 *
 * <p>This interface is consumed only by internal code. The existing gameplay handlers still call the raw
 * attachment API directly and are not migrated in Phase-1.
 */
@ApiStatus.Internal
public interface AttachmentService {

    /**
     * Reads the current value of {@code key} on {@code holder}, returning {@code defaultValue} if absent.
     */
    <T> T get(Object holder, String key, T defaultValue);

    /**
     * Writes {@code value} for {@code key} on {@code holder}.
     */
    <T> void set(Object holder, String key, T value);

    /**
     * Pushes the current value of {@code key} to the owning client. This is a no-op for a connectionless player
     * (e.g. a FakePlayer), which is the FakePlayer-safety guarantee relied on.
     */
    void sync(Object holder, String key);
}
