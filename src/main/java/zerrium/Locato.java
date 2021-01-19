package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import zerrium.Commands.LocatoCommand;
import zerrium.Commands.LocatoShareLoc;
import zerrium.Commands.LocatoWhereis;

import java.sql.*;
import java.util.*;

public class Locato extends JavaPlugin {
    private Connection connection;
    static FileConfiguration fc;
    public static Boolean debug;
    public static String storage;
    public static ArrayList<LocatoZLocation> zLocations;
    public static ArrayList<String> worlds;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Locato] v1.2 by zerrium");
        Objects.requireNonNull(this.getCommand("locato")).setExecutor(new LocatoCommand());
        Objects.requireNonNull(getCommand("locato")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("whereis")).setExecutor(new LocatoWhereis());
        Objects.requireNonNull(getCommand("whereis")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("shareloc")).setExecutor(new LocatoShareLoc());
        Objects.requireNonNull(getCommand("shareloc")).setTabCompleter(this);
        System.out.println(ChatColor.YELLOW+"[Locato] Connecting to database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        storage = Objects.requireNonNull(fc.getString("storage_type")).toLowerCase();
        zLocations = new ArrayList<>();
        worlds = new ArrayList<>();

        //Database connect
        try{
            connection = LocatoSqlCon.openConnection();
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Locato]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //database query
        Statement st = null;
        ResultSet rs = null;
        ResultSet rss = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(storage.equals("sqlite") ? "SELECT name FROM sqlite_master WHERE type='table'" : "show tables");
            if(!rs.next()){
                st.executeUpdate("create table locato(" +
                        "    place_id varchar(30) not null," +
                        "    dimension text not null," +
                        "    chunk1_x int not null," +
                        "    chunk1_z int not null," +
                        "    elevation1 int not null," +
                        "    chunk2_x int not null," +
                        "    chunk2_z int not null," +
                        "    elevation2 int not null," +
                        "    primary key(place_id));");
            }
            rss = st.executeQuery("select * from locato;");
            System.out.println(ChatColor.YELLOW+"[Locato] Getting places list from database...");
            int c = 0;
            while(rss.next()){
                zLocations.add(new LocatoZLocation(rss.getString("place_id"), rss.getString("dimension"),
                        new LocatoZChunk(rss.getInt("chunk1_x"), rss.getInt("chunk1_z"), rss.getInt("elevation1")),
                        new LocatoZChunk(rss.getInt("chunk2_x"), rss.getInt("chunk2_z"), rss.getInt("elevation2"))));
                if(debug){
                    System.out.println(zLocations.get(c).getPlaceId());
                }
                c++;
            }
            System.out.println(ChatColor.YELLOW+"[Locato] Found "+ c +" place records on database.");

        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Locato]"+ChatColor.RED+" An SQL error occured:");
            throwables.printStackTrace();
        } finally {
            try {
                assert st != null;
                st.close();

                assert rs != null;
                rs.close();

                assert rss != null;
                rss.close();

                connection.close();
            } catch (Exception e) {
                if(debug) System.out.println("[Locato] "+ e );
            }
        }

        for(World w:Bukkit.getWorlds()){
            worlds.add(w.getName().toLowerCase());
        }

        System.out.println(ChatColor.YELLOW+"[Locato] Done.");
    }

    @Override
    public void onDisable() {
        LocatoSqlCon.closeConnection();
        System.out.println(ChatColor.YELLOW+"[Locato] Disabling plugin...");
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
}
