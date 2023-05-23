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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class InventoryTableGeneratorImpl extends TableGenerator {
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

    StopWatch timer = new StopWatch();
    ExecutorService executor;

    public InventoryTableGeneratorImpl(DBConnection connection) {
        super(connection);
        this.executor = Executors.newFixedThreadPool(Integer.parseInt(connection.getConfig().getProperty("threads")));
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public long generateRecords(long count) {
        timer.start();
        AtomicInteger invalidInventory = new AtomicInteger();

        List<InventoryDTO> inventoryBatch = new ArrayList<>();
        Stream.generate(InventoryTableGeneratorImpl::generateInventory)
                .limit(count)
                .forEach(inventory -> {
                    if (isValidInventory(inventory)) {
                        inventoryBatch.add(inventory);
                        validInventory.incrementAndGet();
                    } else {
                        invalidInventory.incrementAndGet();
                    }

                    if (inventoryBatch.size() >= batchSize) {
                        while (availableThreads.get() == 0) {
                            try {
                                logger.debug("Inventory stream generator pause");
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        List<InventoryDTO> batch = new ArrayList<>(inventoryBatch);
                        inventoryBatch.clear();
                        submitTask(batch);

                    }

                });
        if (!inventoryBatch.isEmpty()) {
            submitTask(inventoryBatch);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.info("All batches inserted successfully.");
            logger.debug("Invalid inventory = {}", invalidInventory.get());
            logger.debug("Insert valid inventory = {}", validInventory.get());
            long time = timer.getTime(TimeUnit.MILLISECONDS);
            logger.info("Time execution = {}", time);
            logger.info("Created new row={}", getInventoryCount());
            logger.info("RPS insert statement execution = {}", (validInventory.doubleValue() / time) * 1000);
            logger.info("RPS new row created in inventory table = {}", ((double) getInventoryCount() / time) * 1000);
            addIndexToTable();
            addForeignKeyToTable("product");
            addForeignKeyToTable("store");
        } catch (InterruptedException e) {
            logger.error("Error occurred while waiting for the threads to finish.", e);
            Thread.currentThread().interrupt();
        }
        return validInventory.get();
    }

    @Override
    public long generateRecords() {
        return 0;
    }

    public long getInventoryCount() {
        String sql = "SELECT COUNT(*) FROM retail.inventory";
        try (Statement statement = connection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while getting inventory count", e);
        }
        return 0;
    }

    protected void submitTask(List<InventoryDTO> batch) {
        availableThreads.decrementAndGet();
        executor.submit(() -> {
            insertBatch(batch);
            logger.debug("Thread {} insert batch, available threads={}",
                    Thread.currentThread().getName(), availableThreads.get());
            availableThreads.incrementAndGet();

        });
    }

    protected void insertBatch(List<InventoryDTO> batch) {
        logger.debug("Thread {}, receive batch to insert, size={}", Thread.currentThread().getName(), batch.size());
        String sql = "INSERT INTO retail.inventory (product_id, store_id, quantity)" +
                " VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.getConnection().prepareStatement(sql)) {
            for (InventoryDTO inventory : batch) {
                preparedStatement.setLong(1, inventory.getProductId());
                preparedStatement.setLong(2, inventory.getStoreId());
                preparedStatement.setInt(3, inventory.getQuantity());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            logger.debug("Thread {} execute insert batch", Thread.currentThread().getName());
        } catch (SQLException e) {
            logger.error("Error occurred while inserting batch", e);
        }

    }

    private static InventoryDTO generateInventory() {
        int maxQuantity = Integer.parseInt(connection.getConfig().getProperty("inventory_max_quantity"));
        InventoryDTO inventory = new InventoryDTO();
        inventory.setProductId(random.nextInt(productMaxId) + 1);
        inventory.setStoreId(random.nextInt(storeMaxId) + 1);
        inventory.setQuantity(random.nextInt(maxQuantity) + 1);

        return inventory;
    }

    protected boolean isValidInventory(InventoryDTO inventory) {
        Set<ConstraintViolation<InventoryDTO>> violations = validator.validate(inventory);
        return violations.isEmpty();
    }

    protected void addIndexToTable() {
        timer.reset();
        timer.start();
        String indexSql = "CREATE INDEX idx_inventory_product_id ON inventory (product_id)";
        try (Statement statement = connection.getConnection().createStatement()) {
            statement.execute(indexSql);
            logger.info("Index product_id added to inventory table for time = {}ms",
                    timer.getTime(TimeUnit.MILLISECONDS));
        } catch (SQLException e) {
            logger.error("Error occurred while adding index to retail.inventory table", e);
        }
    }

    protected void addForeignKeyToTable(String fkTableName) {
        timer.reset();
        timer.start();
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("ALTER TABLE inventory ADD CONSTRAINT fk_inventory_").append(fkTableName);
        sqlQuery.append(" FOREIGN KEY (").append(fkTableName).append("_id)");
        sqlQuery.append(" REFERENCES ").append(fkTableName).append(" (id);");
        logger.debug("SQL={}", sqlQuery);

        try (Statement statement = connection.getConnection().createStatement()) {
            statement.execute(sqlQuery.toString());
            logger.info("FK {} added to inventory table for time = {}ms.",
                    fkTableName,
                    timer.getTime(TimeUnit.MILLISECONDS));
        } catch (SQLException e) {
            logger.error("Error occurred while adding index to retail.inventory table", e);
        }
    }
}
