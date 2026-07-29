package dev.molang.iamzombieq.platform;

import dev.molang.iamzombieq.platform.fabric.FabricEventBusService;
import org.jetbrains.annotations.ApiStatus;

/**
 * Static holder for the platform services used by internal code. Implementations are the Fabric ones,
 * constructed with plain {@code new}.
 */
@ApiStatus.Internal
public final class Services {

    public static final EventBusService EVENTS = new FabricEventBusService();

    /**
     * Simple in-memory attachment service. Each key maps to a holder→value map.
     * TODO: Replace with a Cardinal-Components-backed implementation for persistence and sync.
     */
    public static final AttachmentService ATTACHMENT = new AttachmentService() {
        private final java.util.Map<String, java.util.Map<Object, Object>> store = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object holder, String key, T defaultValue) {
            var byHolder = store.get(key);
            if (byHolder == null) return defaultValue;
            T value = (T) byHolder.get(holder);
            return value != null ? value : defaultValue;
        }

        @Override
        public <T> void set(Object holder, String key, T value) {
            store.computeIfAbsent(key, k -> new java.util.concurrent.ConcurrentHashMap<>())
                    .put(holder, value);
        }

        @Override
        public void sync(Object holder, String key) {
            // no-op: in-memory store has no client sync
        }
    };

    private Services() {
    }
}
