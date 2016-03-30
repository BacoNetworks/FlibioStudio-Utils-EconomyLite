package io.github.flibio.utils.sql;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Consumer;

import javax.sql.DataSource;

public class LocalSqlManager {

    private Logger logger;
    private Object plugin;
    private String path;
    private SqlService sql;

    private Connection con;

    protected LocalSqlManager(Logger logger, String folderName, String file, Object plugin) {
        this.logger = logger;
        this.plugin = plugin;
        this.path = "config/" + folderName + "/" + file;
        this.sql = Sponge.getServiceManager().provide(SqlService.class).get();
    }

    /**
     * Creates a new DataManager instance.
     * 
     * @param plugin An instance of the main plugin class.
     * @param file The file associated with the data manager
     * @return The new DataManager instance, if the plugin class is valid.
     */
    public static Optional<LocalSqlManager> createInstance(Object plugin, String file) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new LocalSqlManager(logger, annotation.name().toLowerCase().replaceAll(" ", "_"), file, plugin));
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
     * Executes an update to the database. Automatically runs in an async
     * thread.
     * 
     * @param sql The sql to execute.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     */
    public void executeUpdate(String sql, String... vars) {
        Sponge.getScheduler().createTaskBuilder().execute(c -> {
            try {
                openConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                for (int i = 0; i < vars.length; i++) {
                    ps.setString(i + 1, vars[i]);
                }
                ps.executeUpdate();
                closeConnection();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }).async().submit(plugin);
    }

    /**
     * Runs a query on the database. Automatically runs in an async thread.
     * 
     * @param callback Defines what will be run once the query is finished.
     * @param sql The sql to run.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     */
    public void executeQuery(Consumer<Optional<ResultSet>> callback, String sql, String... vars) {
        Sponge.getScheduler().createTaskBuilder().execute(c -> {
            try {
                openConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                for (int i = 0; i < vars.length; i++) {
                    ps.setString(i + 1, vars[i]);
                }
                ResultSet rs = ps.executeQuery();
                closeConnection();
                callback.accept(Optional.of(rs));
            } catch (Exception e) {
                logger.error(e.getMessage());
                callback.accept(Optional.empty());
            }
        }).async().submit(plugin);
    }

}
