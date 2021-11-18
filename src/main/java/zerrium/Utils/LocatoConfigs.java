package zerrium.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import zerrium.Locato;
import zerrium.Models.LocatoConfig;

import java.util.HashMap;

public class LocatoConfigs {
    private static FileConfiguration fc;
    private static HashMap<LocatoConfig, Boolean> booleanConfigs;
    private static HashMap<LocatoConfig, Integer> intConfigs;
    private static HashMap<LocatoConfig, String> stringConfigs;

    private static boolean debug;

    public static boolean getBooleanConfig(LocatoConfig config) {
        return booleanConfigs.get(config);
    }

    public static int getIntConfig(LocatoConfig config) {
        return intConfigs.get(config);
    }

    public static String getStringConfig(LocatoConfig config) {
        return stringConfigs.get(config);
    }

    public static boolean getDebug() {
        return debug;
    }

    public LocatoConfigs() {
        fc = Locato.getPlugin(Locato.class).getConfig();
        debug = fc.getBoolean(LocatoConfig.DEBUG.getConfig());
        this.readConfig();
    }

    private synchronized void readConfig(){
        System.out.println(ChatColor.YELLOW+"[Locato] Reading config file...");

        booleanConfigs = new HashMap<>();
        intConfigs = new HashMap<>();
        stringConfigs = new HashMap<>();

        for (LocatoConfig config: LocatoConfig.getBooleanConfigs()) {
            booleanConfigs.put(config, fc.getBoolean(config.getConfig()));
        }

        for (LocatoConfig config: LocatoConfig.getIntConfigs()) {
            intConfigs.put(config, fc.getInt(config.getConfig()));
        }

        for (LocatoConfig config: LocatoConfig.getStringConfigs()) {
            stringConfigs.put(config, fc.getString(config.getConfig()));
        }

        debug = getBooleanConfig(LocatoConfig.DEBUG);
        System.out.println(ChatColor.YELLOW+"[Locato] Done.");
    }
}
