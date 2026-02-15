package Business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import dal.DatabaseConnection;

public class DBConnectionTest {
    
    @Test
    void testDatabaseConnection() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        
        assertNotNull(conn, "Connection should not be null");
        
        try {
            assertFalse(conn.isClosed(), "Connection should be open");
            System.out.println("✅ Database connected successfully!");
        } catch (Exception e) {
            fail("Connection error: " + e.getMessage());
        }
    }
}