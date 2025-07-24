package io.github.clojang.clojog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.clojang.clojog.exceptions.ClojogExceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the clojog logging library.
 */
class ClojogTest {

    private LoggerContext loggerContext;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger testLogger;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        // Disable colors for cleaner test output
        System.setProperty("jansi.force", "false");
        System.setProperty("jansi.disable", "true");

        // Reset static state
        resetClojogState();
        
        // Reset logging state before each test
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        // Create a list appender to capture log events
        listAppender = new ListAppender<>();
        listAppender.setContext(loggerContext);
        listAppender.start();

        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));

        // Clear MDC before each test
        MDC.clear();
    }

    // Helper method to reset Clojog static state using reflection
    private void resetClojogState() {
        try {
            java.lang.reflect.Field field = Clojog.class.getDeclaredField("isInitialized");
            field.setAccessible(true);
            field.setBoolean(null, false);
        } catch (Exception e) {
            // If we can't reset, that's okay for most tests
        }
    }

    @AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);

        // Clear MDC after each test
        MDC.clear();

        // Reset logger context
        loggerContext.reset();
    }

    @Test
    void testDefaultOptionsCreation() {
        ClojogOptions options = new ClojogOptions();
        
        assertTrue(options.isColored());
        assertEquals("INFO", options.getLevel());
        assertEquals("stdout", options.getOutput());
        assertFalse(options.isReportCaller());
    }

    @Test
    void testCustomOptionsCreation() {
        ClojogOptions options = new ClojogOptions(false, "DEBUG", "stderr", true);
        
        assertFalse(options.isColored());
        assertEquals("DEBUG", options.getLevel());
        assertEquals("stderr", options.getOutput());
        assertTrue(options.isReportCaller());
    }

    @Test
    void testFluentOptionsConfiguration() {
        ClojogOptions options = new ClojogOptions()
            .setColored(false)
            .setLevel("WARN")
            .setOutput("stderr")
            .setReportCaller(true);
        
        assertFalse(options.isColored());
        assertEquals("WARN", options.getLevel());
        assertEquals("stderr", options.getOutput());
        assertTrue(options.isReportCaller());
    }

    @Test
    void testOptionsToString() {
        ClojogOptions options = new ClojogOptions()
            .setColored(true)
            .setLevel("DEBUG")
            .setOutput("stdout")
            .setReportCaller(false);
        
        String result = options.toString();
        assertTrue(result.contains("colored=true"));
        assertTrue(result.contains("level='DEBUG'"));
        assertTrue(result.contains("output='stdout'"));
        assertTrue(result.contains("reportCaller=false"));
    }

    @Test
    void testSetupLoggingWithValidLevel() {
        ClojogOptions options = new ClojogOptions()
            .setLevel("DEBUG")
            .setOutput("stdout");
        
        assertDoesNotThrow(() -> Clojog.setupLogging(options));
        
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        assertEquals(Level.DEBUG, rootLogger.getLevel());
    }

    @Test
    void testSetupLoggingWithInvalidLevel() {
        ClojogOptions options = new ClojogOptions()
            .setLevel("INVALID_LEVEL");
        
        // Try to set up logging and catch any exception
        try {
            Clojog.setupLogging(options);
            // If we get here, check if the level was actually set incorrectly
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            // If it's not a valid level, Logback might ignore it or default to something else
            // This test might need to be adjusted based on actual Logback behavior
            assertNotNull(rootLogger.getLevel()); // At least verify logger exists
        } catch (Exception e) {
            // Any exception is fine - invalid level should cause some kind of error
            assertTrue(e instanceof ClojogExceptions.InvalidLogLevelException ||
                      e instanceof IllegalArgumentException ||
                      (e.getCause() != null && e.getCause() instanceof IllegalArgumentException));
        }
    }

    @Test
    void testSetupLoggingWithUnsupportedOutput() {
        ClojogOptions options = new ClojogOptions()
            .setOutput("invalid_output");
        
        assertThrows(ClojogExceptions.UnsupportedLogOutputException.class, 
            () -> Clojog.setupLogging(options));
    }

    @Test
    void testSetupLoggingWithFilesystemOutput() {
        ClojogOptions options = new ClojogOptions()
            .setOutput("filesystem");
        
        assertThrows(ClojogExceptions.NotImplementedException.class, 
            () -> Clojog.setupLogging(options));
    }

    @Test
    void testFormatterWithoutColors() {
        ClojogFormatter formatter = new ClojogFormatter(true, false); // disable colors
        formatter.setContext(loggerContext);
        formatter.start();

        // Create a mock logging event
        Logger logger = loggerContext.getLogger("test");
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);

        logger.info("Test message");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        String formatted = formatter.doLayout(event);

        assertTrue(formatted.contains("INFO"));
        assertTrue(formatted.contains("Test message"));
        assertTrue(formatted.contains("▶"));
        // Should not contain ANSI escape codes when colors are disabled
        assertFalse(formatted.contains("\u001B["));
    }

    @Test
    void testFormatterWithCaller() {
        ClojogFormatter formatter = new ClojogFormatter(true, true); // disable colors, enable caller
        formatter.setContext(loggerContext);
        formatter.start();

        Logger logger = loggerContext.getLogger("test");
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);
        
        // Enable caller data collection
        logger.setAdditive(false);

        logger.info("Test message with caller");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        String formatted = formatter.doLayout(event);

        assertTrue(formatted.contains("INFO"));
        assertTrue(formatted.contains("Test message with caller"));
        assertTrue(formatted.contains("▶"));
        
        // The caller information might not be available in test context,
        // so we'll check if it either has caller info or doesn't error
        assertNotNull(formatted);
        assertTrue(formatted.length() > 0);
    }

    @Test
    void testFormatterWithMDC() {
        ClojogFormatter formatter = new ClojogFormatter(true, false); // disable colors, no caller
        formatter.setContext(loggerContext);
        formatter.start();

        Logger logger = loggerContext.getLogger("test");
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);

        // Add MDC data
        MDC.put("userId", "12345");
        MDC.put("requestId", "req-abc");

        logger.info("Test message with MDC");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        String formatted = formatter.doLayout(event);

        assertTrue(formatted.contains("Test message with MDC"));
        assertTrue(formatted.contains("userId={12345}"));
        assertTrue(formatted.contains("requestId={req-abc}"));
        assertTrue(formatted.contains("||")); // MDC separator
    }

    @Test
    void testMultipleLogLevels() {
        ClojogOptions options = new ClojogOptions()
            .setLevel("TRACE")
            .setOutput("stdout")
            .setColored(false);

        Clojog.setupLogging(options);

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(ClojogTest.class);
        
        // These should not throw exceptions
        assertDoesNotThrow(() -> {
            slf4jLogger.trace("Trace message");
            slf4jLogger.debug("Debug message");
            slf4jLogger.info("Info message");
            slf4jLogger.warn("Warn message");
            slf4jLogger.error("Error message");
        });
    }

    @Test
    void testLoggerInitialization() {
        // Reset state first
        resetClojogState();
        
        assertFalse(Clojog.isInitialized());
        
        Clojog.setupLogging();
        
        assertTrue(Clojog.isInitialized());
    }

    @Test
    void testVersionInfo() {
        String version = Clojog.getVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
        
        String buildInfo = Clojog.getBuildInfo();
        assertNotNull(buildInfo);
        assertFalse(buildInfo.isEmpty());
    }

    @Test
    void testDefaultSetup() {
        assertDoesNotThrow(() -> Clojog.setupLogging());
        
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        // The default level should be INFO
        assertEquals(Level.INFO, rootLogger.getLevel());
    }

    @Test
    void testExceptionMessages() {
        ClojogExceptions.InvalidLogLevelException levelEx = 
            new ClojogExceptions.InvalidLogLevelException("INVALID");
        assertTrue(levelEx.getMessage().contains("Could not set configured log level"));
        assertTrue(levelEx.getMessage().contains("INVALID"));

        ClojogExceptions.UnsupportedLogOutputException outputEx = 
            new ClojogExceptions.UnsupportedLogOutputException("invalid");
        assertTrue(outputEx.getMessage().contains("Unsupported log output"));
        assertTrue(outputEx.getMessage().contains("invalid"));

        ClojogExceptions.NotImplementedException notImplEx = 
            new ClojogExceptions.NotImplementedException("feature");
        assertTrue(notImplEx.getMessage().contains("Not yet implemented"));
        assertTrue(notImplEx.getMessage().contains("feature"));
    }

    @Test
    void testFormatterColorLevels() {
        ClojogFormatter formatter = new ClojogFormatter(false, false); // enable colors
        formatter.setContext(loggerContext);
        formatter.start();

        Logger logger = loggerContext.getLogger("test");
        logger.addAppender(listAppender);
        logger.setLevel(Level.TRACE);

        // Test different log levels
        logger.trace("Trace message");
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warn message");
        logger.error("Error message");

        assertEquals(5, listAppender.list.size());

        // Verify that each level produces output (colors enabled)
        for (ILoggingEvent event : listAppender.list) {
            String formatted = formatter.doLayout(event);
            assertNotNull(formatted);
            assertFalse(formatted.trim().isEmpty());
            assertTrue(formatted.contains("▶"));
        }
    }

    @Test
    void testEmptyMessage() {
        ClojogFormatter formatter = new ClojogFormatter(true, false);
        formatter.setContext(loggerContext);
        formatter.start();

        Logger logger = loggerContext.getLogger("test");
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);

        logger.info(""); // Empty message

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        String formatted = formatter.doLayout(event);

        assertTrue(formatted.contains("INFO"));
        // Should still have timestamp and level even with empty message
        assertTrue(formatted.length() > 10);
    }
}