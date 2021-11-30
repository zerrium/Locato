package zerrium.Models;

import java.util.LinkedList;
import java.util.List;

public enum LocatoConfig {
    STORAGE_TYPE    ("storage_type"),
    DB_HOST         ("hostname"),
    DB_PORT         ("port"),
    DB_NAME         ("database"),
    DB_USER         ("username"),
    DB_PASSWORD     ("password"),
    DB_SSL          ("use_SSL"),
    DEBUG           ("use_debug");

    private final String config;

    LocatoConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return this.config;
    }

    public static LinkedList<LocatoConfig> getBooleanConfigs() {
        return new LinkedList<>(List.of(
                DB_SSL,
                DEBUG
        ));
    }

    public static LinkedList<LocatoConfig> getIntConfigs() {
        return new LinkedList<>(List.of(
                DB_PORT
        ));
    }

    public static LinkedList<LocatoConfig> getStringConfigs() {
        return new LinkedList<>(List.of(
                STORAGE_TYPE,
                DB_HOST,
                DB_NAME,
                DB_USER,
                DB_PASSWORD
        ));
    }
}
