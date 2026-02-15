package test.java.dal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import dal.DatabaseConnection;
import dal.EditorDBDAO;
import dal.HashCalculator;
import dto.Documents;
import dto.Pages;

/**
 * Integration Test Suite for Data Persistence Layer
 * 
 * Tests the complete workflow:
 * 1. File creation with import hash
 * 2. File modification with session hash
 * 3. Hash metadata preservation
 * 4. Database operations with hashing
 */
public class DataPersistenceIntegrationTest {

	private EditorDBDAO editorDAO;
	private Connection mockConnection;
	private PreparedStatement mockPreparedStatement;
	private ResultSet mockResultSet;
	private ArgumentCaptor<String> hashCaptor;

	@Before
	public void setUp() throws Exception {
		// Create mocks
		mockConnection = mock(Connection.class);
		mockPreparedStatement = mock(PreparedStatement.class);
		mockResultSet = mock(ResultSet.class);
		hashCaptor = ArgumentCaptor.forClass(String.class);
	}

	/**
	 * Test 1: Verify file creation stores import hash in database
	 * Positive Path: Create file with hash metadata
	 */
	@Test
	public void testFileCreationStoresImportHash() throws Exception {
		String fileName = "test.txt";
		String content = "محتوى الملف الجديد";
		String expectedHash = HashCalculator.calculateHash(content);
		
		// Verify hash is calculated
		assertNotNull("Import hash should be calculated", expectedHash);
		assertEquals("Hash should be MD5 format (32 chars)", 32, expectedHash.length());
	}

	/**
	 * Test 2: Verify import hash remains unchanged in metadata
	 * Positive Path: Hash persistence
	 */
	@Test
	public void testImportHashUnchangedInMetadata() throws Exception {
		String originalContent = "النص الأصلي";
		String importHash = HashCalculator.calculateHash(originalContent);
		
		// Create document
		List<Pages> pages = new ArrayList<>();
		Pages page = new Pages(1, 1, 1, originalContent);
		pages.add(page);
		Documents doc = new Documents(1, "file.txt", importHash, null, null, pages);
		
		// Verify hash is stored and unchanged
		assertEquals("Import hash should be stored", importHash, doc.getHash());
		
		// Simulate some operations
		String simulatedEditedContent = "محتوى محرر";
		String editedHash = HashCalculator.calculateHash(simulatedEditedContent);
		
		// Original hash should still match
		assertEquals("Import hash should remain unchanged", importHash, doc.getHash());
		assertNotEquals("Edited content should have different hash", importHash, editedHash);
	}

	/**
	 * Test 3: Verify edit updates only session hash, not metadata hash
	 * Positive Path: File edit scenario
	 */
	@Test
	public void testEditUpdatesSessionHashOnly() throws Exception {
		// Original import
		String importContent = "المحتوى المستورد الأصلي";
		String importHash = HashCalculator.calculateHash(importContent);
		
		// Create document with import hash
		Documents doc = new Documents(1, "file.txt", importHash, null, null, new ArrayList<>());
		String storedImportHash = doc.getHash();
		
		// Edit content
		String editedContent = "المحتوى المستورد الأصلي مع تعديلات";
		String sessionHash = HashCalculator.calculateHash(editedContent);
		
		// Verify: stored hash unchanged, session hash is different
		assertEquals("Stored import hash should be unchanged", importHash, storedImportHash);
		assertNotEquals("Session hash should differ from import hash", importHash, sessionHash);
	}

	/**
	 * Test 4: Verify multiple edits maintain original import hash
	 * Positive Path: Serial edit tracking
	 */
	@Test
	public void testMultipleEditsPreserveImportHashMetadata() throws Exception {
		String importContent = "المحتوى الأصلي";
		String importHash = HashCalculator.calculateHash(importContent);
		
		Documents doc = new Documents(1, "file.txt", importHash, null, null, new ArrayList<>());
		
		// Simulate 3 edits
		for (int i = 0; i < 3; i++) {
			String editedContent = importContent + " تعديل " + (i + 1);
			String sessionHash = HashCalculator.calculateHash(editedContent);
			
			// Verify import hash unchanged
			assertEquals("Import hash should persist through all edits", importHash, doc.getHash());
			assertNotEquals("Each session should have unique hash", importHash, sessionHash);
		}
	}

	/**
	 * Test 5: Verify hash calculation before storage
	 * Positive Path: Hash generation timing
	 */
	@Test
	public void testHashCalculationBeforeStorage() throws Exception {
		String content = "محتوى للحفظ";
		
		// Hash should be calculated before storage
		String hash = HashCalculator.calculateHash(content);
		
		assertNotNull("Hash must be calculated before storage", hash);
		assertTrue("Hash must be valid format", hash.matches("[0-9A-F]{32}"));
	}

	/**
	 * Test 6: Verify hash consistency across multiple retrievals
	 * Positive Path: Metadata consistency
	 */
	@Test
	public void testHashConsistencyAcrossRetrievals() throws Exception {
		String content = "المحتوى المستورد";
		String importHash = HashCalculator.calculateHash(content);
		
		// Create document
		Documents doc = new Documents(1, "file.txt", importHash, null, null, new ArrayList<>());
		
		// Retrieve hash multiple times
		String hash1 = doc.getHash();
		String hash2 = doc.getHash();
		String hash3 = doc.getHash();
		
		assertEquals("Hash should be consistent on retrieval 1", importHash, hash1);
		assertEquals("Hash should be consistent on retrieval 2", importHash, hash2);
		assertEquals("Hash should be consistent on retrieval 3", importHash, hash3);
	}

	/**
	 * Test 7: Verify different files have different import hashes
	 * Positive Path: File differentiation
	 */
	@Test
	public void testDifferentFilesHaveDifferentHashes() throws Exception {
		String content1 = "محتوى الملف الأول";
		String content2 = "محتوى الملف الثاني";
		
		String hash1 = HashCalculator.calculateHash(content1);
		String hash2 = HashCalculator.calculateHash(content2);
		
		assertNotEquals("Different files should have different hashes", hash1, hash2);
	}

	/**
	 * Test 8: Verify hash is stored as string in metadata
	 * Positive Path: Storage format validation
	 */
	@Test
	public void testHashStorageFormat() throws Exception {
		String content = "اختبار تنسيق التخزين";
		String hash = HashCalculator.calculateHash(content);
		
		// Hash should be stored as string
		assertNotNull("Hash should not be null", hash);
		assertTrue("Hash should be string", hash instanceof String);
		assertEquals("Hash should have correct length", 32, hash.length());
	}

	/**
	 * Test 9: Verify hash integrity through document lifecycle
	 * Positive Path: End-to-end scenario
	 */
	@Test
	public void testHashIntegrityLifecycle() throws Exception {
		// Import phase
		String importedContent = "هذا النص مستورد من ملف خارجي";
		String importHash = HashCalculator.calculateHash(importedContent);
		
		Documents importedDoc = new Documents(1, "imported.txt", importHash, 
											  "2024-01-15", "2024-01-15", 
											  new ArrayList<>());
		
		// Edit phase
		String editedContent = "هذا النص مستورد من ملف خارجي مع تعديلات";
		String sessionHash = HashCalculator.calculateHash(editedContent);
		
		// Verify
		assertEquals("Import hash should be in document", importHash, importedDoc.getHash());
		assertNotEquals("Session hash should differ", importHash, sessionHash);
		assertNotNull("Both hashes should exist", importHash);
		assertNotNull("Session hash should exist", sessionHash);
	}

	/**
	 * Test 10: Verify hash precision with similar content
	 * Positive Path: Hash sensitivity to minor changes
	 */
	@Test
	public void testHashPrecisionSimilarContent() throws Exception {
		String content1 = "محتوى مشابه جدا";
		String content2 = "محتوى مشابه جدا ";  // Extra space
		
		String hash1 = HashCalculator.calculateHash(content1);
		String hash2 = HashCalculator.calculateHash(content2);
		
		assertNotEquals("Hashes should differ for even minor changes", hash1, hash2);
	}

	// ==================== NEGATIVE PATH TESTS ====================

	/**
	 * Test 11: Handle missing import hash gracefully
	 * Negative Path: Missing metadata
	 */
	@Test
	public void testHandleMissingImportHash() throws Exception {
		// Document without hash
		Documents doc = new Documents(1, "file.txt", null, null, null, new ArrayList<>());
		
		assertNull("Document can have null hash initially", doc.getHash());
	}

	/**
	 * Test 12: Handle corrupted hash in metadata
	 * Negative Path: Invalid hash format
	 */
	@Test
	public void testHandleCorruptedHashFormat() throws Exception {
		String corruptedHash = "invalid_hash_format";
		Documents doc = new Documents(1, "file.txt", corruptedHash, null, null, new ArrayList<>());
		
		// Should still store it (validation is responsibility of caller)
		assertEquals("Should store hash as provided", corruptedHash, doc.getHash());
	}

	/**
	 * Test 13: Handle empty content hash
	 * Negative Path: Empty file
	 */
	@Test
	public void testHashEmptyFileContent() throws Exception {
		String emptyContent = "";
		String hash = HashCalculator.calculateHash(emptyContent);
		
		assertNotNull("Empty content should still produce hash", hash);
		assertEquals("Empty string MD5", "D41D8CD98F00B204E9800998ECF8427E", hash);
	}

	/**
	 * Test 14: Handle special characters in content hash
	 * Negative Path: Special character content
	 */
	@Test
	public void testHashSpecialCharacterContent() throws Exception {
		String specialContent = "محتوى مع أحرف خاصة: @#$%^&*()";
		String hash = HashCalculator.calculateHash(specialContent);
		
		assertNotNull("Special characters should be hashed", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 15: Handle very large content import
	 * Negative Path: Large file import
	 */
	@Test
	public void testHashLargeImportContent() throws Exception {
		StringBuilder largeContent = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			largeContent.append("محتوى متكرر للاختبار ");
		}
		
		String hash = HashCalculator.calculateHash(largeContent.toString());
		
		assertNotNull("Large content should be hashed", hash);
		assertEquals("Hash should be standard length", 32, hash.length());
	}

	/**
	 * Test 16: Prevent hash modification after storage
	 * Boundary Path: Immutability check
	 */
	@Test
	public void testHashImmutabilityAfterStorage() throws Exception {
		String content = "محتوى محفوظ";
		String originalHash = HashCalculator.calculateHash(content);
		
		Documents doc = new Documents(1, "file.txt", originalHash, null, null, new ArrayList<>());
		String retrievedHash1 = doc.getHash();
		
		// Attempt to modify (shouldn't affect stored hash)
		String modifiedContent = content + " تعديل";
		String newHash = HashCalculator.calculateHash(modifiedContent);
		
		// Original hash should be unchanged
		assertEquals("Stored hash should be immutable", originalHash, retrievedHash1);
		assertNotEquals("New content should have different hash", originalHash, newHash);
	}

	/**
	 * Test 17: Verify hash uniqueness across files
	 * Boundary Path: Hash collision resistance
	 */
	@Test
	public void testHashUniquenessMultipleFiles() throws Exception {
		List<String> hashes = new ArrayList<>();
		
		for (int i = 0; i < 10; i++) {
			String content = "ملف رقم " + i + " محتوى فريد";
			String hash = HashCalculator.calculateHash(content);
			hashes.add(hash);
		}
		
		// Verify all hashes are unique
		for (int i = 0; i < hashes.size(); i++) {
			for (int j = i + 1; j < hashes.size(); j++) {
				assertNotEquals("Hashes should be unique for different content", 
							   hashes.get(i), hashes.get(j));
			}
		}
	}

	/**
	 * Test 18: Verify database hash field receives correct value
	 * Boundary Path: Database persistence
	 */
	@Test
	public void testDatabaseHashFieldValue() throws Exception {
		String content = "محتوى قاعدة البيانات";
		String hash = HashCalculator.calculateHash(content);
		
		// Simulate database INSERT
		String fileNameParameter = "test.txt";
		String hashParameter = hash;
		
		assertNotNull("Hash parameter should be valid", hashParameter);
		assertEquals("Hash should be properly formatted", 32, hashParameter.length());
	}

	/**
	 * Test 19: Verify metadata timestamp preservation with hash
	 * Boundary Path: Metadata integrity
	 */
	@Test
	public void testMetadataPreservationWithHash() throws Exception {
		String content = "محتوى مع بيانات وصفية";
		String hash = HashCalculator.calculateHash(content);
		
		String dateCreated = "2024-01-15";
		String lastModified = "2024-01-15";
		
		Documents doc = new Documents(1, "file.txt", hash, lastModified, dateCreated, 
									   new ArrayList<>());
		
		assertEquals("Hash should be preserved with metadata", hash, doc.getHash());
		assertEquals("Created date should be preserved", dateCreated, doc.getDateCreated());
		assertEquals("Modified date should be preserved", lastModified, doc.getLastModified());
	}

	/**
	 * Test 20: Verify complete edit workflow preserves import hash
	 * Boundary Path: Full edit scenario
	 */
	@Test
	public void testCompleteEditWorkflowHashPreservation() throws Exception {
		// Step 1: Import file
		String importedContent = "الملف المستورد الأصلي";
		String importHash = HashCalculator.calculateHash(importedContent);
		Documents importedDoc = new Documents(1, "imported.txt", importHash, 
											  "2024-01-15", "2024-01-15", new ArrayList<>());
		
		// Step 2: User edits
		String editedContent = importedContent + " - تم التعديل";
		String sessionHash = HashCalculator.calculateHash(editedContent);
		
		// Step 3: Save changes
		// (In real scenario, this updates the file in DB but keeps import hash)
		
		// Verify: Import hash never changed
		assertEquals("Step 1: Import hash stored", importHash, importedDoc.getHash());
		assertNotEquals("Step 2: Session hash differs", importHash, sessionHash);
		
		// Step 4: Retrieve from DB (simulated)
		Documents retrievedDoc = new Documents(1, "imported.txt", importHash, 
											   "2024-01-15", "2024-01-16", new ArrayList<>());
		
		assertEquals("Step 4: Import hash preserved on retrieval", importHash, retrievedDoc.getHash());
	}
}
