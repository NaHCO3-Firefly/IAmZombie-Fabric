package dev.molang.iamzombieq.platform.neoforge;

import dev.molang.iamzombieq.platform.AttachmentService;
import org.jetbrains.annotations.ApiStatus;

/**
 * Placeholder — NeoForge is no longer supported. This file is retained only to satisfy references
 * during the Fabric port transition.
 */
@ApiStatus.Internal
@Deprecated
public final class NeoForgeAttachmentService implements AttachmentService {

    @Override
    public <T> T get(Object holder, String key, T defaultValue) {
        return defaultValue;
    }

    @Override
    public <T> void set(Object holder, String key, T value) {
    }

    @Override
    public void sync(Object holder, String key) {
    }
}
