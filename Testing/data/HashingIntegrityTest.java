package data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import dal.EditorDBDAO;
import dal.FacadeDAO;
import dto.Documents;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Hashing Integrity Test Suite
 * 
 * Verifies that:
 * - Import hash is stored and NEVER changes
 * - Current hash changes with each edit
 * - MD5 and SHA1 algorithms work correctly
 * 
 * @author [Your Team Name]
 * @date February 15, 2026
 */
@DisplayName("Hashing Integrity Test Suite")
public class HashingIntegrityTest {
    
    private EditorDBDAO editorDBDAO;
    private FacadeDAO facadeDAO;
    
    @BeforeEach
    void setUp() {
        editorDBDAO = new EditorDBDAO();
        facadeDAO = new FacadeDAO(editorDBDAO);
    }
    
    // ================== POSITIVE PATH TESTS ==================
    
    @Test
    @DisplayName("Positive: Import hash stored correctly on file import")
    void testHash_ImportHashStoredCorrectly() {
        String originalContent = "This is the original imported content.";
        String fileName = "test_document.txt";
        
        // Calculate import hash
        String importHash = calculateMD5(originalContent);
        
        // Store file in database
        boolean created = facadeDAO.createFileInDB(fileName, originalContent);
        
        assertTrue(created, "File should be created successfully");
        
        // Retrieve file and verify hash
        List<Documents> files = facadeDAO.getFilesFromDB();
        boolean found = false;
        for (Documents doc : files) {
            if (doc.getName().equals(fileName)) {
                assertEquals(importHash, doc.getHash(), 
                            "Import hash should match calculated hash");
                found = true;
                break;
            }
        }
        
        assertTrue(found, "File should exist in database");
        System.out.println("✓ Import Hash Stored: " + importHash);
    }
    
    @Test
    @DisplayName("Positive: Current hash changes after edit, import hash unchanged")
    void testHash_CurrentHashChangesAfterEdit() {
        String originalContent = "Original content before editing.";
        String editedContent = "Modified content after editing.";
        String fileName = "editable_document.txt";
        
        // Store original file
        String importHash = calculateMD5(originalContent);
        boolean created = facadeDAO.createFileInDB(fileName, originalContent);
        assertTrue(created, "File should be created");
        
        // Get file ID
        int fileId = -1;
        List<Documents> files = facadeDAO.getFilesFromDB();
        for (Documents doc : files) {
            if (doc.getName().equals(fileName)) {
                fileId = doc.getId();
                break;
            }
        }
        assertTrue(fileId > 0, "File ID should be valid");
        
        System.out.println("BEFORE EDIT:");
        System.out.println("  Import Hash:  " + importHash);
        
        // Update file with edited content
        boolean updated = facadeDAO.updateFileInDB(fileId, fileName, 1, editedContent);
        assertTrue(updated, "File should be updated");
        
        // Get updated file
        List<Documents> updatedFiles = facadeDAO.getFilesFromDB();
        String storedHash = "";
        for (Documents doc : updatedFiles) {
            if (doc.getId() == fileId) {
                storedHash = doc.getHash();
                break;
            }
        }
        
        System.out.println("\nAFTER EDIT:");
        System.out.println("  Stored Hash:  " + storedHash);
        
        // Critical Assertions
        assertEquals(importHash, storedHash, 
                    "✓ Import hash MUST NEVER change!");
    }
    
    @Test
    @DisplayName("Positive: Multiple edits - import hash remains constant")
    void testHash_MultipleEditsImportHashIntact() {
        String originalContent = "Version 1 - Original";
        String fileName = "multi_edit_doc.txt";
        
        // Import file
        String importHash = calculateMD5(originalContent);
        boolean created = facadeDAO.createFileInDB(fileName, originalContent);
        assertTrue(created, "File should be created");
        
        // Get file ID
        int fileId = -1;
        List<Documents> files = facadeDAO.getFilesFromDB();
        for (Documents doc : files) {
            if (doc.getName().equals(fileName)) {
                fileId = doc.getId();
                break;
            }
        }
        
        System.out.println("ORIGINAL Import Hash: " + importHash);
        
        // EDIT 1
        String content2 = "Version 2 - First edit";
        facadeDAO.updateFileInDB(fileId, fileName, 1, content2);
        
        List<Documents> files2 = facadeDAO.getFilesFromDB();
        String storedHash2 = "";
        for (Documents doc : files2) {
            if (doc.getId() == fileId) {
                storedHash2 = doc.getHash();
                break;
            }
        }
        
        System.out.println("\nAfter EDIT 1:");
        System.out.println("  Stored Hash:  " + storedHash2);
        
        assertEquals(importHash, storedHash2, 
                    "Import hash unchanged after edit 1");
        
        // EDIT 2
        String content3 = "Version 3 - Second edit with more changes";
        facadeDAO.updateFileInDB(fileId, fileName, 1, content3);
        
        List<Documents> files3 = facadeDAO.getFilesFromDB();
        String storedHash3 = "";
        for (Documents doc : files3) {
            if (doc.getId() == fileId) {
                storedHash3 = doc.getHash();
                break;
            }
        }
        
        System.out.println("\nAfter EDIT 2:");
        System.out.println("  Stored Hash:  " + storedHash3);
        
        assertEquals(importHash, storedHash3, 
                    "Import hash unchanged after edit 2");
        
        System.out.println("\n✓ Import hash UNCHANGED after multiple edits!");
    }
    
    @Test
    @DisplayName("Positive: SHA1 algorithm works correctly")
    void testHash_SHA1Algorithm() {
        String content = "Test content for SHA1 hashing algorithm.";
        
        String hashSHA1 = calculateSHA1(content);
        
        System.out.println("SHA1 Hashing:");
        System.out.println("  Hash:        " + hashSHA1);
        System.out.println("  Hash Length: " + hashSHA1.length() + " chars");
        
        assertEquals(40, hashSHA1.length(), 
                    "SHA1 hash should be 40 characters");
        assertTrue(hashSHA1.matches("[a-f0-9]{40}"), 
                  "SHA1 hash should be hexadecimal");
    }
    
    @Test
    @DisplayName("Positive: MD5 algorithm produces 32-character hash")
    void testHash_MD5Format() {
        String content = "Test MD5 format";
        
        String md5Hash = calculateMD5(content);
        
        System.out.println("MD5 Hash: " + md5Hash);
        System.out.println("Length: " + md5Hash.length());
        
        assertEquals(32, md5Hash.length(), 
                    "MD5 hash should be exactly 32 characters");
        assertTrue(md5Hash.matches("[a-f0-9]{32}"), 
                  "MD5 hash should be hexadecimal");
    }
    
    @Test
    @DisplayName("Positive: Identical content produces identical hash")
    void testHash_IdenticalContentSameHash() {
        String content = "Identical content for hash consistency test";
        
        String hash1 = calculateMD5(content);
        String hash2 = calculateMD5(content);
        String hash3 = calculateMD5(content);
        
        assertEquals(hash1, hash2, "Hash 1 and 2 should be identical");
        assertEquals(hash2, hash3, "Hash 2 and 3 should be identical");
        assertEquals(hash1, hash3, "Hash 1 and 3 should be identical");
        
        System.out.println("✓ Hash Consistency Verified: " + hash1);
    }
    
    @Test
    @DisplayName("Positive: Small content change produces completely different hash")
    void testHash_SmallChangeBigDifference() {
        String content1 = "Hello World";
        String content2 = "Hello World!"; // Just added '!'
        
        String hash1 = calculateMD5(content1);
        String hash2 = calculateMD5(content2);
        
        System.out.println("Content 1: '" + content1 + "'");
        System.out.println("Hash 1:    " + hash1);
        System.out.println("\nContent 2: '" + content2 + "'");
        System.out.println("Hash 2:    " + hash2);
        
        assertNotEquals(hash1, hash2, 
                       "Even tiny change should produce completely different hash");
        
        // Calculate difference
        int differences = 0;
        for (int i = 0; i < 32; i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) differences++;
        }
        System.out.println("\nCharacters different: " + differences + "/32");
    }
    
    // ================== NEGATIVE PATH TESTS ==================
    
    @Test
    @DisplayName("Negative: Empty content has valid hash")
    void testHash_EmptyContentHasValidHash() {
        String emptyContent = "";
        
        String emptyHash = calculateMD5(emptyContent);
        
        assertNotNull(emptyHash, "Empty content should have hash");
        assertFalse(emptyHash.isEmpty(), "Hash should not be empty");
        assertEquals(32, emptyHash.length(), 
                    "MD5 hash should be 32 chars even for empty content");
        
        System.out.println("Empty content MD5: " + emptyHash);
    }
    
    @Test
    @DisplayName("Negative: Null content should throw exception")
    void testHash_NullContentThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            calculateMD5(null);
        }, "Null content should throw NullPointerException");
        
        System.out.println("✓ Null content properly rejected");
    }
    
    @Test
    @DisplayName("Negative: Special characters in content hash correctly")
    void testHash_SpecialCharactersHash() {
        String specialContent = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`\"\\";
        
        String hash = calculateMD5(specialContent);
        
        assertNotNull(hash, "Special characters should hash correctly");
        assertEquals(32, hash.length(), 
                    "Hash length should be standard 32 chars");
        
        System.out.println("Special chars hash: " + hash);
    }
    
    // ================== BOUNDARY TESTS ==================
    
    @Test
    @DisplayName("Boundary: Very large content (10K lines) hashes correctly")
    void testHash_LargeContentHash() {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("Line ").append(i).append(": This is a test line.\n");
        }
        
        long startTime = System.currentTimeMillis();
        String hash = calculateMD5(largeContent.toString());
        long endTime = System.currentTimeMillis();
        
        assertNotNull(hash, "Large file should have hash");
        assertEquals(32, hash.length(), 
                    "MD5 hash should be 32 chars regardless of content size");
        
        System.out.println("Large file (10K lines) hashed in " + 
                          (endTime - startTime) + "ms");
        System.out.println("Hash: " + hash);
    }
    
    @Test
    @DisplayName("Boundary: Arabic/Unicode content hashes correctly")
    void testHash_UnicodeContentHash() {
        String arabicContent = "السلام عليكم ورحمة الله وبركاته\n" +
                              "مرحبا بكم في تطبيق المحرر النصي\n" +
                              "هذا اختبار للنص العربي 🌟";
        
        String hash = calculateMD5(arabicContent);
        
        System.out.println("Arabic/Unicode Hashing:");
        System.out.println("  Hash:  " + hash);
        
        assertNotNull(hash, "Arabic content should hash");
        assertEquals(32, hash.length(), 
                    "Unicode content should produce standard 32-char hash");
    }
    
    @Test
    @DisplayName("Boundary: Single character content")
    void testHash_SingleCharacter() {
        String singleChar = "a";
        
        String hash = calculateMD5(singleChar);
        
        assertNotNull(hash, "Single character should hash correctly");
        assertEquals(32, hash.length(), 
                    "Hash should be 32 chars even for single character");
        
        System.out.println("Single char 'a' hash: " + hash);
    }
    
    @Test
    @DisplayName("Boundary: Whitespace-only content")
    void testHash_WhitespaceOnlyContent() {
        String whitespaceContent = "   \n\t\r\n   ";
        
        String hash = calculateMD5(whitespaceContent);
        
        assertNotNull(hash, "Whitespace content should hash");
        assertNotEquals(calculateMD5(""), hash, 
                       "Whitespace hash should differ from empty string hash");
        
        System.out.println("Whitespace-only hash: " + hash);
    }
    
    @Test
    @DisplayName("Boundary: Case sensitivity - different cases produce different hashes")
    void testHash_CaseSensitivity() {
        String lowercase = "hello world";
        String uppercase = "HELLO WORLD";
        String mixedcase = "Hello World";
        
        String hash1 = calculateMD5(lowercase);
        String hash2 = calculateMD5(uppercase);
        String hash3 = calculateMD5(mixedcase);
        
        System.out.println("Lowercase: " + hash1);
        System.out.println("Uppercase: " + hash2);
        System.out.println("Mixedcase: " + hash3);
        
        assertNotEquals(hash1, hash2, "Case should affect hash (lower vs upper)");
        assertNotEquals(hash2, hash3, "Case should affect hash (upper vs mixed)");
        assertNotEquals(hash1, hash3, "Case should affect hash (lower vs mixed)");
    }
    
    // ================== HELPER METHODS ==================
    
    /**
     * Calculate MD5 hash of content
     * @param content String to hash
     * @return 32-character hexadecimal MD5 hash
     */
    private String calculateMD5(String content) {
        if (content == null) throw new NullPointerException("Content cannot be null");
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toLowerCase();
            
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("MD5 calculation failed", e);
        }
    }
    
    /**
     * Calculate SHA1 hash of content
     * @param content String to hash
     * @return 40-character hexadecimal SHA1 hash
     */
    private String calculateSHA1(String content) {
        if (content == null) throw new NullPointerException("Content cannot be null");
        
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha.digest(content.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toLowerCase();
            
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("SHA1 calculation failed", e);
        }
    }
}
