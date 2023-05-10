package shpp.level3.dbseed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.util.Random;

public abstract class TableGenerator {
    protected final Logger logger = LoggerFactory.getLogger(TableGenerator.class);
    protected final Random random = new Random();
    DBConnection connection;

    public TableGenerator(DBConnection connection){
        this.connection = connection;
    }

    public abstract long generateRecords(long count);

    public abstract long generateRecords();
}
