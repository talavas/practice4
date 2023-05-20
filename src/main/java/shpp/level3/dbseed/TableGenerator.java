package shpp.level3.dbseed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.util.Random;

public abstract class TableGenerator {
    protected static final Logger logger = LoggerFactory.getLogger(TableGenerator.class);
    protected static final Random random = new Random();
    protected static DBConnection connection;

    protected int batchSize = 1000;

    protected int availableThreads;

    public TableGenerator(DBConnection connection){

        this.connection = connection;
        String batchSize = connection.getConfig().getProperty("batch.size");
        if(batchSize != null){
            this.batchSize = Integer.parseInt(batchSize);
        }

        this.availableThreads = Integer.parseInt(connection.getConfig().getProperty("threads"));
    }

    public abstract long generateRecords(long count);

    public abstract long generateRecords();
}
