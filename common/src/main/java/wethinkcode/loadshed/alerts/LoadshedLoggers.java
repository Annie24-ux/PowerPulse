package wethinkcode.loadshed.alerts;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging various levels of messages related to load shedding.
 * It provides methods for logging at different severity levels.
 */
public class LoadshedLoggers {
    private static final Logger logger = Logger.getLogger(LoadshedLoggers.class.getName());

    /**
     * Logs a message with a specific logging level and an optional throwable.
     *
     * @param level the level of the log (e.g., SEVERE, INFO, etc.).
     * @param msg the message to be logged.
     * @param thrown an optional throwable to be logged alongside the message.
     */
    public static void log(Level level, String msg, Throwable thrown) {
        logger.log(level, msg, thrown);
    }

    /**
     * Logs a SEVERE level message with an optional throwable.
     *
     * @param msg the message to be logged.
     * @param thrown an optional throwable to be logged alongside the message.
     */
    public static void severe(String msg, Throwable thrown) {
        log(Level.SEVERE, msg, thrown);
    }
}
