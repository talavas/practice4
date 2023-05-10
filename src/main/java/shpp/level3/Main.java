package shpp.level3;

import shpp.level3.dbseed.DDLScriptExecutor;
import shpp.level3.dbseed.StoreTableGeneratorImpl;
import shpp.level3.dbseed.TableSeeder;
import shpp.level3.util.Config;
import shpp.level3.util.DBConnection;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Config config = new Config();
        DBConnection connection = new DBConnection(config);

        DDLScriptExecutor scriptExecutor = new DDLScriptExecutor(connection);
        StoreTableGeneratorImpl storeTableGenerator = new StoreTableGeneratorImpl(connection);

        scriptExecutor.executeScript("ddl_scripts.sql");

        TableSeeder tableSeeder = new TableSeeder(connection);

        int cityMaxId = tableSeeder.seed("city.csv");
        tableSeeder.setRandomForeignKey(cityMaxId);
        int locationMaxId = tableSeeder.seed("location.csv");
        tableSeeder.seed("product_type.csv");
        int storeMaxId = tableSeeder.seed("store_type.csv");

        storeTableGenerator.setStoreTypeMaxId(storeMaxId);
        storeTableGenerator.setLocationTypeMaxId(locationMaxId);
        storeTableGenerator.generateRecords();
    }
}