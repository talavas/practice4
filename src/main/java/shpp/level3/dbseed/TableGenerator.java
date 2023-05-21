package shpp.level3.dbseed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TableGenerator {
    protected static final Logger logger = LoggerFactory.getLogger(TableGenerator.class);
    protected static final Random random = new Random();
    protected static DBConnection connection;

    protected int batchSize = 1000;

    protected AtomicInteger availableThreads;

    protected TableGenerator(DBConnection connection){

        this.connection = connection;
        String batchSizeStr = connection.getConfig().getProperty("batch.size");
        if(batchSizeStr != null){
            this.batchSize = Integer.parseInt(batchSizeStr);
        }

        this.availableThreads = new AtomicInteger(Integer.parseInt(connection.getConfig().getProperty("threads")));
    }

    public abstract long generateRecords(long count);

    public abstract long generateRecords();
}
