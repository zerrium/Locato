package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Locato extends JavaPlugin {
    private Connection connection;
    static FileConfiguration fc;
    public static Boolean debug;
    public static String storage;
    public static ArrayList<ZLocation> zLocations;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Locato] v1.0 by zerrium");
        Objects.requireNonNull(this.getCommand("locato")).setExecutor(new zerrium.Commands.Locato());
        Objects.requireNonNull(getCommand("locato")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("whereis")).setExecutor(new zerrium.Commands.Whereis());
        Objects.requireNonNull(getCommand("whereis")).setTabCompleter(this);
        Objects.requireNonNull(this.getCommand("shareloc")).setExecutor(new zerrium.Commands.ShareLoc());
        Objects.requireNonNull(getCommand("shareloc")).setTabCompleter(this);
        System.out.println(ChatColor.YELLOW+"[Locato] Connecting to database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        storage = Objects.requireNonNull(fc.getString("storage_type")).toLowerCase();
        zLocations = new ArrayList<>();

        //Database connect
        try{
            connection = SqlCon.openConnection();
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
                zLocations.add(new ZLocation(rss.getString("place_id"), rss.getString("dimension"),
                        new ZChunk(rss.getInt("chunk1_x"), rss.getInt("chunk1_z"), rss.getInt("elevation1")),
                        new ZChunk(rss.getInt("chunk2_x"), rss.getInt("chunk2_z"), rss.getInt("elevation2"))));
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
        System.out.println(ChatColor.YELLOW+"[Locato] Done.");
    }

    @Override
    public void onDisable() {
        SqlCon.closeConnection();
        System.out.println(ChatColor.YELLOW+"[Locato] Disabling plugin...");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> locations = new ArrayList<String>();
        for (ZLocation location: zLocations) {
            locations.add(location.getPlaceId());
        }

        if(command.getName().equals("locato")) {
            if(args.length == 1)
                return Arrays.asList("add", "edit", "remove", "search", "status");
            if(args.length == 2 && args[0].equals("add") || args[0].equals("search") )
                return Arrays.asList();
            if(args.length == 2 && (args[0].equals("edit") || args[0].equals("remove") || args[0].equals("status")))
                return locations;
        }
        if(args.length < 2 && command.getName().equals("whereis")) {
            return locations;
        }
        if(args.length < 2 && command.getName().equals("shareloc")) {
            return null;
        }
        return Arrays.asList();
    }
}
