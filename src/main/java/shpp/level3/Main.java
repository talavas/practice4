package shpp.level3;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.dbseed.*;
import shpp.level3.model.Store;
import shpp.level3.util.Config;
import shpp.level3.util.DBConnection;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class Main {
    static StopWatch timer = new StopWatch();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws SQLException {
        Config config = new Config("app.properties");
        DBConnection connection = new DBConnection(config);

        String productType = System.getProperty("type");
        if(productType != null){
            Store store = new Store();
            Store.setConnection(connection);
            timer.start();
            Store foundStore = store.findStoreWithMaxInventory(productType);
            if(foundStore != null){
                logger.info("Знайдено магазин {} з найбільшою кількістю {}шт. товарів категорії {} ", store, store.getMaxQuantity(), productType);
                logger.info("Час витрачений на пошук = {}мс", timer.getTime(TimeUnit.MILLISECONDS));
            }else{
                logger.info("Магазин не знайдено.");
            }


        }else{
            DDLScriptExecutor scriptExecutor = new DDLScriptExecutor(connection);
            StoreTableGeneratorImpl storeTableGenerator = new StoreTableGeneratorImpl(connection);
            ProductTableGeneratorImpl productTableGenerator = new ProductTableGeneratorImpl(connection);
            InventoryTableGeneratorImpl inventoryTableGenerator = new InventoryTableGeneratorImpl(connection);

            scriptExecutor.executeScript("ddl_scripts.sql");

            TableSeeder tableSeeder = new TableSeeder(connection);

            int cityMaxId = tableSeeder.seed("city.csv");

            tableSeeder.setRandomForeignKey(cityMaxId);

            int locationMaxId = tableSeeder.seed("location.csv");
            int productTypeMaxId = tableSeeder.seed("product_type.csv");
            int storeTypeMaxId = tableSeeder.seed("store_type.csv");

            storeTableGenerator.setStoreTypeMaxId(storeTypeMaxId);
            storeTableGenerator.setLocationTypeMaxId(locationMaxId);

            int storeMaxId = (int) storeTableGenerator.generateRecords();

            tableSeeder.setForeignKey("location", "city");
            tableSeeder.setForeignKey("store", "location");
            tableSeeder.setForeignKey("store", "store_type");

            ProductTableGeneratorImpl.setProductTypeMaxId(productTypeMaxId);
            int productMaxId = (int) productTableGenerator.generateRecords(Integer.parseInt(config.getProperty("products")));
            tableSeeder.setForeignKey("product", "product_type");

            InventoryTableGeneratorImpl.setProductMaxId(productMaxId);
            InventoryTableGeneratorImpl.setStoreMaxId(storeMaxId);
            inventoryTableGenerator.generateRecords(Integer.parseInt(config.getProperty("inventory")));

        }
        connection.disconnect();
    }
}