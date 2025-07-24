package io.github.clojang.clojog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import io.github.clojang.clojog.exceptions.ClojogExceptions;
import org.slf4j.LoggerFactory;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

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
    
    private static final String VERSION;
    private static final String BUILD_DATE;
    private static final String GIT_COMMIT;
    private static final String GIT_BRANCH;
    
    static {
        Properties versionProps = new Properties();
        Properties gitProps = new Properties();
        
        // Load version information from Maven-filtered properties
        try (InputStream is = Clojog.class.getResourceAsStream("/version.properties")) {
            if (is != null) {
                versionProps.load(is);
            }
        } catch (IOException e) {
            // Ignore - use defaults
        }
        
        // Load git information
        try (InputStream is = Clojog.class.getResourceAsStream("/git.properties")) {
            if (is != null) {
                gitProps.load(is);
            }
        } catch (IOException e) {
            // Ignore - use defaults
        }
        
        // Version comes from Maven project version
        VERSION = versionProps.getProperty("project.version", "unknown");
        
        // Git info comes from git-commit-id plugin
        BUILD_DATE = gitProps.getProperty("git.build.time", versionProps.getProperty("maven.build.timestamp", "unknown"));
        GIT_COMMIT = gitProps.getProperty("git.commit.id.abbrev", "unknown");
        GIT_BRANCH = gitProps.getProperty("git.branch", "unknown");
    }
    
    private static boolean isInitialized = false;
    private static boolean ansiInstalled = false;
    
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
        // Enable ANSI colors on Windows (only install once)
        if (options.isColored() && !ansiInstalled) {
            AnsiConsole.systemInstall();
            ansiInstalled = true;
        }
        
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); // Clear any existing configuration - this allows reconfiguration
        
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
        appender.setEncoder(formatter);
        appender.start();
        
        // Configure root logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders(); // Ensure we clear previous appenders
        rootLogger.addAppender(appender);
        
        // Set log level
        try {
            Level level = Level.valueOf(options.getLevel().toUpperCase());
            rootLogger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw new ClojogExceptions.InvalidLogLevelException(options.getLevel(), e);
        }
        
        // Only log initialization message on first setup
        if (!isInitialized) {
            isInitialized = true;
            org.slf4j.Logger log = LoggerFactory.getLogger(Clojog.class);
            log.info("Logging initialized.");
        }
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
        return VERSION;
    }
    
    /**
     * @return a build string containing git branch, commit, and build date
     */
    public static String getBuildInfo() {
        if ("unknown".equals(GIT_COMMIT)) {
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
}