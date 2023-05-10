package shpp.level3.dbseed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;

public class DDLScriptExecutor {
    private final Logger logger = LoggerFactory.getLogger(DDLScriptExecutor.class);
    private DBConnection connection;
    public DDLScriptExecutor(DBConnection connection) {
        this.connection = connection;
    }

    public void executeScript(String filename){
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        try (
                Statement statement = connection.getConnection().createStatement();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            StringBuilder scriptBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line.trim());

                // Check if the line ends with a semicolon, indicating the end of a statement
                if (line.trim().endsWith(";")) {
                    String script = scriptBuilder.toString();
                    statement.execute(script);
                    scriptBuilder.setLength(0); // Reset the StringBuilder for the next statement
                }
            }

            logger.info("DDL scripts executed successfully.");

        } catch (IOException | SQLException e) {
           logger.error("Can't execute ddl scripts", e);
        }
    }

}
