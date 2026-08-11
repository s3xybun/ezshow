package dev.julian.ezshow.core.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Loader-independent, in-memory cooldown policy.
 *
 * <p>The caller provides a monotonic timestamp, normally {@link System#nanoTime()}.
 * Deadlines intentionally use subtraction for comparisons so nanoTime wrapping is
 * handled correctly as long as an individual cooldown is shorter than 2^63 ticks.</p>
 */
public final class CooldownGate<K> {
    private final Map<K, Long> deadlines = new HashMap<>();

    public synchronized boolean tryAcquire(K key, long now, long duration) {
        Objects.requireNonNull(key, "key");
        if (duration < 0L) {
            throw new IllegalArgumentException("duration must not be negative");
        }

        if (duration == 0L) {
            deadlines.remove(key);
            return true;
        }

        Long deadline = deadlines.get(key);
        if (deadline != null && now - deadline < 0L) {
            return false;
        }

        deadlines.put(key, now + duration);
        return true;
    }

    public synchronized void clear() {
        deadlines.clear();
    }

}
