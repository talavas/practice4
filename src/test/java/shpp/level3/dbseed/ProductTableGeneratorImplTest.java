package shpp.level3.dbseed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shpp.level3.dto.ProductDTO;
import shpp.level3.util.Config;
import shpp.level3.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductTableGeneratorImplTest {
    @Mock
    private DBConnection mockDBConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private Statement mockStatement;

    private ProductTableGeneratorImpl productTableGenerator;


    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        Config config = new Config("app.properties");
        when(mockDBConnection.getConnection()).thenReturn(mockConnection);
        when(mockDBConnection.getConfig()).thenReturn(config);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        productTableGenerator = new ProductTableGeneratorImpl(mockDBConnection);
    }

    @Test
    void generateRecords(){
        long count = 10;
        ProductTableGeneratorImpl.setProductTypeMaxId(10);

        ProductTableGeneratorImpl spyGenerator = Mockito.spy(productTableGenerator);

        Mockito.doReturn(true).when(spyGenerator).isValidProduct(any(ProductDTO.class));
        Mockito.doNothing().when(spyGenerator).insertBatch(anyList());

        spyGenerator.generateRecords(count);

        verify(spyGenerator, times((int) count)).isValidProduct(any(ProductDTO.class));
        verify(spyGenerator, times(1)).submitTask(anyList());
        verify(spyGenerator, times(1)).insertBatch(anyList());
        verify(spyGenerator, times(1)).addIndexToTable();

    }

    @Test
    void insertBatch() throws SQLException {

        List<ProductDTO> batch = new ArrayList<>();
        ProductDTO product1 = new ProductDTO(1,"testproduct1", 10.00f);
        ProductDTO product2 = new ProductDTO(2, "testproduct2", 20.00f);
        batch.add(product1);
        batch.add(product2);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);


       productTableGenerator.insertBatch(batch);


        verify(mockPreparedStatement, times(2)).setLong(anyInt(), anyLong());
        verify(mockPreparedStatement, times(4)).setString(anyInt(), anyString());
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();

    }
}