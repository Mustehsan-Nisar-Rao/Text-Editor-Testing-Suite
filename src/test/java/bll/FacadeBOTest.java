package bll;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import dto.Documents;

@RunWith(MockitoJUnitRunner.class)
public class FacadeBOTest {

	@Mock
	private IEditorBO mockEditorBO;

	private FacadeBO facadeBO;

	@Before
	public void setUp() {
		facadeBO = new FacadeBO(mockEditorBO);
	}

	// ===================== createFile() Tests =====================

	@Test
	public void testCreateFile_Success() {
		String fileName = "test.txt";
		String content = "Test content";
		when(mockEditorBO.createFile(fileName, content)).thenReturn(true);

		boolean result = facadeBO.createFile(fileName, content);

		assertTrue("File creation should succeed", result);
		verify(mockEditorBO, times(1)).createFile(fileName, content);
	}

	@Test
	public void testCreateFile_Failure() {
		String fileName = "test.txt";
		String content = "Test content";
		when(mockEditorBO.createFile(fileName, content)).thenReturn(false);

		boolean result = facadeBO.createFile(fileName, content);

		assertFalse("File creation should fail", result);
		verify(mockEditorBO, times(1)).createFile(fileName, content);
	}

	@Test
	public void testCreateFile_NullFileName() {
		String content = "Test content";
		when(mockEditorBO.createFile(null, content)).thenReturn(false);

		boolean result = facadeBO.createFile(null, content);

		assertFalse("File creation with null name should fail", result);
	}

	@Test
	public void testCreateFile_NullContent() {
		String fileName = "test.txt";
		when(mockEditorBO.createFile(fileName, null)).thenReturn(false);

		boolean result = facadeBO.createFile(fileName, null);

		assertFalse("File creation with null content should fail", result);
	}

	// ===================== updateFile() Tests =====================

	@Test
	public void testUpdateFile_Success() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = 1;
		String content = "Updated content";
		when(mockEditorBO.updateFile(id, fileName, pageNumber, content)).thenReturn(true);

		boolean result = facadeBO.updateFile(id, fileName, pageNumber, content);

		assertTrue("File update should succeed", result);
		verify(mockEditorBO, times(1)).updateFile(id, fileName, pageNumber, content);
	}

	@Test
	public void testUpdateFile_Failure() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = 1;
		String content = "Updated content";
		when(mockEditorBO.updateFile(id, fileName, pageNumber, content)).thenReturn(false);

		boolean result = facadeBO.updateFile(id, fileName, pageNumber, content);

		assertFalse("File update should fail", result);
	}

	@Test
	public void testUpdateFile_InvalidPageNumber() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = -1;
		String content = "Updated content";
		when(mockEditorBO.updateFile(id, fileName, pageNumber, content)).thenReturn(false);

		boolean result = facadeBO.updateFile(id, fileName, pageNumber, content);

		assertFalse("Update with invalid page number should fail", result);
	}

	// ===================== deleteFile() Tests =====================

	@Test
	public void testDeleteFile_Success() {
		int id = 1;
		when(mockEditorBO.deleteFile(id)).thenReturn(true);

		boolean result = facadeBO.deleteFile(id);

		assertTrue("File deletion should succeed", result);
		verify(mockEditorBO, times(1)).deleteFile(id);
	}

	@Test
	public void testDeleteFile_Failure() {
		int id = 1;
		when(mockEditorBO.deleteFile(id)).thenReturn(false);

		boolean result = facadeBO.deleteFile(id);

		assertFalse("File deletion should fail", result);
	}

	@Test
	public void testDeleteFile_InvalidId() {
		int id = -1;
		when(mockEditorBO.deleteFile(id)).thenReturn(false);

		boolean result = facadeBO.deleteFile(id);

		assertFalse("Deletion with invalid ID should fail", result);
	}

	// ===================== importTextFiles() Tests =====================

	@Test
	public void testImportTextFiles_Success() {
		File file = new File("test.txt");
		String fileName = "test.txt";
		when(mockEditorBO.importTextFiles(file, fileName)).thenReturn(true);

		boolean result = facadeBO.importTextFiles(file, fileName);

		assertTrue("Import should succeed", result);
		verify(mockEditorBO, times(1)).importTextFiles(file, fileName);
	}

	@Test
	public void testImportTextFiles_Failure() {
		File file = new File("test.txt");
		String fileName = "test.txt";
		when(mockEditorBO.importTextFiles(file, fileName)).thenReturn(false);

		boolean result = facadeBO.importTextFiles(file, fileName);

		assertFalse("Import should fail", result);
	}

	@Test
	public void testImportTextFiles_NullFile() {
		String fileName = "test.txt";
		when(mockEditorBO.importTextFiles(null, fileName)).thenReturn(false);

		boolean result = facadeBO.importTextFiles(null, fileName);

		assertFalse("Import with null file should fail", result);
	}

	// ===================== getFile() Tests =====================

	@Test
	public void testGetFile_FileFound() {
		int id = 1;
		Documents mockDoc = new Documents(id, "test.txt", "hash123", "2024-01-01", "2024-01-01",
				new ArrayList<>());
		when(mockEditorBO.getFile(id)).thenReturn(mockDoc);

		Documents result = facadeBO.getFile(id);

		assertNotNull("File should be found", result);
		assertEquals("File ID should match", id, result.getId());
		verify(mockEditorBO, times(1)).getFile(id);
	}

	@Test
	public void testGetFile_FileNotFound() {
		int id = 999;
		when(mockEditorBO.getFile(id)).thenReturn(null);

		Documents result = facadeBO.getFile(id);

		assertNull("File should not be found", result);
	}

	// ===================== getAllFiles() Tests =====================

	@Test
	public void testGetAllFiles_MultipleFiles() {
		List<Documents> mockFiles = new ArrayList<>();
		mockFiles.add(new Documents(1, "file1.txt", "hash1", "2024-01-01", "2024-01-01", new ArrayList<>()));
		mockFiles.add(new Documents(2, "file2.txt", "hash2", "2024-01-01", "2024-01-01", new ArrayList<>()));
		when(mockEditorBO.getAllFiles()).thenReturn(mockFiles);

		List<Documents> result = facadeBO.getAllFiles();

		assertNotNull("Files list should not be null", result);
		assertEquals("Should have 2 files", 2, result.size());
		verify(mockEditorBO, times(1)).getAllFiles();
	}

	@Test
	public void testGetAllFiles_EmptyList() {
		List<Documents> emptyList = new ArrayList<>();
		when(mockEditorBO.getAllFiles()).thenReturn(emptyList);

		List<Documents> result = facadeBO.getAllFiles();

		assertNotNull("Files list should not be null", result);
		assertEquals("Should have 0 files", 0, result.size());
	}

	// ===================== getFileExtension() Tests =====================

	@Test
	public void testGetFileExtension_TxtFile() {
		String fileName = "document.txt";
		when(mockEditorBO.getFileExtension(fileName)).thenReturn("txt");

		String result = facadeBO.getFileExtension(fileName);

		assertEquals("Extension should be txt", "txt", result);
		verify(mockEditorBO, times(1)).getFileExtension(fileName);
	}

	@Test
	public void testGetFileExtension_NoExtension() {
		String fileName = "document";
		when(mockEditorBO.getFileExtension(fileName)).thenReturn("");

		String result = facadeBO.getFileExtension(fileName);

		assertEquals("Extension should be empty", "", result);
	}

	@Test
	public void testGetFileExtension_MultipleExtensions() {
		String fileName = "archive.tar.gz";
		when(mockEditorBO.getFileExtension(fileName)).thenReturn("gz");

		String result = facadeBO.getFileExtension(fileName);

		assertEquals("Extension should be gz", "gz", result);
	}

	// ===================== transliterate() Tests =====================

	@Test
	public void testTransliterate_Success() {
		int pageId = 1;
		String arabicText = "مرحبا";
		String expectedResult = "Marhaba";
		when(mockEditorBO.transliterate(pageId, arabicText)).thenReturn(expectedResult);

		String result = facadeBO.transliterate(pageId, arabicText);

		assertEquals("Transliteration should succeed", expectedResult, result);
		verify(mockEditorBO, times(1)).transliterate(pageId, arabicText);
	}

	@Test
	public void testTransliterate_EmptyText() {
		int pageId = 1;
		String arabicText = "";
		when(mockEditorBO.transliterate(pageId, arabicText)).thenReturn("");

		String result = facadeBO.transliterate(pageId, arabicText);

		assertEquals("Empty text should return empty", "", result);
	}

	@Test
	public void testTransliterate_NullText() {
		int pageId = 1;
		when(mockEditorBO.transliterate(pageId, null)).thenReturn(null);

		String result = facadeBO.transliterate(pageId, null);

		assertNull("Null text should return null", result);
	}

	@Test
	public void testTransliterate_LongText() {
		int pageId = 1;
		String arabicText = "هذا نص طويل جداً يحتوي على العديد من الكلمات العربية المختلفة";
		String expectedResult = "Hadha nss twyl jdan yhtwy ala aladdid mn alklimat alarabyah almkhtlfa";
		when(mockEditorBO.transliterate(pageId, arabicText)).thenReturn(expectedResult);

		String result = facadeBO.transliterate(pageId, arabicText);

		assertEquals("Long text transliteration should work", expectedResult, result);
	}

	// ===================== searchKeyword() Tests =====================

	@Test
	public void testSearchKeyword_KeywordFound() {
		String keyword = "test";
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("result1");
		expectedResults.add("result2");
		when(mockEditorBO.searchKeyword(keyword)).thenReturn(expectedResults);

		List<String> result = facadeBO.searchKeyword(keyword);

		assertNotNull("Results should not be null", result);
		assertEquals("Should have 2 results", 2, result.size());
		verify(mockEditorBO, times(1)).searchKeyword(keyword);
	}

	@Test
	public void testSearchKeyword_NoResults() {
		String keyword = "nonexistent";
		when(mockEditorBO.searchKeyword(keyword)).thenReturn(new ArrayList<>());

		List<String> result = facadeBO.searchKeyword(keyword);

		assertNotNull("Results should not be null", result);
		assertEquals("Should have 0 results", 0, result.size());
	}

	@Test
	public void testSearchKeyword_EmptyKeyword() {
		String keyword = "";
		when(mockEditorBO.searchKeyword(keyword)).thenReturn(new ArrayList<>());

		List<String> result = facadeBO.searchKeyword(keyword);

		assertNotNull("Results should not be null", result);
	}

	// ===================== lemmatizeWords() Tests =====================

	@Test
	public void testLemmatizeWords_Success() {
		String text = "running runs runner";
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("running", "run");
		expectedResult.put("runs", "run");
		expectedResult.put("runner", "run");
		when(mockEditorBO.lemmatizeWords(text)).thenReturn(expectedResult);

		Map<String, String> result = facadeBO.lemmatizeWords(text);

		assertNotNull("Results should not be null", result);
		assertEquals("Should have 3 entries", 3, result.size());
		verify(mockEditorBO, times(1)).lemmatizeWords(text);
	}

	@Test
	public void testLemmatizeWords_EmptyText() {
		String text = "";
		when(mockEditorBO.lemmatizeWords(text)).thenReturn(new HashMap<>());

		Map<String, String> result = facadeBO.lemmatizeWords(text);

		assertNotNull("Results should not be null", result);
		assertEquals("Should have 0 entries", 0, result.size());
	}

	// ===================== extractPOS() Tests =====================

	@Test
	public void testExtractPOS_Success() {
		String text = "The quick brown fox";
		Map<String, List<String>> expectedResult = new HashMap<>();
		expectedResult.put("NOUN", new ArrayList<>());
		expectedResult.put("ADJ", new ArrayList<>());
		when(mockEditorBO.extractPOS(text)).thenReturn(expectedResult);

		Map<String, List<String>> result = facadeBO.extractPOS(text);

		assertNotNull("Results should not be null", result);
		assertTrue("Should contain NOUN", result.containsKey("NOUN"));
		verify(mockEditorBO, times(1)).extractPOS(text);
	}

	// ===================== extractRoots() Tests =====================

	@Test
	public void testExtractRoots_Success() {
		String text = "مرحبا";
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("مرحبا", "رحب");
		when(mockEditorBO.extractRoots(text)).thenReturn(expectedResult);

		Map<String, String> result = facadeBO.extractRoots(text);

		assertNotNull("Results should not be null", result);
		verify(mockEditorBO, times(1)).extractRoots(text);
	}

	// ===================== performTFIDF() Tests =====================

	@Test
	public void testPerformTFIDF_Success() {
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("doc1 content");
		String selectedDocContent = "selected doc content";
		double expectedResult = 0.75;

		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDocContent)).thenReturn(expectedResult);

		double result = facadeBO.performTFIDF(unselectedDocs, selectedDocContent);

		assertEquals("TFIDF result should be 0.75", 0.75, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDocContent);
	}

	// ===================== performPMI() Tests =====================

	@Test
	public void testPerformPMI_Success() {
		String content = "test content";
		Map<String, Double> expectedResult = new HashMap<>();
		expectedResult.put("test", 0.5);
		expectedResult.put("content", 0.3);

		when(mockEditorBO.performPMI(content)).thenReturn(expectedResult);

		Map<String, Double> result = facadeBO.performPMI(content);

		assertNotNull("Results should not be null", result);
		assertEquals("Should have 2 entries", 2, result.size());
		verify(mockEditorBO, times(1)).performPMI(content);
	}

	// ===================== performPKL() Tests =====================

	@Test
	public void testPerformPKL_Success() {
		String content = "test content";
		Map<String, Double> expectedResult = new HashMap<>();
		expectedResult.put("test", 0.6);

		when(mockEditorBO.performPKL(content)).thenReturn(expectedResult);

		Map<String, Double> result = facadeBO.performPKL(content);

		assertNotNull("Results should not be null", result);
		verify(mockEditorBO, times(1)).performPKL(content);
	}

	// ===================== stemWords() Tests =====================

	@Test
	public void testStemWords_Success() {
		String text = "running runs runner";
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("running", "run");
		expectedResult.put("runs", "run");

		when(mockEditorBO.stemWords(text)).thenReturn(expectedResult);

		Map<String, String> result = facadeBO.stemWords(text);

		assertNotNull("Results should not be null", result);
		verify(mockEditorBO, times(1)).stemWords(text);
	}

	// ===================== segmentWords() Tests =====================

	@Test
	public void testSegmentWords_Success() {
		String text = "مرحبابالعالم";
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("مرحبا", "word1");
		expectedResult.put("بالعالم", "word2");

		when(mockEditorBO.segmentWords(text)).thenReturn(expectedResult);

		Map<String, String> result = facadeBO.segmentWords(text);

		assertNotNull("Results should not be null", result);
		verify(mockEditorBO, times(1)).segmentWords(text);
	}

}
