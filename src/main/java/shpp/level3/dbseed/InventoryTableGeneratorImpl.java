package shpp.level3.dbseed;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.time.StopWatch;
import shpp.level3.dto.InventoryDTO;
import shpp.level3.util.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class InventoryTableGeneratorImpl extends TableGenerator{
    private final Validator validator;
    AtomicInteger validInventory = new AtomicInteger(0);

    public static void setStoreMaxId(int storeMaxId) {
        InventoryTableGeneratorImpl.storeMaxId = storeMaxId;
    }

    private static int storeMaxId;

    public static void setProductMaxId(int productMaxId) {
        InventoryTableGeneratorImpl.productMaxId = productMaxId;
    }

    private static int productMaxId;

    AtomicInteger invalidInventory = new AtomicInteger(0);
    StopWatch timer = new StopWatch();
    ExecutorService executor;

    public InventoryTableGeneratorImpl(DBConnection connection) {
        super(connection);
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public long generateRecords(long count) {
        timer.start();
        BlockingQueue<InventoryDTO> inventoryDTOs = new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors() * batchSize);
        Stream.generate(InventoryTableGeneratorImpl::generateInventory)
                .limit(count)
                .forEach(inventory -> {
                    if(isValidInventory(inventory)){
                        inventoryDTOs.offer(inventory);
                    }else{
                        invalidInventory.incrementAndGet();
                    }

                    if(inventoryDTOs.size() >= batchSize){
                        List<InventoryDTO> batch = new ArrayList<>();
                        int countBatch = inventoryDTOs.drainTo(batch, batchSize);
                        if(countBatch !=0 ){
                            executor.submit(() -> insertBatch(batch));
                        }
                    }

                });
        List<InventoryDTO> batch = new ArrayList<>();
        int countBatch = inventoryDTOs.drainTo(batch, batchSize);
        if(countBatch !=0 ){
            executor.submit(() -> insertBatch(batch));
        }

        executor.shutdown();

        try {
            // Wait for all threads to finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.info("All batches inserted successfully.");
            logger.debug("Invalid inventory = {}", invalidInventory.get());
            logger.info("Created INSERT statement for table inventory = {}", validInventory.get());
            logger.debug("Created new rows in table inventory = {}", getInventoryCount());
            long time = timer.getTime(TimeUnit.MILLISECONDS);logger.info("Time execution = {}", time);
            logger.info("Create new rows in inventory table RPS={}", ((double) getInventoryCount() / time) * 1000);
            addIndexToTable();
            addForeignKeyToTable("product");
            addForeignKeyToTable("store");
        } catch (InterruptedException e) {
            logger.error("Error occurred while waiting for the threads to finish.",e);
            Thread.currentThread().interrupt();
        }
        return validInventory.get();
    }

    @Override
    public long generateRecords() {
        return 0;
    }

    private void insertBatch(List<InventoryDTO> batch){
        logger.debug("Thread {}, receive batch to insert, size={}", Thread.currentThread().getName(), batch.size());
        String sql = "INSERT INTO inventory (product_id, store_id, quantity)" +
                " VALUES (?, ?, ?)" +
                "ON CONFLICT (product_id, store_id) "+
                "DO UPDATE SET quantity = inventory.quantity + excluded.quantity";
        try (PreparedStatement preparedStatement = connection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (InventoryDTO inventory : batch) {
                preparedStatement.setLong(1, inventory.getProductId());
                preparedStatement.setLong(2, inventory.getStoreId());
                preparedStatement.setInt(3, inventory.getQuantity());
                preparedStatement.addBatch();
            }

            int[] count = preparedStatement.executeBatch();
            validInventory.addAndGet(count.length);
            logger.debug("Thread {} execute insert batch", Thread.currentThread().getName());
        } catch (SQLException e) {
            logger.error("Error occurred while inserting batch",e);
        }

    }

    private static InventoryDTO generateInventory(){
        int maxQuantity = Integer.parseInt(connection.getConfig().getProperty("inventory_max_quantity"));
        InventoryDTO inventory = new InventoryDTO();
        inventory.setProductId(random.nextInt(productMaxId)+1);
        inventory.setStoreId(random.nextInt(storeMaxId)+1);
        inventory.setQuantity(random.nextInt(maxQuantity)+1);

        return inventory;
    }

    private long getInventoryCount(){
        String sqlStm = "SELECT COUNT(*) FROM inventory";
        try (Statement statement = connection.getConnection().createStatement()) {
            ResultSet result = statement.executeQuery(sqlStm);
            if (result.next()) {
                return result.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Can't select all rows from inventory", e);
        }
        return 0;
    }

    private boolean isValidInventory(InventoryDTO inventory) {
        Set<ConstraintViolation<InventoryDTO>> violations = validator.validate(inventory);
        return violations.isEmpty();
    }

    private void addIndexToTable() {
        String indexSql = "CREATE INDEX idx_inventory_product_store ON retail.inventory (product_id, store_id)";
        try (Statement statement = connection.getConnection().createStatement()) {
            statement.execute(indexSql);
            logger.info("Index added to retail.inventory table.");
        } catch (SQLException e) {
            logger.error("Error occurred while adding index to retail.inventory table", e);
        }
    }

    private void addForeignKeyToTable(String fkTableName){
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("ALTER TABLE retail.inventory ADD CONSTRAINT fk_inventory_").append(fkTableName);
        sqlQuery.append(" FOREIGN KEY (").append(fkTableName).append("_id)");
        sqlQuery.append(" REFERENCES retail.").append(fkTableName).append(" (id);");
        logger.debug("SQL={}", sqlQuery);

        try (Statement statement = connection.getConnection().createStatement()) {
            statement.execute(sqlQuery.toString());
            logger.info("FK {} added to retail.inventory table.", fkTableName);
        } catch (SQLException e) {
            logger.error("Error occurred while adding index to retail.inventory table", e);
        }
    }
}
