/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2017 FlibioStudio
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
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

public class SqlManager {

    private Logger logger;
    private String datasource;
    private SqlService sql;

    private Connection con;

    protected SqlManager(Logger logger, String datasource) {
        this.logger = logger;
        this.datasource = datasource;
        this.sql = Sponge.getServiceManager().provide(SqlService.class).get();
    }

    public boolean initialTestConnection() {
        try {
            sql.getDataSource(datasource);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tests if the connection to the database if functional.
     *
     * @return If the connection to the database if functional.
     */
    public boolean testConnection() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT 1");
            ps.executeQuery();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void openConnection() {
        try {
            DataSource source = sql.getDataSource(datasource);
            con = source.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private void reconnect() {
        try {
            if (!testConnection() || con == null) {
                if (con != null && !con.isClosed())
                    con.close();
                openConnection();
            }
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
    public boolean executeUpdate(String sql, Object... vars) {
        try {
            reconnect();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
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
    public Optional<ResultSet> executeQuery(String sql, Object... vars) {
        try {
            reconnect();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
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
    public <T> Optional<T> queryType(String columnName, Class<T> type, String sql, Object... vars) {
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
     * Queries the database and retrieves a list of data.
     *
     * @param columnName The column whose data will be added to the list.
     * @param type The type of data to retrieve.
     * @param sql The sql to run.
     * @param vars The variables to replace in sql. Replaced in chronological
     *        order.
     * @return The list of data.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> queryTypeList(String columnName, Class<T> type, String sql, Object... vars) {
        ArrayList<T> list = new ArrayList<>();
        try {
            Optional<ResultSet> rOpt = executeQuery(sql, vars);
            if (rOpt.isPresent()) {
                ResultSet rs = rOpt.get();
                while (rs.next()) {
                    Object raw = rs.getObject(columnName);
                    list.add((T) raw);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return list;
    }

    /**
     * Queries the database and checks if a row exists.
     *
     * @param sql The sql to run.
     * @param vars The variables to replace in the sql. Replaced in
     *        chronological order.
     * @return If the row was found or not.
     */
    public boolean queryExists(String sql, Object... vars) {
        try {
            Optional<ResultSet> rOpt = executeQuery(sql, vars);
            if (rOpt.isPresent()) {
                ResultSet rs = rOpt.get();
                return rs.next();
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
