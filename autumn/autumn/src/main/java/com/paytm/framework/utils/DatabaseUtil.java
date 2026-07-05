package com.paytm.framework.utils;

import com.paytm.framework.reporting.Reporter;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Class to manage the database related functionality like managing the database connection,
 * Executing the specified SQL queries etc.
 */
public class DatabaseUtil {

    private static DatabaseUtil dbConnectionUtil;
    private static ConcurrentMap<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private Connection connection;
    private Statement statement;

    private DatabaseUtil() {
    }

    /**
     * Class constructor creates a new object of DatabaseUtil if
     * that object already doesn't exist, otherwise returns the existing object.
     *
     * @return The synchronized object of DatabaseUtil class.
     */
    public static synchronized DatabaseUtil getInstance() {
        if (dbConnectionUtil == null) {
            dbConnectionUtil = new DatabaseUtil();
        }
        return dbConnectionUtil;
    }

    /**
     * Creates the database connection with the database specified in
     * the connection URL.
     *
     * @param dbConnectionURL The string URL specifying the database details.
     * @return The object of database connection.
     */
    public synchronized Connection getConnection(String dbConnectionURL) {

        if (connectionMap.containsKey(dbConnectionURL)) {
            Connection connection = connectionMap.get(dbConnectionURL);

            try {
                if (connection.isValid(30)) {
                    return connection;
                } else {
                    connectionMap.get(dbConnectionURL).close();
                    connectionMap.remove(dbConnectionURL);
                }
            } catch (SQLException e) {
                try {
                    connectionMap.get(dbConnectionURL).close();
                } catch (SQLException e1) {
                    Reporter.report.error("Couldn't closeAllConnections "+e1.getMessage());
                }
                Reporter.report.error("Couldn't getConnection "+e.getMessage());
            }
        }

        Connection connection = null;
        String[] arr = dbConnectionURL.split(":");
        String dbDriver = arr[1];

        switch (dbDriver.toUpperCase()) {
            case "MYSQL":
                DbUtils.loadDriver("com.mysql.jdbc.Driver");
                break;
            case "ORACLE":
                DbUtils.loadDriver("oracle.jdbc.driver.OracleDriver");
                break;
            case "MICROSOFT":
                DbUtils.loadDriver("com.microsoft.jdbc.sqlserver.SQLServerDriver");
                break;
            default:
                throw new RuntimeException("Incorrect database driver: " + dbDriver);
        }
        try {
            Properties properties = new Properties();
            properties.put("connectTimeout","60000");
            connection = DriverManager.getConnection(dbConnectionURL, properties);
            connectionMap.put(dbConnectionURL, connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the existing database connection from the database specified in
     * the database connection URL.
     *
     * @param dbConnectionURL The string URL specifying the database details.
     */
    public synchronized void closeConnection(String dbConnectionURL) {
        if (connectionMap.containsKey(dbConnectionURL)) {
            Connection connection = connectionMap.get(dbConnectionURL);
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Reporter.report.error("Couldn't closeConnection "+e.getMessage());
            } finally {
                connectionMap.remove(dbConnectionURL);
            }
        }
    }

    /**
     * Closes all the currently opened database connections.
     */
    public synchronized void closeAllConnections() {
        Set<String> keys = connectionMap.keySet();
        for (String key : keys) {
            Connection connection = connectionMap.get(key);
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Reporter.report.error("Couldn't closeAllConnections "+e.getMessage());
            } finally {
                connectionMap.remove(key);
            }
        }
    }

    /**
     * Executes the specified sql select query with respect the database provided in the dbConnectionURL.
     *
     * @param dbConnectionURL The string URL specifying the database details.
     * @param sqlQuery        The SQL select query to execute.
     * @return The resultset returned after execution of the specified SQL query.
     */
    public synchronized List<Map<String, Object>> executeSelectQuery(String dbConnectionURL, String sqlQuery) {
        ResultSet resultSet;
        try {
            connection = getConnection(dbConnectionURL);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlQuery);
            MapListHandler mapListHandler = new MapListHandler();
            List<Map<String, Object>> result = mapListHandler.handle(resultSet);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes the specified sql update query with respect the database provided in the dbConnectionURL.
     *
     * @param dbConnectionURL The string URL specifying the database details.
     * @param sqlQuery        The SQL update query to execute.
     */
    public synchronized void executeUpdateQuery(String dbConnectionURL, String sqlQuery) {

        try {
            connection = getConnection(dbConnectionURL);
            statement = connection.createStatement();
            statement.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }
}