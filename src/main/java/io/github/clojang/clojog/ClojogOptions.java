package io.github.clojang.clojog;

/**
 * Configuration options for the clojog logger setup.
 * 
 * This class contains all the configuration parameters needed to set up
 * the custom logging formatter and behavior.
 */
public class ClojogOptions {
    private boolean colored = true;
    private String level = "INFO";
    private String output = "stdout";
    private boolean reportCaller = false;

    /**
     * Creates a new ClojogOptions instance with default values.
     */
    public ClojogOptions() {
        // Default values set above
    }

    /**
     * Creates a new ClojogOptions instance with specified values.
     * 
     * @param colored whether to enable colored output
     * @param level the logging level (TRACE, DEBUG, INFO, WARN, ERROR)
     * @param output the output destination (stdout, stderr)
     * @param reportCaller whether to include caller information in logs
     */
    public ClojogOptions(boolean colored, String level, String output, boolean reportCaller) {
        this.colored = colored;
        this.level = level;
        this.output = output;
        this.reportCaller = reportCaller;
    }

    /**
     * @return true if colored output is enabled
     */
    public boolean isColored() {
        return colored;
    }

    /**
     * @param colored whether to enable colored output
     * @return this instance for method chaining
     */
    public ClojogOptions setColored(boolean colored) {
        this.colored = colored;
        return this;
    }

    /**
     * @return the logging level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the logging level (TRACE, DEBUG, INFO, WARN, ERROR)
     * @return this instance for method chaining
     */
    public ClojogOptions setLevel(String level) {
        this.level = level;
        return this;
    }

    /**
     * @return the output destination
     */
    public String getOutput() {
        return output;
    }

    /**
     * @param output the output destination (stdout, stderr)
     * @return this instance for method chaining
     */
    public ClojogOptions setOutput(String output) {
        this.output = output;
        return this;
    }

    /**
     * @return true if caller information should be included in logs
     */
    public boolean isReportCaller() {
        return reportCaller;
    }

    /**
     * @param reportCaller whether to include caller information in logs
     * @return this instance for method chaining
     */
    public ClojogOptions setReportCaller(boolean reportCaller) {
        this.reportCaller = reportCaller;
        return this;
    }

    @Override
    public String toString() {
        return "ClojogOptions{" +
                "colored=" + colored +
                ", level='" + level + '\'' +
                ", output='" + output + '\'' +
                ", reportCaller=" + reportCaller +
                '}';
    }
}