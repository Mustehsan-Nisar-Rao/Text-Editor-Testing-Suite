package business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.File;

import bll.EditorBO;
import dal.FacadeDAO;
import dal.EditorDBDAO;

public class ImportCommandTest {
    
    private EditorBO businessObj;
    
    @BeforeEach
    void setUp() {
        EditorDBDAO editorDBDAO = new EditorDBDAO();
        FacadeDAO facadeDAO = new FacadeDAO(editorDBDAO);
        businessObj = new EditorBO(facadeDAO);
    }
    
    @Test
    void testImportTextFile_ValidTxt() throws Exception {
        // Create temp file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "Hello World".getBytes(StandardCharsets.UTF_8));
        
        // Test actual importTextFiles method
        boolean result = businessObj.importTextFiles(
            tempFile.toFile(),
            tempFile.getFileName().toString()
        );
        
        assertTrue(result, "Valid text file should import successfully");
    }
    
    @Test
    void testImportTextFile_InvalidExtension() throws Exception {
        Path tempFile = Files.createTempFile("test", ".xyz");
        Files.write(tempFile, "Hello World".getBytes(StandardCharsets.UTF_8));
        
        boolean result = businessObj.importTextFiles(
            tempFile.toFile(),
            tempFile.getFileName().toString()
        );
        
        assertFalse(result, "Invalid extension should fail");
    }
}
