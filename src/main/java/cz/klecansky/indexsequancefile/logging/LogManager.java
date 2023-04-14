package cz.klecansky.indexsequancefile.logging;

public final class LogManager {

    private static final Logger logger = new Logger();

    public static Logger getLogger() {
        return logger;
    }

}
