
package com.backend.Service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000;

    private final ConcurrentHashMap<String, Window> attempts =
            new ConcurrentHashMap<>();

    public void checkLimit(String email) {

        long now = System.currentTimeMillis();

        attempts.compute(email.toLowerCase(), (key, window) -> {

            if (window == null ||
                    now - window.startTime() >= WINDOW_MILLIS) {
                return new Window(1, now);
            }

            if (window.count() >= MAX_ATTEMPTS) {
                throw new RateLimitExceededException(
                        "Too many attempts. Try again after 15 minutes."
                );
            }

            return new Window(window.count() + 1, window.startTime());
        });
    }

    private record Window(int count, long startTime) {}
}
