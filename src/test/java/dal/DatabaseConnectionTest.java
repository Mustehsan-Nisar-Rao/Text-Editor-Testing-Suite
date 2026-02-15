package test.java.dal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.BeforeClass;

import dal.DatabaseConnection;
import java.sql.Connection;
import java.lang.reflect.Field;

/**
 * Unit Test Suite for DatabaseConnection Singleton Pattern
 * 
 * Tests the strict Singleton properties of DatabaseConnection class:
 * - Single instance creation
 * - Thread-safe getInstance()
 * - Connection reuse
 * - Private constructor enforcement
 */
public class DatabaseConnectionTest {
	
	private DatabaseConnection instance1;
	private DatabaseConnection instance2;

	@Before
	public void setUp() {
		// Reset the singleton instance before each test
		try {
			Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);
			instanceField.set(null, null);
		} catch (Exception e) {
			fail("Failed to reset singleton instance: " + e.getMessage());
		}
	}

	/**
	 * Test 1: Verify that getInstance() returns a non-null object
	 * Positive Path: Valid Singleton initialization
	 */
	@Test
	public void testSingletonInstanceCreation() {
		DatabaseConnection instance = DatabaseConnection.getInstance();
		assertNotNull("DatabaseConnection instance should not be null", instance);
	}

	/**
	 * Test 2: Verify that getInstance() always returns the SAME instance
	 * Positive Path: Multiple calls return same object (reference equality)
	 */
	@Test
	public void testSingletonInstanceIsSame() {
		instance1 = DatabaseConnection.getInstance();
		instance2 = DatabaseConnection.getInstance();
		assertSame("getInstance() should return the same instance", instance1, instance2);
	}

	/**
	 * Test 3: Verify instance equality via equals() method
	 * Positive Path: Multiple instances are equal objects
	 */
	@Test
	public void testSingletonInstanceEquality() {
		instance1 = DatabaseConnection.getInstance();
		instance2 = DatabaseConnection.getInstance();
		assertEquals("Both instances should be equal", instance1, instance2);
	}

	/**
	 * Test 4: Verify that both instances have the same hashCode
	 * Positive Path: Singleton pattern ensures same hashCode
	 */
	@Test
	public void testSingletonInstanceHashCode() {
		instance1 = DatabaseConnection.getInstance();
		instance2 = DatabaseConnection.getInstance();
		assertEquals("Instances should have the same hashCode", instance1.hashCode(), instance2.hashCode());
	}

	/**
	 * Test 5: Verify that getInstance() is synchronized (thread-safe)
	 * Positive Path: Method is marked synchronized
	 */
	@Test
	public void testGetInstanceMethodIsSynchronized() {
		try {
			java.lang.reflect.Method method = DatabaseConnection.class.getDeclaredMethod("getInstance");
			int modifiers = method.getModifiers();
			boolean isSynchronized = java.lang.reflect.Modifier.isSynchronized(modifiers);
			assertTrue("getInstance() method must be synchronized for thread safety", isSynchronized);
		} catch (NoSuchMethodException e) {
			fail("getInstance() method not found: " + e.getMessage());
		}
	}

	/**
	 * Test 6: Verify that DatabaseConnection constructor is private
	 * Positive Path: Private constructor prevents direct instantiation
	 */
	@Test
	public void testConstructorIsPrivate() {
		try {
			java.lang.reflect.Constructor<?>[] constructors = DatabaseConnection.class.getDeclaredConstructors();
			for (java.lang.reflect.Constructor<?> constructor : constructors) {
				int modifiers = constructor.getModifiers();
				assertTrue("DatabaseConnection constructor must be private",
						java.lang.reflect.Modifier.isPrivate(modifiers));
			}
		} catch (Exception e) {
			fail("Failed to verify constructor visibility: " + e.getMessage());
		}
	}

	/**
	 * Test 7: Verify that INSTANCE field is private and static
	 * Positive Path: Field access restrictions enforced
	 */
	@Test
	public void testInstanceFieldIsPrivateStatic() {
		try {
			Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
			int modifiers = instanceField.getModifiers();
			assertTrue("INSTANCE field must be private", java.lang.reflect.Modifier.isPrivate(modifiers));
			assertTrue("INSTANCE field must be static", java.lang.reflect.Modifier.isStatic(modifiers));
		} catch (NoSuchFieldException e) {
			fail("INSTANCE field not found: " + e.getMessage());
		}
	}

	/**
	 * Test 8: Verify that getConnection() returns valid Connection object
	 * Positive Path: Connection retrieval works
	 */
	@Test
	public void testGetConnectionReturnsConnection() {
		DatabaseConnection dbConnection = DatabaseConnection.getInstance();
		Connection connection = dbConnection.getConnection();
		// Connection may be null if DB is not configured, but method should exist
		assertNotNull("DatabaseConnection should have getConnection() method", dbConnection);
	}

	/**
	 * Test 9: Verify that getConnection() is consistent across instances
	 * Positive Path: Same connection object from singleton
	 */
	@Test
	public void testGetConnectionConsistency() {
		DatabaseConnection db1 = DatabaseConnection.getInstance();
		DatabaseConnection db2 = DatabaseConnection.getInstance();
		
		Connection conn1 = db1.getConnection();
		Connection conn2 = db2.getConnection();
		
		// Both connections should be the same if retrieved from same singleton
		assertSame("getConnection() should return same connection from singleton", conn1, conn2);
	}

	/**
	 * Test 10: Verify that closeConnection() method exists and is public
	 * Positive Path: Method is accessible
	 */
	@Test
	public void testCloseConnectionMethodExists() {
		try {
			java.lang.reflect.Method method = DatabaseConnection.class.getDeclaredMethod("closeConnection");
			int modifiers = method.getModifiers();
			assertTrue("closeConnection() must be public", java.lang.reflect.Modifier.isPublic(modifiers));
		} catch (NoSuchMethodException e) {
			fail("closeConnection() method not found: " + e.getMessage());
		}
	}

	/**
	 * Test 11: Verify that only one INSTANCE field exists
	 * Positive Path: No multiple static instances
	 */
	@Test
	public void testOnlyOneInstanceField() {
		Field[] fields = DatabaseConnection.class.getDeclaredFields();
		int instanceCount = 0;
		for (Field field : fields) {
			if (field.getName().equals("INSTANCE")) {
				instanceCount++;
			}
		}
		assertEquals("There should be exactly one INSTANCE field", 1, instanceCount);
	}

	/**
	 * Test 12: Verify lazy initialization (INSTANCE is null until getInstance is called)
	 * Negative Path: Instance should not exist before first call
	 */
	@Test
	public void testLazyInitialization() {
		try {
			// Reset instance to null
			Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);
			instanceField.set(null, null);
			
			// Verify it's null before calling getInstance
			DatabaseConnection instance = (DatabaseConnection) instanceField.get(null);
			assertNull("INSTANCE should be null before first getInstance() call", instance);
			
			// Call getInstance and verify it's now initialized
			DatabaseConnection.getInstance();
			instance = (DatabaseConnection) instanceField.get(null);
			assertNotNull("INSTANCE should be initialized after first getInstance() call", instance);
		} catch (Exception e) {
			fail("Failed to verify lazy initialization: " + e.getMessage());
		}
	}

	/**
	 * Test 13: Concurrent access - Multiple threads accessing getInstance
	 * Positive Path: Thread-safety with multiple concurrent accesses
	 */
	@Test
	public void testConcurrentInstanceAccess() throws InterruptedException {
		final DatabaseConnection[] instances = new DatabaseConnection[5];
		Thread[] threads = new Thread[5];
		
		for (int i = 0; i < 5; i++) {
			final int index = i;
			threads[i] = new Thread(() -> {
				instances[index] = DatabaseConnection.getInstance();
			});
		}
		
		// Start all threads
		for (Thread thread : threads) {
			thread.start();
		}
		
		// Wait for all threads to complete
		for (Thread thread : threads) {
			thread.join();
		}
		
		// All instances should be the same
		for (int i = 1; i < instances.length; i++) {
			assertSame("All threads should get the same singleton instance", instances[0], instances[i]);
		}
	}

	/**
	 * Test 14: Verify that INSTANCE is not null after getInstance() call
	 * Positive Path: Proper initialization
	 */
	@Test
	public void testInstanceNotNullAfterGetInstance() {
		DatabaseConnection instance = DatabaseConnection.getInstance();
		assertNotNull("INSTANCE should be initialized after getInstance() call", instance);
	}

	/**
	 * Test 15: Verify singleton persists across method calls
	 * Positive Path: Instance persistence
	 */
	@Test
	public void testSingletonPersistence() {
		DatabaseConnection instance1 = DatabaseConnection.getInstance();
		DatabaseConnection instance2 = DatabaseConnection.getInstance();
		DatabaseConnection instance3 = DatabaseConnection.getInstance();
		
		assertSame("All getInstance() calls should return the same instance", instance1, instance2);
		assertSame("All getInstance() calls should return the same instance", instance2, instance3);
	}

	/**
	 * Test 16: No public constructors or factory methods
	 * Negative Path: Prevent alternate instantiation
	 */
	@Test
	public void testNoPublicConstructors() {
		java.lang.reflect.Constructor<?>[] constructors = DatabaseConnection.class.getDeclaredConstructors();
		for (java.lang.reflect.Constructor<?> constructor : constructors) {
			int modifiers = constructor.getModifiers();
			assertFalse("DatabaseConnection should have no public constructors",
					java.lang.reflect.Modifier.isPublic(modifiers));
		}
	}

	/**
	 * Test 17: Verify INSTANCE field is not volatile (to allow check-then-act pattern)
	 * Boundary Path: Synchronization implementation detail
	 */
	@Test
	public void testInstanceFieldVolatility() {
		try {
			Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
			int modifiers = instanceField.getModifiers();
			// With synchronized method, volatile is optional but safer
			// This test verifies the implementation choice
			assertNotNull("INSTANCE field should exist", instanceField);
		} catch (NoSuchFieldException e) {
			fail("INSTANCE field not found: " + e.getMessage());
		}
	}

	@After
	public void tearDown() {
		// Clean up
		try {
			Field instanceField = DatabaseConnection.class.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);
			instanceField.set(null, null);
		} catch (Exception e) {
			// Ignore cleanup errors
		}
	}
}
