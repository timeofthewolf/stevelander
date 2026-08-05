package net.stevelander;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Safety {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stevelander.MOD_ID);
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private Safety() {
    }

    public static void run(String where, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (REPORTED.add(where)) {
                LOGGER.error("Suppressed error in {}", where, t);
            }
        }
    }
}
