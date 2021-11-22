package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import zerrium.Commands.LocatoCommand;
import zerrium.Commands.LocatoShareLoc;
import zerrium.Commands.LocatoWhereis;
import zerrium.Models.LocatoZLocation;
import zerrium.Utils.LocatoConfigs;
import zerrium.Utils.LocatoSqlUtils;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Locato extends JavaPlugin {
    private static ArrayList<LocatoZLocation> zLocations;
    private static ArrayList<String> worlds;
    private Logger log;

    @Override
    public void onEnable() {
        log = getLogger();
        log.setLevel(Level.INFO);
        log.info(ChatColor.YELLOW+"[Locato]"+ChatColor.RESET+" v2.0 by zerrium");
        Objects.requireNonNull(this.getCommand("locato")).setExecutor(new LocatoCommand());
        Objects.requireNonNull(getCommand("locato")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("whereis")).setExecutor(new LocatoWhereis());
        Objects.requireNonNull(getCommand("whereis")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("shareloc")).setExecutor(new LocatoShareLoc());
        Objects.requireNonNull(getCommand("shareloc")).setTabCompleter(this);
        log.info(ChatColor.YELLOW+"[Locato]"+ChatColor.RESET+" Connecting to database...");

        this.saveDefaultConfig(); //get config file
        new LocatoConfigs();
        zLocations = new ArrayList<>();
        worlds = new ArrayList<>();

        //Database connect
        Connection connection = null;
        try{
            connection = LocatoSqlUtils.openConnection();
        } catch (SQLException throwables) {
            log.severe(ChatColor.YELLOW+"[Locato]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //database query
        assert connection != null;
        LocatoSqlUtils.initSQL(connection, zLocations);

        for(World w:Bukkit.getWorlds()){
            worlds.add(w.getName().toLowerCase());
        }

        log.info(ChatColor.YELLOW+"[Locato]"+ChatColor.RESET+" Done.");
    }

    @Override
    public void onDisable() {
        LocatoSqlUtils.closeConnection();
        log.info(ChatColor.YELLOW+"[Locato]"+ChatColor.RESET+" Disabling plugin...");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> locations = new ArrayList<>();
        for (LocatoZLocation location: zLocations) {
            locations.add(location.getPlaceId());
        }

        switch (command.getName()){
            case "locato":
                return locatoTabComplete(args, locations);
            case "whereis":
                if(args.length < 2) return locations;
                else return Collections.emptyList();
            case "shareloc":
                if(args.length < 2) return null; //auto complete online players
                else return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    private List<String> locatoTabComplete(String[] args, ArrayList<String> locations){
        switch(args.length){
            case 1:
                return Arrays.asList("add", "edit", "remove", "search", "status");
            case 2:
                switch(args[0]){
                    case "add":
                        return Collections.singletonList("<place_name>");
                    case "search":
                    case "edit":
                    case "remove":
                    case "delete":
                    case "status":
                        return locations;
                    default:
                        return Collections.emptyList();
                }
            default:
                return locatoAddEditTabComplete(args);
        }
    }

    private List<String> locatoAddEditTabComplete(String[] args){
        if(args[0].equals("edit") || args[0].equals("add")){
            switch (args.length){
                case 3:
                    return Collections.singletonList("<chunk1_x>");
                case 4:
                    return Collections.singletonList("<chunk1_y>");
                case 5:
                    return Collections.singletonList("<chunk1_z>");
                case 6:
                    return Collections.singletonList("<chunk2_x>");
                case 7:
                    return Collections.singletonList("<chunk2_y>");
                case 8:
                    return Collections.singletonList("<chunk2_z>");
                case 9:
                    return worlds;
                default:
                    return Collections.emptyList();
            }
        }else return Collections.emptyList();
    }

    public static ArrayList<LocatoZLocation> getzLocations() {
        return zLocations;
    }

    public static ArrayList<String> getWorlds() {
        return worlds;
    }
}
