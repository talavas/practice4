package shpp.level3.dbseed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shpp.level3.util.Config;
import shpp.level3.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreTableGeneratorImplTest {
    @Mock
    private DBConnection mockDBConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private Statement mockStatement;

    private StoreTableGeneratorImpl storeTableGenerator;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        Config config = new Config("app.properties");
        when(mockDBConnection.getConnection()).thenReturn(mockConnection);
        when(mockDBConnection.getConfig()).thenReturn(config);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        storeTableGenerator = spy(new StoreTableGeneratorImpl(mockDBConnection));
    }

    @Test
    void generateRecords() throws SQLException {
        // Arrange
        int storeTypeMaxId = 5;
        int locationTypeMaxId = 10;
        storeTableGenerator.setStoreTypeMaxId(storeTypeMaxId);
        storeTableGenerator.setLocationTypeMaxId(locationTypeMaxId);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        int[] mockBatchResult = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        when(mockPreparedStatement.executeBatch()).thenReturn(mockBatchResult);

        storeTableGenerator.generateRecords();

        verify(mockPreparedStatement, times(locationTypeMaxId)).setInt(eq(1), anyInt());
        verify(mockPreparedStatement, times(locationTypeMaxId)).setInt(eq(2), anyInt());
        verify(mockPreparedStatement, times(locationTypeMaxId)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();
    }

}