package io.github.clojang.clojog.demo;

import io.github.clojang.clojog.Clojog;
import io.github.clojang.clojog.ClojogOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Demo application showing clojog usage.
 * 
 * This demonstrates the various features of clojog including:
 * - Different log levels with colors
 * - Caller information reporting
 * - MDC (Mapped Diagnostic Context) usage for structured logging
 */
public class DemoApp {
    
    private static final Logger log = LoggerFactory.getLogger(DemoApp.class);
    
    public static void main(String[] args) {
        System.out.println("clojog Java Demo Application");
        System.out.println("Version: " + Clojog.getVersion());
        System.out.println("Build: " + Clojog.getBuildInfo());
        System.out.println();
        
        // Setup logging with colors and caller info
        Clojog.setupLogging(new ClojogOptions()
            .setColored(true)
            .setLevel("TRACE")
            .setOutput("stdout")
            .setReportCaller(true));
        
        // Demonstrate different log levels
        log.trace("This is a TRACE message - most detailed");
        log.debug("This is a DEBUG message - detailed information");
        log.info("This is an INFO message - general information");
        log.warn("This is a WARN message - warning about something");
        log.error("This is an ERROR message - something went wrong");
        
        // Demonstrate structured logging with MDC
        MDC.put("userId", "12345");
        MDC.put("sessionId", "abc-def-ghi");
        MDC.put("requestId", "req-789");
        
        log.info("User performed an action");
        log.warn("User attempted unauthorized action");
        
        // Clear MDC
        MDC.clear();
        
        // Demonstrate without caller info
        System.out.println("\n--- Reconfiguring without caller info ---\n");
        
        Clojog.setupLogging(new ClojogOptions()
            .setColored(true)
            .setLevel("INFO")
            .setOutput("stdout")
            .setReportCaller(false));
        
        log.info("You are standing in an open field west of a white house.");
        log.warn("It is pitch black. You are likely to be eaten by a grue.");
        log.error("The grue has eaten you!");
        
        // Demonstrate with structured data again
        MDC.put("location", "open field");
        MDC.put("direction", "west");
        MDC.put("structure", "white house");
        
        log.info("Player moved to new location");
        
        MDC.clear();
        
        // Demonstrate different output (stderr)
        System.out.println("\n--- Switching to stderr output ---\n");
        
        Clojog.setupLogging(new ClojogOptions()
            .setColored(true)
            .setLevel("WARN")
            .setOutput("stderr")
            .setReportCaller(false));
        
        log.warn("This warning goes to stderr");
        log.error("This error also goes to stderr");
        
        // Demonstrate without colors
        System.out.println("\n--- Disabling colors ---\n");
        
        Clojog.setupLogging(new ClojogOptions()
            .setColored(false)
            .setLevel("INFO")
            .setOutput("stdout")
            .setReportCaller(true));
        
        log.info("This message has no colors but includes caller info");
        log.warn("This warning also has no colors");
        
        System.out.println("\nDemo completed!");
    }
}