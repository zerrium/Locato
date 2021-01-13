package zerrium;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlCon {
    private final static String hostname = Locato.fc.getString("hostname");
    private final static int port = Locato.fc.getInt("port");
    private final static String db_name = Locato.fc.getString("database");
    private final static String username = Locato.fc.getString("username");
    private final static String password = Locato.fc.getString("password");
    private final static boolean useSSL = Locato.fc.getBoolean("use_SSL");
    private final static HikariConfig config = new HikariConfig();
    private final static HikariDataSource ds;

    static {
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

        switch(Locato.storage){
            case "mysql":
                config.setDriverClassName("com.mysql.jdbc.Driver");
                config.setJdbcUrl( "jdbc:mysql://" + hostname + ":" + port + "/" + db_name );
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

    private SqlCon() {}

    protected static Connection openConnection() throws SQLException {
        return ds.getConnection();
    }

    protected static void closeConnection(){
        ds.close();
    }
}
