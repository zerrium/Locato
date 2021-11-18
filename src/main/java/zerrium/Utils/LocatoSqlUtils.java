package zerrium.Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import zerrium.Locato;
import zerrium.Models.LocatoConfig;
import zerrium.Models.LocatoZChunk;
import zerrium.Models.LocatoZLocation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LocatoSqlUtils {
    private final static String hostname = LocatoConfigs.getStringConfig(LocatoConfig.DB_HOST);
    private final static int port = LocatoConfigs.getIntConfig(LocatoConfig.DB_PORT);
    private final static String dbName = LocatoConfigs.getStringConfig(LocatoConfig.DB_NAME);
    private final static String username = LocatoConfigs.getStringConfig(LocatoConfig.DB_USER);
    private final static String password = LocatoConfigs.getStringConfig(LocatoConfig.DB_PASSWORD);
    private final static boolean useSSL = LocatoConfigs.getBooleanConfig(LocatoConfig.DB_SSL);
    private final static HikariConfig config = new HikariConfig();
    private final static HikariDataSource ds;

    static {
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

        switch(LocatoConfigs.getStringConfig(LocatoConfig.STORAGE_TYPE).toLowerCase()){
            case "mysql":
                config.setDriverClassName("com.mysql.jdbc.Driver");
                config.setJdbcUrl( "jdbc:mysql://" + hostname + ":" + port + "/" + dbName);
                config.setUsername(username);
                config.setPassword(password);
                config.addDataSourceProperty("useSSL", useSSL);
                config.addDataSourceProperty("maxLifetime", 18000);
                break;

            case "sqlite":
                config.setPoolName("Locato");
                config.setDriverClassName("org.sqlite.JDBC");
                config.setJdbcUrl("jdbc:sqlite:plugins/Locato/Locato.db");
                config.setConnectionTestQuery("SELECT 1");
                config.setMaxLifetime(60000); // 60 Sec
                config.setIdleTimeout(45000); // 45 Sec
                config.setMaximumPoolSize(50);
                break;

            default:
                System.out.println(ChatColor.YELLOW+"[Locato] Wrong database configuration! Check \"storage_type\" in config.yml");
                Bukkit.getPluginManager().disablePlugin(Locato.getPlugin(Locato.class));
        }
        ds = new HikariDataSource( config );
    }

    private LocatoSqlUtils() {}

    public static Connection openConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeConnection(){
        ds.close();
    }

    public static void initSQL(Connection connection, ArrayList<LocatoZLocation> zLocations){
        final boolean debug = LocatoConfigs.getDebug();
        final String storage = LocatoConfigs.getStringConfig(LocatoConfig.STORAGE_TYPE);

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
    }
}
