package shpp.level3.dbseed;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.time.StopWatch;
import shpp.level3.dto.ProductDTO;
import shpp.level3.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ProductTableGeneratorImpl extends TableGenerator{
    protected static final int MAX_LENGTH = 50;

    private final  Object lock = new Object();
    


    private final Validator validator;
    protected static final int BATCH_SIZE = 1000;

    StopWatch timer = new StopWatch();

    public static void setProductTypeMaxId(int productTypeMaxId) {
        ProductTableGeneratorImpl.productTypeMaxId = productTypeMaxId;
    }

    private static int productTypeMaxId;

    ExecutorService executor;
    public ProductTableGeneratorImpl(DBConnection connection) {
        super(connection);
        this.executor = Executors.newFixedThreadPool(Integer.parseInt(connection.getConfig().getProperty("threads")));
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public long generateRecords(long count) {
        timer.start();
        AtomicInteger invalidProduct = new AtomicInteger();
        AtomicInteger validProduct = new AtomicInteger();
        List<ProductDTO> productBatch = new ArrayList<>();
        Stream.generate(ProductTableGeneratorImpl::generateProduct)
                .limit(count)
                .forEach(product -> {
                    if(isValidProduct(product)){
                        productBatch.add(product);
                        validProduct.incrementAndGet();
                    }else{
                        invalidProduct.incrementAndGet();
                    }

                    synchronized (lock) {
                        if (productBatch.size() >= batchSize) {
                            while (availableThreads == 0){
                                try {
                                    logger.debug("Product stream generator pause");
                                    lock.wait(); // Пауза генерації, якщо немає вільних потоків
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            List<ProductDTO> batch = new ArrayList<>(productBatch);
                            productBatch.clear();
                            submitTask(batch);
                        }
                    }
                });
        if(!productBatch.isEmpty()){
            submitTask(productBatch);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.info("All batches inserted successfully.");
            logger.debug("Invalid product = {}", invalidProduct.get());
            logger.debug("Insert valid product = {}", validProduct.get());
            long time = timer.getTime(TimeUnit.MILLISECONDS);
            logger.info("Time execution = {}", time);
            logger.info("Insert product RPS={}", (validProduct.doubleValue() / time) * 1000);

            addIndexToTable();
            addUniqueConstraintToIdColumn();
        } catch (InterruptedException e) {
            logger.error("Error occurred while waiting for the threads to finish.",e);
            Thread.currentThread().interrupt();
        }
        return validProduct.get();
    }

    private boolean isValidProduct(ProductDTO product) {
        Set<ConstraintViolation<ProductDTO>> violations = validator.validate(product);
        return violations.isEmpty();
    }

    private void addUniqueConstraintToIdColumn() {
        try (Statement statement = connection.getConnection().createStatement()) {
            String alterSql = "ALTER TABLE product ADD CONSTRAINT unique_product_id UNIQUE (id)";
            statement.executeUpdate(alterSql);
            logger.info("Unique constraint 'unique_product_id' added to column 'id' in table 'retail.product'");
        } catch (SQLException e) {
            logger.error("Error occurred while adding unique constraint to column 'id' in table 'retail.product'", e);
        }
    }

    @Override
    public long generateRecords() {
        return 0;
    }

    private void submitTask(List<ProductDTO> batch){
        availableThreads--;
        executor.submit(() -> {
            insertBatch(batch);
            synchronized ((lock)){
                logger.debug("Unlock product stream generator");
                availableThreads++;
                lock.notifyAll();
            }
        });
    }

    private void insertBatch(List<ProductDTO> batch){
        logger.debug("Thread {}, receive batch to insert, size={}", Thread.currentThread().getName(), batch.size());
        String sql = "INSERT INTO product (product_type_id, name, price) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (ProductDTO product : batch) {
                preparedStatement.setLong(1, product.getProductTypeId());
                preparedStatement.setString(2, product.getName());
                preparedStatement.setString(3, product.getPrice());
                preparedStatement.addBatch();
            }
            logger.debug("Batch prepared.");
            preparedStatement.executeBatch();
            logger.debug("Thread {} execute insert batch", Thread.currentThread().getName());
        } catch (SQLException e) {
           logger.error("Error occurred while inserting batch",e);
        }

    }

    private static ProductDTO generateProduct() {
            ProductDTO product = new ProductDTO();
            product.setProductTypeId(random.nextInt(productTypeMaxId) + 1);
            product.setName(generateRandomString());
            product.setPrice(random.nextFloat() * 10000);

        return product;
    }

    private  static String generateRandomString() {
        int length  = random.nextInt(MAX_LENGTH);
        return random.ints('a', 'z' + 1)
                .limit(length + 1L)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private void addIndexToTable() {
        try (Statement statement = connection.getConnection().createStatement()) {
            String indexSql = "CREATE INDEX idx_product_id ON retail.product (id)";
            statement.executeUpdate(indexSql);
            logger.info("Index 'idx_product_id' added to table 'retail.product'");
        } catch (SQLException e) {
            logger.error("Error occurred while adding index to table 'retail.product'", e);
        }
    }

}
