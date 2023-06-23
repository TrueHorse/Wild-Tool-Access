package net.trueHorse.wildToolAccess.config;

public class ConfigOption {
    private final String defaultVal;
    private String val;
    private final String description;

    public ConfigOption(String defaultVal, String description){
        this.defaultVal = defaultVal;
        this.val = defaultVal;
        this.description = description;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getDescription() {
        return description;
    }
}
