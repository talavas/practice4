package shpp.level3.dbseed;

import shpp.level3.util.DBConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class StoreTableGeneratorImpl extends TableGenerator{

    public void setStoreTypeMaxId(int storeTypeMaxId) {
        this.storeTypeMaxId = storeTypeMaxId;
    }

    private int storeTypeMaxId;

    public void setLocationTypeMaxId(int locationTypeMaxId) {
        this.locationTypeMaxId = locationTypeMaxId;
    }

    private int locationTypeMaxId;
    public StoreTableGeneratorImpl(DBConnection connection) {
        super(connection);
    }

    @Override
    public long generateRecords(long count) {
       return 0;
    }

    @Override
    public long generateRecords() {
        String insertQuery = "INSERT INTO retail.store (store_type_id,location_id) VALUES (?,?)";
        int lastGeneratedId = 0;
        try(PreparedStatement preparedStatement = connection.getConnection().prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)){
            for(int i = 1; i <= storeTypeMaxId; i++) {
                for (int j = 1; j <= locationTypeMaxId; j++) {
                    preparedStatement.setInt(1, i);
                    preparedStatement.setInt(2, j);
                    preparedStatement.addBatch();
                }
            }

            int[] insertedRows = preparedStatement.executeBatch();

            lastGeneratedId = insertedRows.length;

        } catch (SQLException e) {
            logger.error("Can't create sql statement", e);
        }
        return lastGeneratedId;
    }
}
