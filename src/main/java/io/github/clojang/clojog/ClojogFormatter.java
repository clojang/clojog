package io.github.clojang.clojog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.encoder.EncoderBase;
import org.fusesource.jansi.Ansi;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Custom encoder for clojog that provides colored output and structured formatting.
 * 
 * The formatter outputs logs in the following formats:
 * 
 * Without caller info:
 * YYYY-MM-DDTHH:MM:SS-TZ:00 LEVEL ▶ logged message ...
 * 
 * With caller info:
 * YYYY-MM-DDTHH:MM:SS-TZ:00 LEVEL [class.method:LINE] ▶ logged message ...
 * 
 * Any MDC (Mapped Diagnostic Context) data is appended as key-value pairs.
 */
public class ClojogFormatter extends EncoderBase<ILoggingEvent> {
    
    private final boolean disableColors;
    private final boolean reportCaller;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    /**
     * Creates a new ClojogFormatter.
     * 
     * @param disableColors whether to disable colored output
     * @param reportCaller whether to include caller information
     */
    public ClojogFormatter(boolean disableColors, boolean reportCaller) {
        this.disableColors = disableColors;
        this.reportCaller = reportCaller;
    }
    
    @Override
    public byte[] encode(ILoggingEvent event) {
        return doLayout(event).getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public byte[] headerBytes() {
        return new byte[0];
    }
    
    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
    
    public String doLayout(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        
        // Format timestamp
        String timestamp = Instant.ofEpochMilli(event.getTimeStamp())
                .atOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
                .format(timeFormatter);
        
        if (!disableColors) {
            sb.append(Ansi.ansi().fg(Ansi.Color.GREEN).a(timestamp).reset());
        } else {
            sb.append(timestamp);
        }
        
        sb.append(" ");
        
        // Format level with color
        String levelStr = event.getLevel().toString();
        if (!disableColors) {
            sb.append(colorLevel(levelStr));
        } else {
            sb.append(levelStr);
        }
        
        // Add caller information if enabled
        if (reportCaller) {
            StackTraceElement[] callerData = event.getCallerData();
            if (callerData != null && callerData.length > 0) {
                StackTraceElement caller = callerData[0];
                String callerInfo = String.format("%s.%s:%d", 
                    caller.getClassName(), caller.getMethodName(), caller.getLineNumber());
                
                sb.append(" [");
                if (!disableColors) {
                    sb.append(Ansi.ansi().fg(Ansi.Color.YELLOW).a(callerInfo).reset());
                } else {
                    sb.append(callerInfo);
                }
                sb.append("]");
            }
        }
        
        // Add message
        if (event.getFormattedMessage() != null && !event.getFormattedMessage().isEmpty()) {
            if (!disableColors) {
                sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a(" ▶ ").reset());
            } else {
                sb.append(" ▶ ");
            }
            sb.append(event.getFormattedMessage());
        }
        
        // Add MDC properties
        Map<String, String> mdcProperties = event.getMDCPropertyMap();
        if (mdcProperties != null && !mdcProperties.isEmpty()) {
            sb.append(" || ");
            boolean first = true;
            for (Map.Entry<String, String> entry : mdcProperties.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("={").append(entry.getValue()).append("}");
                first = false;
            }
        }
        
        sb.append(System.lineSeparator());
        return sb.toString();
    }
    
    /**
     * Applies color to the log level based on its severity.
     * 
     * @param level the log level string
     * @return the colored level string
     */
    private String colorLevel(String level) {
        if (disableColors) {
            return level;
        }
        
        Ansi.Color color;
        switch (level.toUpperCase()) {
            case "TRACE":
                color = Ansi.Color.MAGENTA;
                break;
            case "DEBUG":
                color = Ansi.Color.CYAN;
                break;
            case "INFO":
                color = Ansi.Color.GREEN;
                break;
            case "WARN":
                color = Ansi.Color.YELLOW;
                break;
            case "ERROR":
                color = Ansi.Color.RED;
                break;
            default:
                color = Ansi.Color.WHITE;
                break;
        }
        
        return Ansi.ansi().fg(color).bold().a(level).reset().toString();
    }
}