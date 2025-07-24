package io.github.clojang.clojog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import io.github.clojang.clojog.exceptions.ClojogExceptions;
import org.slf4j.LoggerFactory;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;

/**
 * Clojog provides simple setup and configuration for structured, colored logging.
 * 
 * This class is the main entry point for configuring the clojog logger. It sets up
 * SLF4J/Logback with custom formatting, color support, and various output options.
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Exceedingly simple setup</li>
 *   <li>Colored output (enabled/disabled with a boolean)</li>
 *   <li>Configurable logging level</li>
 *   <li>Output to stdout or stderr</li>
 *   <li>Optional caller information (class, method, line number)</li>
 *   <li>Custom format similar to Clojure's Twig and LFE's Logjam libraries</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * import io.github.clojang.clojog.Clojog;
 * import io.github.clojang.clojog.ClojogOptions;
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 * 
 * public class MyApp {
 *     private static final Logger log = LoggerFactory.getLogger(MyApp.class);
 *     
 *     public static void main(String[] args) {
 *         Clojog.setupLogging(new ClojogOptions()
 *             .setColored(true)
 *             .setLevel("INFO")
 *             .setOutput("stdout")
 *             .setReportCaller(false));
 *         
 *         log.info("Application started!");
 *     }
 * }
 * }</pre>
 */
public class Clojog {
    
    private static final String VERSION = "0.1.6";
    private static final String BUILD_DATE = getBuildDate();
    private static final String GIT_COMMIT = getGitCommit();
    private static final String GIT_BRANCH = getGitBranch();
    private static final String GIT_SUMMARY = getGitSummary();
    
    private static boolean isInitialized = false;
    
    /**
     * Sets up the clojog logger with the specified options.
     * 
     * This method configures SLF4J/Logback with custom formatting, color support,
     * and the specified output destination. After calling this method, you can
     * use SLF4J loggers normally throughout your application.
     * 
     * @param options the configuration options for the logger
     * @throws ClojogExceptions.InvalidLogLevelException if the log level is invalid
     * @throws ClojogExceptions.UnsupportedLogOutputException if the output destination is unsupported
     * @throws ClojogExceptions.NotImplementedException if filesystem output is requested
     */
    public static void setupLogging(ClojogOptions options) {
        if (isInitialized) {
            return; // Prevent multiple initialization
        }
        
        // Enable ANSI colors on Windows
        if (options.isColored()) {
            AnsiConsole.systemInstall();
        }
        
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); // Clear any existing configuration
        
        // Create console appender
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = 
            new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("console");
        
        // Set output target
        switch (options.getOutput().toLowerCase()) {
            case "stdout":
                appender.setTarget("System.out");
                break;
            case "stderr":
                appender.setTarget("System.err");
                break;
            case "filesystem":
                throw new ClojogExceptions.NotImplementedException("filesystem log output");
            default:
                throw new ClojogExceptions.UnsupportedLogOutputException(options.getOutput());
        }
        
        // Create and configure our custom formatter
        ClojogFormatter formatter = new ClojogFormatter(!options.isColored(), options.isReportCaller());
        formatter.setContext(context);
        formatter.start();
        
        // Set the formatter on the appender
        appender.setLayout(formatter);
        appender.start();
        
        // Configure root logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
        
        // Set log level
        try {
            Level level = Level.valueOf(options.getLevel().toUpperCase());
            rootLogger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw new ClojogExceptions.InvalidLogLevelException(options.getLevel(), e);
        }
        
        isInitialized = true;
        
        // Log initialization message
        org.slf4j.Logger log = LoggerFactory.getLogger(Clojog.class);
        log.info("Logging initialized.");
    }
    
    /**
     * Sets up logging with default options.
     * 
     * Uses INFO level, colored output to stdout, without caller reporting.
     */
    public static void setupLogging() {
        setupLogging(new ClojogOptions());
    }
    
    /**
     * @return the version string of the clojog library
     */
    public static String getVersion() {
        return VERSION != null && !VERSION.isEmpty() ? VERSION : "N/A";
    }
    
    /**
     * @return a build string containing git branch, commit, and build date
     */
    public static String getBuildInfo() {
        if (GIT_COMMIT == null || GIT_COMMIT.isEmpty()) {
            return "N/A";
        }
        return String.format("%s@%s, %s", GIT_BRANCH, GIT_COMMIT, BUILD_DATE);
    }
    
    /**
     * @return true if the logger has been initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
    
    // These methods would typically be populated by build-time processing
    // For now, they return placeholder values
    
    private static String getBuildDate() {
        // This would be populated by Maven filtering or similar build-time processing
        return System.getProperty("clojog.build.date", "unknown");
    }
    
    private static String getGitCommit() {
        return System.getProperty("clojog.git.commit", "unknown");
    }
    
    private static String getGitBranch() {
        return System.getProperty("clojog.git.branch", "unknown");
    }
    
    private static String getGitSummary() {
        return System.getProperty("clojog.git.summary", "unknown");
    }
}