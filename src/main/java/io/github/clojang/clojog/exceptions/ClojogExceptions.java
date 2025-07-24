package io.github.clojang.clojog.exceptions;

/**
 * Custom exceptions for the clojog library.
 */
public class ClojogExceptions {

    /**
     * Exception thrown when an invalid log level is specified.
     */
    public static class InvalidLogLevelException extends RuntimeException {
        public InvalidLogLevelException(String message) {
            super("Could not set configured log level: " + message);
        }
        
        public InvalidLogLevelException(String message, Throwable cause) {
            super("Could not set configured log level: " + message, cause);
        }
    }

    /**
     * Exception thrown when an unsupported log output destination is specified.
     */
    public static class UnsupportedLogOutputException extends RuntimeException {
        public UnsupportedLogOutputException(String output) {
            super("Unsupported log output: " + output);
        }
    }

    /**
     * Exception thrown when a feature is not yet implemented.
     */
    public static class NotImplementedException extends RuntimeException {
        public NotImplementedException(String feature) {
            super("Not yet implemented: " + feature);
        }
    }
}