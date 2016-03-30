package io.github.flibio.utils.sql;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import javax.sql.DataSource;

public class LocalSqlManager {

    private Logger logger;
    private String path;
    private SqlService sql;

    private Connection con;

    protected LocalSqlManager(Logger logger, String folderName, String file) {
        this.logger = logger;
        this.path = "config/" + folderName + "/" + file;
        this.sql = Sponge.getServiceManager().provide(SqlService.class).get();
    }

    /**
     * Creates a new LocalSqlManager instance.
     * 
     * @param plugin An instance of the main plugin class.
     * @param file The file associated with the data manager
     * @return The new LocalSqlManager instance, if the plugin class is valid.
     */
    public static Optional<LocalSqlManager> createInstance(Object plugin, String file) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new LocalSqlManager(logger, annotation.name().toLowerCase().replaceAll(" ", "_"), file));
        } else {
            return Optional.empty();
        }
    }

    private void openConnection() {
        try {
            DataSource source = sql.getDataSource("jdbc:h2:" + path);
            con = source.getConnection();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            con.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Executes an update to the database. Recommended to run in an async
     * thread.
     * 
     * @param sql The sql to execute.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     * @return If the update was successful or not.
     */
    public boolean executeUpdate(String sql, String... vars) {
        try {
            openConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setString(i + 1, vars[i]);
            }
            if (ps.executeUpdate() > 0) {
                closeConnection();
                return true;
            } else {
                closeConnection();
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Runs a query on the database. Recommended to run in an async thread.
     * 
     * @param sql The sql to run.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     * @return The ResultSet retrieved from the query.
     */
    public Optional<ResultSet> executeQuery(String sql, String... vars) {
        try {
            openConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setString(i + 1, vars[i]);
            }
            ResultSet rs = ps.executeQuery();
            closeConnection();
            return Optional.of(rs);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

}
