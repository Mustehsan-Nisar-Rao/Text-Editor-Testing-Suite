package Business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;

import bll.EditorBO;
import dal.FacadeDAO;
import dal.EditorDBDAO;
import dto.Documents;

public class ImportCommandTest {
    
    private EditorBO businessObj;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        System.out.println("\n🔵 SETUP START ==================");
        try {
            EditorDBDAO editorDBDAO = new EditorDBDAO();
            System.out.println("✅ EditorDBDAO created");
            
            FacadeDAO facadeDAO = new FacadeDAO(editorDBDAO);
            System.out.println("✅ FacadeDAO created");
            
            businessObj = new EditorBO(facadeDAO);
            System.out.println("✅ EditorBO created");
        } catch (Exception e) {
            System.out.println("❌ Setup error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("🔵 SETUP END ==================\n");
    }
    
    @Test
    void testImportTextFile_ValidTxt() throws Exception {
        System.out.println("\n🟢 TEST START ==================");
        
        // Create test file
        Path testFile = tempDir.resolve("test.txt");
        String content = "Test import content";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
        
        System.out.println("📄 File created:");
        System.out.println("   Path: " + testFile.toAbsolutePath());
        System.out.println("   Exists: " + testFile.toFile().exists());
        System.out.println("   Size: " + testFile.toFile().length() + " bytes");
        System.out.println("   Name: " + testFile.getFileName());
        System.out.println("   Extension: " + getExtension(testFile.getFileName().toString()));
        
        // Call importTextFiles
        System.out.println("\n📞 Calling importTextFiles...");
        boolean result = false;
        try {
            result = businessObj.importTextFiles(
                testFile.toFile(),
                testFile.getFileName().toString()
            );
            System.out.println("   Result: " + (result ? "✅ true" : "❌ false"));
        } catch (Exception e) {
            System.out.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Check database
        System.out.println("\n📊 Database check:");
        try {
            List<Documents> docs = businessObj.getAllFiles();
            System.out.println("   Total files in DB: " + docs.size());
            boolean found = false;
            for (Documents doc : docs) {
                System.out.println("   - ID: " + doc.getId() + ", Name: " + doc.getName());
                if (doc.getName().equals("test.txt")) {
                    found = true;
                }
            }
            System.out.println("   File found in DB: " + (found ? "✅ yes" : "❌ no"));
        } catch (Exception e) {
            System.out.println("❌ DB check error: " + e.getMessage());
        }
        
        System.out.println("🟢 TEST END ==================\n");
        
        assertTrue(result, "Import should succeed");
    }
    
    private String getExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        return (lastIndexOfDot == -1) ? "" : fileName.substring(lastIndexOfDot + 1);
    }
}