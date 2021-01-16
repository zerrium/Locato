package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class Locato extends JavaPlugin {
    private Connection connection;
    static FileConfiguration fc;
    public static Boolean debug;
    public static String storage;
    public static ArrayList<ZLocation> zLocations;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Locato] v0.1 by zerrium");
        Objects.requireNonNull(this.getCommand("locato")).setExecutor(new zerrium.Commands.Locato());
        Objects.requireNonNull(this.getCommand("whereis")).setExecutor(new zerrium.Commands.Whereis());
        Objects.requireNonNull(this.getCommand("shareloc")).setExecutor(new zerrium.Commands.ShareLoc());
        System.out.println(ChatColor.YELLOW+"[Locato] Connecting to database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        storage = Objects.requireNonNull(fc.getString("storage_type")).toLowerCase();

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
            rs = st.executeQuery("show tables");
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

}
