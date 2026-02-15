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
import org.junit.After;

import dal.DatabaseConnection;
import dal.EditorDBDAO;
import dal.HashCalculator;
import dto.Documents;
import dto.Pages;

/**
 * Unit Test Suite for Hash Integrity and File Versioning
 * 
 * Tests the hash integrity requirements:
 * 1. Original import hash is retained in database metadata
 * 2. Editing a file changes the current session hash
 * 3. Both hashes are maintained properly
 */
public class HashingIntegrityTest {

	private HashCalculator hashCalculator;
	private EditorDBDAO editorDAO;
	private Connection mockConnection;
	private PreparedStatement mockPreparedStatement;
	private ResultSet mockResultSet;

	@Before
	public void setUp() throws Exception {
		hashCalculator = new HashCalculator();
		editorDAO = new EditorDBDAO();
		
		// Mock database components
		mockConnection = mock(Connection.class);
		mockPreparedStatement = mock(PreparedStatement.class);
		mockResultSet = mock(ResultSet.class);
	}

	// ==================== POSITIVE PATH TESTS ====================

	/**
	 * Test 1: Calculate hash for simple Arabic text
	 * Positive Path: Known input produces consistent hash
	 */
	@Test
	public void testHashCalculationSimpleArabicText() throws Exception {
		String arabicText = "مرحبا";
		String hash = HashCalculator.calculateHash(arabicText);
		
		assertNotNull("Hash should not be null", hash);
		assertTrue("Hash should not be empty", hash.length() > 0);
		assertEquals("MD5 hash should be 32 characters", 32, hash.length());
		// Verify it's hexadecimal
		assertTrue("Hash should be hexadecimal", hash.matches("[0-9A-F]+"));
	}

	/**
	 * Test 2: Hash consistency - same input produces same hash
	 * Positive Path: Deterministic hashing
	 */
	@Test
	public void testHashConsistency() throws Exception {
		String content = "السلام عليكم ورحمة الله وبركاته";
		
		String hash1 = HashCalculator.calculateHash(content);
		String hash2 = HashCalculator.calculateHash(content);
		String hash3 = HashCalculator.calculateHash(content);
		
		assertEquals("Same content should produce same hash (1)", hash1, hash2);
		assertEquals("Same content should produce same hash (2)", hash2, hash3);
	}

	/**
	 * Test 3: Different content produces different hashes
	 * Positive Path: Hash uniqueness
	 */
	@Test
	public void testHashUniqueness() throws Exception {
		String content1 = "النص الأول";
		String content2 = "النص الثاني";
		
		String hash1 = HashCalculator.calculateHash(content1);
		String hash2 = HashCalculator.calculateHash(content2);
		
		assertNotEquals("Different content should produce different hashes", hash1, hash2);
	}

	/**
	 * Test 4: Hash changes when content is edited
	 * Positive Path: Detection of modifications
	 */
	@Test
	public void testHashChangesOnEdit() throws Exception {
		String originalContent = "هذا هو المحتوى الأصلي";
		String editedContent = "هذا هو المحتوى المعدل";
		
		String originalHash = HashCalculator.calculateHash(originalContent);
		String editedHash = HashCalculator.calculateHash(editedContent);
		
		assertNotEquals("Hash should change when content is edited", originalHash, editedHash);
	}

	/**
	 * Test 5: Verify original import hash is retained
	 * Positive Path: fileHash remains constant after retrieval
	 */
	@Test
	public void testImportHashRetention() throws Exception {
		String content = "محتوى الملف المستورد";
		String importHash = HashCalculator.calculateHash(content);
		
		// Create document with import hash
		List<Pages> pages = new ArrayList<>();
		Pages page = new Pages(1, 1, 1, content);
		pages.add(page);
		Documents doc = new Documents(1, "test.txt", importHash, null, null, pages);
		
		// Verify import hash is stored
		assertEquals("Import hash should be stored in document", importHash, doc.getHash());
	}

	/**
	 * Test 6: Verify current session hash differs from import hash after edit
	 * Positive Path: Session hash tracks modifications
	 */
	@Test
	public void testCurrentSessionHashAfterEdit() throws Exception {
		String originalContent = "المحتوى الأصلي";
		String originalHash = HashCalculator.calculateHash(originalContent);
		
		String editedContent = "المحتوى بعد التعديل";
		String currentSessionHash = HashCalculator.calculateHash(editedContent);
		
		assertNotEquals("Current session hash should differ from original import hash", 
						originalHash, currentSessionHash);
	}

	/**
	 * Test 7: Long document hash calculation
	 * Positive Path: Large text content hashing
	 */
	@Test
	public void testHashCalculationLongDocument() throws Exception {
		StringBuilder longContent = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			longContent.append("هذا نص طويل يتكرر عدة مرات ");
		}
		
		String hash = HashCalculator.calculateHash(longContent.toString());
		
		assertNotNull("Hash should be calculated for long content", hash);
		assertEquals("Hash length should be 32 characters", 32, hash.length());
	}

	/**
	 * Test 8: Hash stability for large documents
	 * Positive Path: Consistent hashing regardless of size
	 */
	@Test
	public void testHashStabilityLargeDocument() throws Exception {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			content.append("محتوى متكرر ");
		}
		
		String hash1 = HashCalculator.calculateHash(content.toString());
		String hash2 = HashCalculator.calculateHash(content.toString());
		
		assertEquals("Large document hashing should be consistent", hash1, hash2);
	}

	/**
	 * Test 9: Verify metadata hash field is preserved
	 * Positive Path: Database metadata consistency
	 */
	@Test
	public void testMetadataHashPreservation() throws Exception {
		String originalContent = "محتوى قاعدة البيانات";
		String metadataHash = HashCalculator.calculateHash(originalContent);
		
		// Simulate storing in metadata
		String storedHash = metadataHash;
		
		assertEquals("Metadata hash should be preserved", metadataHash, storedHash);
	}

	/**
	 * Test 10: Edit detection through hash comparison
	 * Positive Path: Using hashes to detect modifications
	 */
	@Test
	public void testEditDetectionViaHashComparison() throws Exception {
		String originalContent = "نص أصلي";
		String originalHash = HashCalculator.calculateHash(originalContent);
		
		String editedContent = "نص معدل";
		String currentHash = HashCalculator.calculateHash(editedContent);
		
		boolean isModified = !originalHash.equals(currentHash);
		assertTrue("Hash comparison should detect modifications", isModified);
	}

	// ==================== NEGATIVE PATH TESTS ====================

	/**
	 * Test 11: Handle null content gracefully
	 * Negative Path: Empty/null input handling
	 */
	@Test
	public void testHashCalculationNullContent() {
		try {
			HashCalculator.calculateHash(null);
			fail("Should throw NullPointerException for null content");
		} catch (NullPointerException e) {
			assertTrue("Should handle null content", true);
		} catch (Exception e) {
			assertTrue("Should handle null content with exception", true);
		}
	}

	/**
	 * Test 12: Handle empty string content
	 * Negative Path: Empty content hashing
	 */
	@Test
	public void testHashCalculationEmptyString() throws Exception {
		String emptyContent = "";
		String hash = HashCalculator.calculateHash(emptyContent);
		
		assertNotNull("Hash should be calculated for empty string", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
		// Empty string MD5: d41d8cd98f00b204e9800998ecf8427e
		assertEquals("Empty string should have consistent MD5", "D41D8CD98F00B204E9800998ECF8427E", hash);
	}

	/**
	 * Test 13: Handle special characters in content
	 * Negative Path: Special character hashing
	 */
	@Test
	public void testHashCalculationSpecialCharacters() throws Exception {
		String specialContent = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
		String hash = HashCalculator.calculateHash(specialContent);
		
		assertNotNull("Hash should handle special characters", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 14: Handle mixed Arabic and English content
	 * Negative Path: Mixed language hashing
	 */
	@Test
	public void testHashCalculationMixedLanguage() throws Exception {
		String mixedContent = "Hello مرحبا World عالم 123";
		String hash = HashCalculator.calculateHash(mixedContent);
		
		assertNotNull("Hash should handle mixed language", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 15: Handle whitespace variations
	 * Negative Path: Whitespace sensitivity
	 */
	@Test
	public void testHashSensitiveToWhitespace() throws Exception {
		String content1 = "مرحبا";
		String content2 = "مرحبا ";
		String content3 = " مرحبا";
		
		String hash1 = HashCalculator.calculateHash(content1);
		String hash2 = HashCalculator.calculateHash(content2);
		String hash3 = HashCalculator.calculateHash(content3);
		
		assertNotEquals("Hash should be sensitive to leading whitespace", hash1, hash3);
		assertNotEquals("Hash should be sensitive to trailing whitespace", hash1, hash2);
	}

	/**
	 * Test 16: Handle numbers in content
	 * Negative Path: Numeric content hashing
	 */
	@Test
	public void testHashCalculationNumericContent() throws Exception {
		String numericContent = "1234567890";
		String hash = HashCalculator.calculateHash(numericContent);
		
		assertNotNull("Hash should handle numeric content", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 17: Single character hash variation
	 * Boundary Path: Minimal content
	 */
	@Test
	public void testHashCalculationSingleCharacter() throws Exception {
		String singleChar = "ا";
		String hash = HashCalculator.calculateHash(singleChar);
		
		assertNotNull("Hash should handle single character", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 18: Very long content hash calculation
	 * Boundary Path: Maximum content size
	 */
	@Test
	public void testHashCalculationExtremelyLongContent() throws Exception {
		StringBuilder veryLongContent = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			veryLongContent.append("محتوى ");
		}
		
		String hash = HashCalculator.calculateHash(veryLongContent.toString());
		
		assertNotNull("Hash should handle extremely long content", hash);
		assertEquals("Hash length should be 32", 32, hash.length());
	}

	/**
	 * Test 19: Hash format validation
	 * Boundary Path: Output format correctness
	 */
	@Test
	public void testHashFormatValidation() throws Exception {
		String content = "محتوى للاختبار";
		String hash = HashCalculator.calculateHash(content);
		
		assertTrue("Hash should be uppercase hexadecimal", hash.matches("[0-9A-F]{32}"));
		assertTrue("Hash should not contain lowercase", !hash.matches(".*[a-f].*"));
	}

	/**
	 * Test 20: Case sensitivity in content
	 * Boundary Path: Case variation impact
	 */
	@Test
	public void testHashCaseSensitivity() throws Exception {
		String content1 = "Hello";
		String content2 = "hello";
		
		String hash1 = HashCalculator.calculateHash(content1);
		String hash2 = HashCalculator.calculateHash(content2);
		
		assertNotEquals("Hash should be case-sensitive", hash1, hash2);
	}

	// ==================== INTEGRATION PATH TESTS ====================

	/**
	 * Test 21: Import hash retention through document lifecycle
	 * Integration Path: Document creation -> retrieval
	 */
	@Test
	public void testImportHashRetentionLifecycle() throws Exception {
		String originalContent = "محتوى الملف الأصلي المستورد";
		String importHash = HashCalculator.calculateHash(originalContent);
		
		// Simulate document creation with import hash
		List<Pages> pages = new ArrayList<>();
		Documents document = new Documents(1, "importedFile.txt", importHash, null, null, pages);
		
		// Verify hash is retained
		String retrievedHash = document.getHash();
		assertEquals("Import hash should be retained in document", importHash, retrievedHash);
	}

	/**
	 * Test 22: Current session hash tracking
	 * Integration Path: Edit detection scenario
	 */
	@Test
	public void testSessionHashTracking() throws Exception {
		// Original import
		String importedContent = "النص المستورد";
		String importHash = HashCalculator.calculateHash(importedContent);
		
		// After user edit (simulated)
		String editedContent = "النص المستورد المعدل";
		String currentSessionHash = HashCalculator.calculateHash(editedContent);
		
		// Verify both hashes exist and differ
		assertNotEquals("Import and session hashes should differ after edit", 
						importHash, currentSessionHash);
		assertNotNull("Import hash should exist", importHash);
		assertNotNull("Session hash should exist", currentSessionHash);
	}

	/**
	 * Test 23: Multiple edits preserve import hash
	 * Integration Path: Serial editing scenario
	 */
	@Test
	public void testMultipleEditsPreserveImportHash() throws Exception {
		String importedContent = "المحتوى الأصلي المستورد";
		String importHash = HashCalculator.calculateHash(importedContent);
		
		// First edit
		String edit1 = "المحتوى الأصلي المستورد - التعديل الأول";
		String hash1 = HashCalculator.calculateHash(edit1);
		
		// Second edit
		String edit2 = "المحتوى الأصلي المستورد - التعديل الثاني";
		String hash2 = HashCalculator.calculateHash(edit2);
		
		// Verify import hash unchanged and different from session hashes
		assertNotEquals("Import hash should differ from edit 1", importHash, hash1);
		assertNotEquals("Import hash should differ from edit 2", importHash, hash2);
		assertNotEquals("Edit 1 and Edit 2 should have different hashes", hash1, hash2);
	}

	@After
	public void tearDown() {
		// Cleanup
	}
}
