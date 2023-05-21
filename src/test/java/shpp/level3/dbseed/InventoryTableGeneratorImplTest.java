package shpp.level3.dbseed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shpp.level3.dto.InventoryDTO;
import shpp.level3.util.Config;
import shpp.level3.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class InventoryTableGeneratorImplTest {
    @Mock
    private DBConnection mockDBConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;


    private Config config;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;
    @BeforeEach
    void setUp() throws SQLException {
        config = new Config("app.properties");
        MockitoAnnotations.openMocks(this);
        when(mockDBConnection.getConnection()).thenReturn(mockConnection);

        when(mockDBConnection.getConfig()).thenReturn(config);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        //        when(mockResultSet.getLong(eq(1))).thenReturn(anyLong());


    }

    @Test
    void generateRecords() {
        // Arrange
        long count = 10;
        InventoryTableGeneratorImpl generator = new InventoryTableGeneratorImpl(mockDBConnection);
        InventoryTableGeneratorImpl.setProductMaxId(10);
        InventoryTableGeneratorImpl.setStoreMaxId(10);

        InventoryTableGeneratorImpl spyGenerator = Mockito.spy(generator);

        Mockito.doReturn(true).when(spyGenerator).isValidInventory(any(InventoryDTO.class));
        Mockito.doNothing().when(spyGenerator).insertBatch(anyList());


        spyGenerator.generateRecords(count);

        verify(spyGenerator, times((int) count)).isValidInventory(any(InventoryDTO.class));
        verify(spyGenerator, times(1)).submitTask(anyList());
        verify(spyGenerator, times(1)).insertBatch(anyList());
        verify(spyGenerator, times(1)).addIndexToTable();
    }

    @Test
    void insertBatch_ExecutesPreparedStatementWithCorrectValues() throws SQLException {
        InventoryTableGeneratorImpl inventoryTableGenerator = new InventoryTableGeneratorImpl(mockDBConnection);

        List<InventoryDTO> batch = new ArrayList<>();
        InventoryDTO inventory1 = new InventoryDTO(1, 1, 1);
        InventoryDTO inventory2 = new InventoryDTO(2, 2, 1);
        batch.add(inventory1);
        batch.add(inventory2);

        String expectedSql = "INSERT INTO retail.inventory (product_id, store_id, quantity)" +
                " VALUES (?, ?, ?)" +
                "ON CONFLICT (product_id, store_id) "+
                "DO UPDATE SET quantity = inventory.quantity + excluded.quantity";

        when(mockConnection.prepareStatement(eq(expectedSql))).thenReturn(mockPreparedStatement);


        inventoryTableGenerator.insertBatch(batch);


        verify(mockPreparedStatement, times(4)).setLong(anyInt(), anyLong());
        verify(mockPreparedStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();

    }

}