package shpp.level3.dbseed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shpp.level3.util.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DDLScriptExecutorTest {
    @Mock
    private DBConnection mockDBConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;

    private DDLScriptExecutor ddlScriptExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDBConnection.getConnection()).thenReturn(mockConnection);
        ddlScriptExecutor = new DDLScriptExecutor(mockDBConnection);
    }

    @Test
    void executeScript_ScriptExecutedSuccessfully() throws SQLException {
        String scriptFileName = "test_ddl.sql";
        when(mockConnection.createStatement()).thenReturn(mockStatement);;

        ddlScriptExecutor.executeScript(scriptFileName);

        verify(mockConnection, times(1)).createStatement();
        verify(mockStatement, times(2)).execute(anyString());
        verify(mockDBConnection, times(1)).setDataBase();
    }

}