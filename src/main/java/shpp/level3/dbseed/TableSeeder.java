package shpp.level3.dbseed;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class TableSeeder {
    private final Logger logger = LoggerFactory.getLogger(TableSeeder.class);

    private final DBConnection connection;

    public void setRandomForeignKey(int randomForeignKey) {
        this.randomForeignKey = randomForeignKey;
    }
    private final Random random = new Random();
    private int randomForeignKey;

    public TableSeeder(DBConnection connection) {
        this.connection = connection;
    }

    public int seed(String filename) throws SQLException{
        int lastGeneratedId = -1;
        InputStream csvFileInputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(filename);

        CSVFormat csvFormat = CSVFormat.DEFAULT.
                builder().
                setHeader().
                setSkipHeaderRecord(true)
                .build();

        String[] headers = getCSVHeaders(filename);
        String tableName = filename.substring(0, filename.lastIndexOf("."));
        String insertQuery = generateInsertQuery(tableName, headers);
        if(csvFileInputStream != null){
            try (
                    Reader reader = new InputStreamReader(csvFileInputStream);
                    CSVParser csvParser = new CSVParser(reader, csvFormat);
                    PreparedStatement preparedStatement = connection.getConnection()
                            .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)
            ) {

                logger.debug("Start seeding table {}", tableName);

                for (CSVRecord csvRecord : csvParser) {
                    for(int i = 1; i <= headers.length; i++){
                        if(csvRecord.get(i-1).equals("random")){
                            preparedStatement.setInt(i, random.nextInt(randomForeignKey) + 1);
                        }else{
                            preparedStatement.setString(i, csvRecord.get(i-1));
                        }
                    }
                    preparedStatement.addBatch();
                }
                setRandomForeignKey(0);

                int[] insertedRows = preparedStatement.executeBatch();
                lastGeneratedId = insertedRows.length;

            } catch (IOException e) {
                logger.error("Can't load file {}", filename, e);
            }
        }
        //connection.executeStatement(generateCreateIndexSQL(tableName));

        return lastGeneratedId;
    }

    public String[] getCSVHeaders(String filename){
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        String[] headers = null;
        if(inputStream != null){
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                headers = reader.readLine().strip().split(",");
            } catch (IOException e) {
                logger.error("Can't read file");
            }
        }
        return headers;
    }

    private String generateInsertQuery(String tableName, String[] headerNames) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        queryBuilder.append("retail.").append(tableName).append(" (");

        for (String column : headerNames){
            queryBuilder.append(column).append(",");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);

        queryBuilder.append(") VALUES (");

        queryBuilder.append("?,".repeat(headerNames.length));
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(")");
        logger.debug("Generated insert query string {}", queryBuilder);
        return queryBuilder.toString();
    }

    private String generateCreateIndexSQL(String tableName){
        logger.debug("Generate index query");
        return "CREATE INDEX idx_"+tableName+"_id ON retail."+ tableName + " (id)";
    }

    public void setForeignKey(String tableName, String fkTableName){
        StringBuilder sqlQuery = new StringBuilder();
        //sqlQuery.append("ALTER TABLE retail.").append(fkTableName);
        //sqlQuery.append(" ADD CONSTRAINT uk_").append(fkTableName).append("_id").append(" UNIQUE (id);");

        sqlQuery.append( "ALTER TABLE retail.").append(tableName);
        sqlQuery.append(" ADD CONSTRAINT fk_").append(tableName).append("_").append(fkTableName);
        sqlQuery.append(" FOREIGN KEY (").append(fkTableName).append("_id) REFERENCES retail.").append(fkTableName + " (id);");

        logger.debug("SQL={}", sqlQuery);
        try (Statement statement = connection.getConnection().createStatement()) {
            statement.executeUpdate(sqlQuery.toString());
        } catch (SQLException e) {
            logger.error("Error occurred while adding foreign key constraint", e);
        }
    }

}
