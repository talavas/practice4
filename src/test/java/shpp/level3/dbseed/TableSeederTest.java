package shpp.level3.dbseed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shpp.level3.util.DBConnection;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TableSeederTest {
    @Mock
    private DBConnection mockDBConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    private TableSeeder tableSeeder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tableSeeder = new TableSeeder(mockDBConnection);
        when(mockDBConnection.getConnection()).thenReturn(mockConnection);
    }

    @Test
    void seed_OneColumn() throws SQLException {
        when(mockDBConnection.getConnection().prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockDBConnection.getConnection().createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1});

        tableSeeder.seed("data.csv");

        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();
        verify(mockPreparedStatement, times(2)).setString(anyInt(), anyString());

    }

    @Test
    void seed_TwoColumn() throws SQLException {
        when(mockDBConnection.getConnection().prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockDBConnection.getConnection().createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1});

        tableSeeder.setRandomForeignKey(2);
        tableSeeder.seed("data-2columns.csv");

        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();
        verify(mockPreparedStatement, times(2)).setString(anyInt(), anyString());
        verify(mockPreparedStatement, times(2)).setInt(anyInt(), anyInt());
    }

    @Test
    void test_getCSVHeaders(){
        String[] headers = {"column1"};
        assertArrayEquals(headers, tableSeeder.getCSVHeaders("data.csv"));

        String[] headers2Columns = {"column1", "column2"};
        assertArrayEquals(headers2Columns, tableSeeder.getCSVHeaders("data-2columns.csv"));
    }

    @Test
    void generateInsertQuery_withSingleColumn() {
        String tableName = "data";
        String[] headerNames = {"column1"};

        String insertQuery = tableSeeder.generateInsertQuery(tableName, headerNames);

        String expectedQuery = "INSERT INTO data (column1) VALUES (?)";
        Assertions.assertEquals(expectedQuery, insertQuery);
    }

    @Test
    void generateInsertQuery_withMultipleColumns() {
        String tableName = "data";
        String[] headerNames = {"column1", "column2", "column3"};

        String insertQuery = tableSeeder.generateInsertQuery(tableName, headerNames);

        String expectedQuery = "INSERT INTO data (column1,column2,column3) VALUES (?,?,?)";
        Assertions.assertEquals(expectedQuery, insertQuery);
    }

    @Test
    void setForeignKey_executesExpectedSQLQuery() throws SQLException {
        Statement mockStatement = Mockito.mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        String tableName = "table1";
        String fkTableName = "table2";

        tableSeeder.setForeignKey(tableName, fkTableName);

        String expectedSqlQuery = "ALTER TABLE table1 ADD CONSTRAINT fk_table1_table2 FOREIGN KEY (table2_id) REFERENCES table2 (id);";
        verify(mockStatement, times(1)).executeUpdate(expectedSqlQuery);
    }

}