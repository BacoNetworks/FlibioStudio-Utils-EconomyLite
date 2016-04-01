/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        this.path = "./config/" + folderName + "/" + file;
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
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private void reconnect() {
        try {
            if (con != null && !con.isClosed())
                con.close();
            openConnection();
        } catch (Exception e) {
            e.printStackTrace();
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
            reconnect();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setString(i + 1, vars[i]);
            }
            return (ps.executeUpdate() > 0);
        } catch (Exception e) {
            e.printStackTrace();
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
            reconnect();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setString(i + 1, vars[i]);
            }
            return Optional.of(ps.executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Queries the database and retrieves a column's data.
     * 
     * @param columnName The column to retrieve that data of.
     * @param type The type of data to retrieve.
     * @param sql The sql to run.
     * @param vars The variables to replace in sql. Replaced in chronological
     *        order.
     * @return The column's data, if it was found.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> queryType(String columnName, Class<T> type, String sql, String... vars) {
        try {
            Optional<ResultSet> rOpt = executeQuery(sql, vars);
            if (rOpt.isPresent()) {
                ResultSet rs = rOpt.get();
                rs.next();
                Object raw = rs.getObject(columnName);
                if (raw.getClass().equals(type)) {
                    return Optional.of((T) raw);
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Queries the database and checks if a row exists.
     * 
     * @param sql The sql to run.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     * @return If the row was found or not.
     */
    public boolean queryExists(String sql, String... vars) {
        try {
            Optional<ResultSet> rOpt = executeQuery(sql, vars);
            if (rOpt.isPresent()) {
                return rOpt.get().next();
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
