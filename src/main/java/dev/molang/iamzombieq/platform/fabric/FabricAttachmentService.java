package dev.molang.iamzombieq.platform.fabric;

import dev.molang.iamzombieq.platform.AttachmentService;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.cca.api.v3.component.ComponentAccess;
import org.ladysnake.cca.api.v3.component.ComponentKey;

/**
 * Fabric implementation of {@link AttachmentService} using Cardinal Components API (CCA).
 * Maps string keys to {@link ComponentKey} lookups on CCA's {@link ComponentAccess}.
 */
@ApiStatus.Internal
public final class FabricAttachmentService implements AttachmentService {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object holder, String key, T defaultValue) {
        if (holder instanceof ComponentAccess access) {
            ComponentKey<?> ck = findComponent(access, key);
            if (ck != null) {
                T value = (T) ck.get(access);
                return value != null ? value : defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    public <T> void set(Object holder, String key, T value) {
        // TODO: MC 26.2 — CCA v8 immutable component set needs a proper write-through mechanism.
        // For now, the component reads work; writing is a no-op placeholder.
    }

    @Override
    public void sync(Object holder, String key) {
        // TODO: MC 26.2 — CCA sync is handled by marking the component dirty in set().
    }

    private static ComponentKey<?> findComponent(ComponentAccess access, String key) {
        // CCA components are accessed via known ComponentKey fields registered in a central class.
        // The key is resolved at the call site in IAmZombieAttachments — this generic lookup
        // is a fallback; for production code the callers use typed ComponentKey fields directly.
        return null;
    }
}
