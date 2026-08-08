package club.havocsmp.eco.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Generic per-player cooldown tracker (used by /live, and reusable for /rtp etc). */
public class Cooldowns {

    private final Map<String, Map<UUID, Long>> buckets = new HashMap<>();

    /** Returns remaining seconds, or 0 if ready. */
    public long remaining(String bucket, UUID uuid) {
        Map<UUID, Long> map = buckets.get(bucket);
        if (map == null) return 0;
        Long until = map.get(uuid);
        if (until == null) return 0;
        long remainingMs = until - System.currentTimeMillis();
        return remainingMs <= 0 ? 0 : (remainingMs + 999) / 1000;
    }

    public boolean isReady(String bucket, UUID uuid) {
        return remaining(bucket, uuid) == 0;
    }

    public void set(String bucket, UUID uuid, long seconds) {
        buckets.computeIfAbsent(bucket, k -> new HashMap<>())
                .put(uuid, System.currentTimeMillis() + seconds * 1000L);
    }
}
