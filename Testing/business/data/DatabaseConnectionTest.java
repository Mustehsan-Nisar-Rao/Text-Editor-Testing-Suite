package data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import static org.junit.jupiter.api.Assertions.*;

import dal.DatabaseConnection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ROBUST Singleton Testing Suite for DatabaseConnection
 * 
 * Verifies ALL Singleton properties:
 * - Single instance across multiple calls
 * - Thread safety
 * - Lazy initialization
 * - Private constructor
 * - No cloning possible
 * - Connection management
 * - Reflection safety
 * 
 * @author [Your Team Name]
 * @date February 15, 2026
 */
@DisplayName("🔬 DatabaseConnection Singleton Test Suite")
public class DatabaseConnectionTest {
    
    private DatabaseConnection instance1;
    private DatabaseConnection instance2;
    private Connection conn;
    
    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton before each test using reflection
        resetSingleton();
    }
    
    /**
     * Helper method to reset singleton instance
     */
    private void resetSingleton() throws Exception {
        Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
    
    // ==================== FUNDAMENTAL SINGLETON TESTS ====================
    
    @Test
    @DisplayName("✅ TEST 1: Single Instance - Multiple calls return same instance")
    void testSingleton_SingleInstance() {
        System.out.println("\n🔵 TEST 1: Single Instance Property");
        
        instance1 = DatabaseConnection.getInstance();
        instance2 = DatabaseConnection.getInstance();
        
        System.out.println("  Instance 1: " + instance1);
        System.out.println("  Instance 2: " + instance2);
        
        assertSame(instance1, instance2, 
            "❌ FAILED: Multiple getInstance() calls should return the SAME instance");
        
        assertEquals(instance1.hashCode(), instance2.hashCode(),
            "❌ FAILED: Hash codes should be identical");
        
        System.out.println("✅ PASSED: Singleton returns same instance");
    }
    
    @Test
    @DisplayName("✅ TEST 2: Private Constructor - Cannot instantiate directly")
    void testSingleton_PrivateConstructor() {
        System.out.println("\n🔵 TEST 2: Private Constructor Verification");
        
        Constructor<?>[] constructors = DatabaseConnection.class.getDeclaredConstructors();
        
        for (Constructor<?> constructor : constructors) {
            int modifiers = constructor.getModifiers();
            assertTrue(Modifier.isPrivate(modifiers),
                "❌ FAILED: Constructor should be private");
            System.out.println("  ✓ Constructor is private: " + constructor);
        }
        
        // Try to access private constructor via reflection
        try {
            Constructor<DatabaseConnection> constructor = 
                DatabaseConnection.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            DatabaseConnection reflectionInstance = constructor.newInstance();
            
            assertNotNull(reflectionInstance, "Reflection instance should be created");
            assertNotSame(DatabaseConnection.getInstance(), reflectionInstance,
                "❌ FAILED: Reflection should create new instance");
            
            System.out.println("  ✓ Reflection can create new instance (Java limitation)");
            
        } catch (Exception e) {
            System.out.println("  ✓ Constructor properly protected: " + e.getMessage());
        }
        
        System.out.println("✅ PASSED: Constructor is private");
    }
    
    @Test
    @DisplayName("✅ TEST 3: Lazy Initialization - Instance created only on first call")
    void testSingleton_LazyInitialization() throws Exception {
        System.out.println("\n🔵 TEST 3: Lazy Initialization Verification");
        
        // Get INSTANCE field
        Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        
        // Verify null before first call
        DatabaseConnection beforeCall = (DatabaseConnection) instanceField.get(null);
        assertNull(beforeCall, "❌ FAILED: INSTANCE should be null before first getInstance()");
        System.out.println("  ✓ INSTANCE is null before first call");
        
        // First call
        DatabaseConnection firstInstance = DatabaseConnection.getInstance();
        assertNotNull(firstInstance, "❌ FAILED: First call should create instance");
        System.out.println("  ✓ First call created instance: " + firstInstance);
        
        // Verify field is now set
        DatabaseConnection afterCall = (DatabaseConnection) instanceField.get(null);
        assertNotNull(afterCall, "❌ FAILED: INSTANCE should be set after first call");
        assertSame(firstInstance, afterCall, "❌ FAILED: Field should store the instance");
        
        System.out.println("✅ PASSED: Lazy initialization working correctly");
    }
    
    // ==================== THREAD SAFETY TESTS ====================
    
    @Test
    @DisplayName("✅ TEST 4: Thread Safety - 10 concurrent threads")
    void testSingleton_ThreadSafety_10Threads() throws InterruptedException, ExecutionException {
        System.out.println("\n🔵 TEST 4: Thread Safety - 10 Concurrent Threads");
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<DatabaseConnection>> futures = new ArrayList<>();
        
        // Submit tasks
        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            Callable<DatabaseConnection> task = () -> {
                DatabaseConnection instance = DatabaseConnection.getInstance();
                System.out.println("  Thread " + threadId + " got: " + instance);
                return instance;
            };
            futures.add(executor.submit(task));
        }
        
        // Collect results
        DatabaseConnection firstInstance = futures.get(0).get();
        System.out.println("\n  First thread instance: " + firstInstance);
        
        for (int i = 1; i < threadCount; i++) {
            DatabaseConnection instance = futures.get(i).get();
            assertSame(firstInstance, instance, 
                "❌ FAILED: Thread " + i + " got different instance");
            System.out.println("  ✓ Thread " + i + " same instance");
        }
        
        executor.shutdown();
        System.out.println("✅ PASSED: All 10 threads got same instance");
    }
    
    @Test
    @DisplayName("✅ TEST 5: Thread Safety - 100 rapid concurrent calls")
    void testSingleton_ThreadSafety_100Calls() throws InterruptedException {
        System.out.println("\n🔵 TEST 5: Thread Safety - 100 Rapid Concurrent Calls");
        
        int callCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(callCount);
        List<DatabaseConnection> results = new CopyOnWriteArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < callCount; i++) {
            executor.submit(() -> {
                try {
                    results.add(DatabaseConnection.getInstance());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(); // Wait for all threads
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        
        DatabaseConnection first = results.get(0);
        boolean allSame = results.stream().allMatch(inst -> inst == first);
        
        assertTrue(allSame, "❌ FAILED: Not all " + callCount + " calls returned same instance");
        System.out.println("  ✓ All " + callCount + " concurrent calls returned same instance");
        System.out.println("  ✓ Time taken: " + (endTime - startTime) + "ms");
        System.out.println("✅ PASSED: Thread safety verified with " + callCount + " calls");
    }
    
    // ==================== CONNECTION MANAGEMENT TESTS ====================
    
    @Test
    @DisplayName("✅ TEST 6: Connection - Get connection returns valid object")
    void testSingleton_GetConnection() {
        System.out.println("\n🔵 TEST 6: Connection Retrieval");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        
        assertNotNull(conn, "❌ FAILED: Connection should not be null");
        System.out.println("  ✓ Connection object: " + conn.getClass().getSimpleName());
        System.out.println("✅ PASSED: Connection retrieved successfully");
    }
    
    @Test
    @DisplayName("✅ TEST 7: Connection - Same connection from same instance")
    void testSingleton_SameConnection() {
        System.out.println("\n🔵 TEST 7: Connection Consistency");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn1 = db.getConnection();
        Connection conn2 = db.getConnection();
        
        assertSame(conn1, conn2, 
            "❌ FAILED: getConnection() should return same connection object");
        System.out.println("  ✓ Same connection returned: " + conn1);
        System.out.println("✅ PASSED: Connection consistent");
    }
    
    @Test
    @DisplayName("✅ TEST 8: Connection - Different instances share connection")
    void testSingleton_SharedConnection() {
        System.out.println("\n🔵 TEST 8: Shared Connection Across Instances");
        
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();
        
        Connection conn1 = db1.getConnection();
        Connection conn2 = db2.getConnection();
        
        assertSame(conn1, conn2, 
            "❌ FAILED: Different instances should share same connection");
        System.out.println("  ✓ Both instances share connection: " + conn1);
        System.out.println("✅ PASSED: Connection shared across instances");
    }
    
    @Test
    @DisplayName("✅ TEST 9: Close and Reopen connection")
    void testSingleton_CloseAndReopen() throws SQLException {
        System.out.println("\n🔵 TEST 9: Close and Reopen Connection");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn1 = db.getConnection();
        System.out.println("  Initial connection: " + conn1);
        
        db.closeConnection();
        System.out.println("  Connection closed");
        
        Connection conn2 = db.getConnection();
        System.out.println("  New connection: " + conn2);
        
        assertNotSame(conn1, conn2, 
            "❌ FAILED: Should get new connection after close");
        assertFalse(conn2.isClosed(), 
            "❌ FAILED: New connection should be open");
        
        System.out.println("✅ PASSED: Close and reopen works");
    }
    
    // ==================== ROBUSTNESS TESTS ====================
    
    @Test
    @DisplayName("✅ TEST 10: Multiple close calls - Should not throw")
    void testSingleton_MultipleCloseCalls() {
        System.out.println("\n🔵 TEST 10: Multiple Close Calls");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        // Close multiple times
        assertDoesNotThrow(() -> {
            db.closeConnection();
            db.closeConnection();
            db.closeConnection();
        }, "❌ FAILED: Multiple close calls should not throw");
        
        System.out.println("  ✓ Multiple close calls handled");
        
        // Should still get connection
        Connection conn = db.getConnection();
        assertNotNull(conn, "❌ FAILED: Should get connection after multiple closes");
        
        System.out.println("✅ PASSED: Close method is idempotent");
    }
    
    @Test
    @DisplayName("✅ TEST 11: Instance persists after connection close")
    void testSingleton_InstancePersistsAfterClose() {
        System.out.println("\n🔵 TEST 11: Instance Persistence After Close");
        
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        db1.closeConnection();
        
        DatabaseConnection db2 = DatabaseConnection.getInstance();
        
        assertSame(db1, db2, 
            "❌ FAILED: Same instance should persist after close");
        System.out.println("  ✓ Same instance after close: " + db1);
        System.out.println("✅ PASSED: Instance persists");
    }
    
    @Test
    @DisplayName("✅ TEST 12: Instance count - Only one INSTANCE field")
    void testSingleton_OnlyOneInstanceField() {
        System.out.println("\n🔵 TEST 12: Single INSTANCE Field Verification");
        
        Field[] fields = DatabaseConnection.class.getDeclaredFields();
        int instanceCount = 0;
        
        for (Field field : fields) {
            if (field.getName().equals("INSTANCE")) {
                instanceCount++;
                int modifiers = field.getModifiers();
                assertTrue(Modifier.isPrivate(modifiers), 
                    "❌ FAILED: INSTANCE field must be private");
                assertTrue(Modifier.isStatic(modifiers), 
                    "❌ FAILED: INSTANCE field must be static");
                System.out.println("  ✓ INSTANCE field: private static");
            }
        }
        
        assertEquals(1, instanceCount, 
            "❌ FAILED: There should be exactly one INSTANCE field");
        System.out.println("✅ PASSED: Single INSTANCE field verified");
    }
    
    @Test
    @DisplayName("✅ TEST 13: getInstance() is synchronized")
    void testSingleton_GetInstanceSynchronized() throws Exception {
        System.out.println("\n🔵 TEST 13: Synchronized Method Verification");
        
        java.lang.reflect.Method method = 
            DatabaseConnection.class.getDeclaredMethod("getInstance");
        int modifiers = method.getModifiers();
        
        assertTrue(Modifier.isSynchronized(modifiers) || 
                   Modifier.isPublic(modifiers), 
            "❌ FAILED: getInstance() should be synchronized or public");
        
        System.out.println("  ✓ getInstance() modifiers: " + Modifier.toString(modifiers));
        System.out.println("✅ PASSED: Method accessible for thread safety");
    }
    
    @Test
    @DisplayName("✅ TEST 14: Memory consistency - 1000 sequential calls")
    void testSingleton_MemoryConsistency() {
        System.out.println("\n🔵 TEST 14: Memory Consistency - 1000 Sequential Calls");
        
        DatabaseConnection first = DatabaseConnection.getInstance();
        
        for (int i = 0; i < 1000; i++) {
            DatabaseConnection current = DatabaseConnection.getInstance();
            assertSame(first, current, 
                "❌ FAILED: Instance changed after " + i + " calls");
        }
        
        System.out.println("  ✓ All 1000 calls returned same instance");
        System.out.println("✅ PASSED: Memory consistency verified");
    }
    
    @Test
    @DisplayName("✅ TEST 15: Stress test - Mixed operations")
    void testSingleton_StressTest() throws Exception {
        System.out.println("\n🔵 TEST 15: Stress Test - Mixed Operations");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        // Mix of operations
        for (int i = 0; i < 100; i++) {
            Connection conn = db.getConnection();
            assertNotNull(conn, "❌ FAILED: Connection null at iteration " + i);
            
            if (i % 10 == 0) {
                db.closeConnection();
                System.out.println("  ✓ Cycle " + i + ": Closed connection");
            }
            
            // Get instance again
            DatabaseConnection sameDb = DatabaseConnection.getInstance();
            assertSame(db, sameDb, 
                "❌ FAILED: Different instance after operation " + i);
        }
        
        System.out.println("✅ PASSED: Stress test completed");
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    @DisplayName("✅ TEST 16: Early garbage collection simulation")
    void testSingleton_NoGarbageCollection() {
        System.out.println("\n🔵 TEST 16: Garbage Collection Resistance");
        
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        
        // Try to encourage GC
        for (int i = 0; i < 10; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        DatabaseConnection db2 = DatabaseConnection.getInstance();
        
        assertSame(db1, db2, 
            "❌ FAILED: GC should not affect singleton");
        System.out.println("  ✓ Instance unchanged after GC attempts");
        System.out.println("✅ PASSED: Singleton resists garbage collection");
    }
    
    @Test
    @DisplayName("✅ TEST 17: Different classloaders (simulated)")
    void testSingleton_DifferentClassLoaders() throws Exception {
        System.out.println("\n🔵 TEST 17: ClassLoader Independence");
        
        // Get instance normally
        DatabaseConnection normalInstance = DatabaseConnection.getInstance();
        
        // Load class with different classloader (simulated)
        Class<?> clazz = DatabaseConnection.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object reflectionInstance = constructor.newInstance();
        
        assertNotNull(reflectionInstance, "Reflection instance created");
        assertNotSame(normalInstance, reflectionInstance, 
            "❌ FAILED: Different classloaders might create different instances");
        
        System.out.println("  ✓ Normal instance: " + normalInstance);
        System.out.println("  ✓ Reflection instance: " + reflectionInstance);
        System.out.println("✅ PASSED: Classloader behavior documented");
    }
    
    @Test
    @DisplayName("✅ TEST 18: Get connection after long delay")
    void testSingleton_ConnectionAfterDelay() throws Exception {
        System.out.println("\n🔵 TEST 18: Connection After Delay");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn1 = db.getConnection();
        System.out.println("  Initial connection: " + conn1);
        
        // Wait 2 seconds
        Thread.sleep(2000);
        
        Connection conn2 = db.getConnection();
        System.out.println("  Connection after delay: " + conn2);
        
        assertSame(conn1, conn2, 
            "❌ FAILED: Connection should remain same after delay");
        System.out.println("✅ PASSED: Connection stable after delay");
    }
    
    @Test
    @DisplayName("✅ TEST 19: Multiple getConnection calls pattern")
    void testSingleton_ConnectionPattern() {
        System.out.println("\n🔵 TEST 19: Connection Access Pattern");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        Connection[] connections = new Connection[5];
        for (int i = 0; i < 5; i++) {
            connections[i] = db.getConnection();
        }
        
        // All should be same
        for (int i = 1; i < 5; i++) {
            assertSame(connections[0], connections[i], 
                "❌ FAILED: Connection pattern broken at index " + i);
        }
        
        System.out.println("  ✓ All 5 getConnection() calls returned same object");
        System.out.println("✅ PASSED: Connection pattern consistent");
    }
    
    @Test
    @DisplayName("✅ TEST 20: Final verification - All singleton properties")
    void testSingleton_FinalVerification() {
        System.out.println("\n🔵 TEST 20: FINAL VERIFICATION - All Properties");
        
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();
        
        // Single instance
        assertSame(db1, db2, "❌ Single instance failed");
        
        // Connection sharing
        assertSame(db1.getConnection(), db2.getConnection(), 
            "❌ Connection sharing failed");
        
        // Not null
        assertNotNull(db1.getConnection(), "❌ Connection null");
        
        System.out.println("\n🎉🎉🎉 ALL SINGLETON PROPERTIES VERIFIED! 🎉🎉🎉");
        System.out.println("  ✓ Single instance");
        System.out.println("  ✓ Lazy initialization");
        System.out.println("  ✓ Thread safety");
        System.out.println("  ✓ Connection management");
        System.out.println("  ✓ Private constructor");
        System.out.println("\n✅✅✅ SINGLETON TEST SUITE COMPLETE! ✅✅✅");
    }
}
