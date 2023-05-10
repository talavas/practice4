package shpp.level3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private final Config config;

    private Connection connection;

    public DBConnection(Config config) throws SQLException {
        this.config = config;
        connect();
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(
                config.getProperty("postgresql.url"),
                config.getProperty("db.username"),
                config.getProperty("db.password")
        );
        connection.setSchema("retail");
        logger.info("Connected to the database.");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Disconnected from the database.");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
