# clojog

[![Build Status][gh-actions-badge]][gh-actions]

[![Project Logo][logo]][logo-large]

*A simple wrapper for customized SLF4J/Logback usage with colored output*

This is a Java port of the original Go zylog library, providing the same clean, colored logging interface for Java applications.

## Features

- **Simple Setup**: One-line configuration to get beautiful, structured logs
- **Colored Output**: Configurable ANSI color support with cross-platform compatibility
- **Flexible Levels**: Support for all standard log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- **Multiple Outputs**: Log to stdout, stderr, or (planned) filesystem
- **Caller Information**: Optional method/line number reporting
- **Structured Logging**: Full MDC (Mapped Diagnostic Context) support
- **Maven Central Ready**: Configured for easy publishing and distribution

## Installation

### Maven Dependency

Add to your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.clojang</groupId>
        <artifactId>clojog</artifactId>
        <version>0.1.3</version>
    </dependency>
</dependencies>
```

### Gradle Dependency

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.clojang:clojog:0.1.3'
}
```

### SBT Dependency (Scala)

Add to your `build.sbt`:

```scala
libraryDependencies += "io.github.clojang" % "clojog" % "0.1.0"
```

### Leiningen Dependency (Clojure)

Add to your `project.clj`:

```clojure
:dependencies [[io.github.clojang/clojog "0.1.0"]]
```

## Quick Start

Here's a simple example to get you started:

```java
import io.github.clojang.clojog.Clojog;
import io.github.clojang.clojog.ClojogOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApp {
    private static final Logger log = LoggerFactory.getLogger(MyApp.class);
    
    public static void main(String[] args) {
        // Setup logging with colors
        Clojog.setupLogging(new ClojogOptions()
            .setColored(true)
            .setLevel("INFO")
            .setOutput("stdout")
            .setReportCaller(false));
        
        log.info("You are standing in an open field west of a white house.");
        log.warn("It is pitch black. You are likely to be eaten by a grue.");
    }
}
```

## Configuration

Configure clojog using the `ClojogOptions` class:

```java
ClojogOptions options = new ClojogOptions()
    .setColored(true)           // Enable ANSI colors
    .setLevel("DEBUG")          // Set minimum log level
    .setOutput("stdout")        // Output destination
    .setReportCaller(true);     // Include caller info

Clojog.setupLogging(options);
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `colored` | boolean | true | Enable/disable ANSI color output |
| `level` | String | "INFO" | Minimum log level (TRACE, DEBUG, INFO, WARN, ERROR) |
| `output` | String | "stdout" | Output destination (stdout, stderr) |
| `reportCaller` | boolean | false | Include class, method, and line number in logs |

## Output Format

clojog produces clean, structured log output:

**Without caller info:**
```
2024-01-15T10:30:45-05:00 INFO ▶ Application started successfully
```

**With caller info:**
```
2024-01-15T10:30:45-05:00 INFO [com.example.MyApp.main:23] ▶ Application started successfully
```

**With structured data (MDC):**
```java
MDC.put("userId", "12345");
MDC.put("requestId", "req-abc");
log.info("User logged in");
```
```
2024-01-15T10:30:45-05:00 INFO ▶ User logged in || userId={12345}, requestId={req-abc}
```

## Structured Logging

clojog fully supports SLF4J's MDC (Mapped Diagnostic Context) for structured logging:

```java
import org.slf4j.MDC;

// Add context
MDC.put("userId", "12345");
MDC.put("sessionId", "session-abc");

log.info("Processing user request");

// Clear context when done
MDC.clear();
```

## Color Scheme

Different log levels are colored for easy visual scanning:

- **TRACE**: Magenta
- **DEBUG**: Cyan  
- **INFO**: Green
- **WARN**: Yellow
- **ERROR**: Red

Colors automatically disable on non-TTY outputs and can be manually disabled.

## Demo Application

Run the demo to see clojog in action:

```bash
mvn compile exec:java -Dexec.mainClass="io.github.clojang.clojog.demo.DemoApp"
```
[![Demo screenshot][screenshot]][screenshot]

## Integration with Configuration Libraries

clojog works great with configuration libraries like Spring Boot's configuration or other property systems:

```java
// Spring Boot example
@Value("${logging.colored:true}")
private boolean colored;

@Value("${logging.level:INFO}")
private String level;

@PostConstruct
public void setupLogging() {
    Clojog.setupLogging(new ClojogOptions()
        .setColored(colored)
        .setLevel(level)
        .setOutput("stdout")
        .setReportCaller(false));
}
```

## Building from Source

```bash
git clone https://github.com/clojang/clojog.git
cd clojog
mvn clean install
```

## Publishing to Maven Central

This project is configured for publishing to Maven Central. See the publishing guide in `docs/PUBLISHING.md` for details.

## Background

This library is inspired by and ports the Go zylog library, which in turn was inspired by:
- [Twig](https://github.com/clojusc/twig) (Clojure)
- [Logjam](https://github.com/lfex/logjam) (LFE)

The goal is to provide clean, readable, structured logging across different platforms and languages.

## License

© 2025, Clojang. All rights reserved.

Licensed under the Apache License, Version 2.0. See `LICENSE` file for details.

[//]: ---Named-Links---

[logo]: resources/images/logo.jpg
[logo-large]: resources/images/logo-large.jpg
[screenshot]: resources/images/demo-screenshot.png
[gh-actions-badge]: https://github.com/clojang/clojog/workflows/CI/badge.svg
[gh-actions]: https://github.com/clojang/clojog/actions?query=workflow%3ACI
