package shpp.level3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    public Config getConfig() {
        return config;
    }

    private final Config config;


    private Connection connection;

    public DBConnection(Config config) throws SQLException {
        this.config = config;
        connect();
    }

    public void connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", config.getProperty("db.username"));
        props.setProperty("password", config.getProperty("db.password"));
        props.setProperty("useServerPrepStmts", "true");

        connection = DriverManager.getConnection(config.getProperty("postgresql.url"), props);
        logger.info("Connected to the database.");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            logger.info("Disconnected from the database.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setDataBase(){
        try {
            connection.setSchema(config.getProperty("db.name"));
        } catch (SQLException e) {
            logger.error("Can't set database scheme");
        }
    }

}
