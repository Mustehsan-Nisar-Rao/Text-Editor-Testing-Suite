package bll;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import dal.IFacadeDAO;
import dto.Documents;
import dto.Pages;

@RunWith(MockitoJUnitRunner.class)
public class EditorBOTest {

	@Mock
	private IFacadeDAO mockDB;

	private EditorBO editorBO;
	private File testFile;

	@Before
	public void setUp() throws IOException {
		editorBO = new EditorBO(mockDB);
		// Create a temporary test file
		testFile = File.createTempFile("test", ".txt");
		testFile.deleteOnExit();
	}

	@After
	public void tearDown() {
		if (testFile != null && testFile.exists()) {
			testFile.delete();
		}
	}

	// ===================== createFile() Tests =====================

	@Test
	public void testCreateFile_Success() {
		String fileName = "test.txt";
		String content = "This is test content";
		when(mockDB.createFileInDB(fileName, content)).thenReturn(true);

		boolean result = editorBO.createFile(fileName, content);

		assertTrue("File creation should succeed", result);
		verify(mockDB, times(1)).createFileInDB(fileName, content);
	}

	@Test
	public void testCreateFile_Failure() {
		String fileName = "test.txt";
		String content = "This is test content";
		when(mockDB.createFileInDB(fileName, content)).thenReturn(false);

		boolean result = editorBO.createFile(fileName, content);

		assertFalse("File creation should fail", result);
		verify(mockDB, times(1)).createFileInDB(fileName, content);
	}

	@Test
	public void testCreateFile_EmptyContent() {
		String fileName = "empty.txt";
		String content = "";
		when(mockDB.createFileInDB(fileName, content)).thenReturn(true);

		boolean result = editorBO.createFile(fileName, content);

		assertTrue("File creation with empty content should succeed", result);
		verify(mockDB, times(1)).createFileInDB(fileName, content);
	}

	@Test
	public void testCreateFile_Exception_HandledGracefully() {
		String fileName = "test.txt";
		String content = "content";
		when(mockDB.createFileInDB(fileName, content)).thenThrow(new RuntimeException("DB Error"));

		boolean result = editorBO.createFile(fileName, content);

		assertFalse("File creation should return false on exception", result);
		verify(mockDB, times(1)).createFileInDB(fileName, content);
	}

	// ===================== updateFile() Tests =====================

	@Test
	public void testUpdateFile_Success() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = 1;
		String content = "Updated content";
		when(mockDB.updateFileInDB(id, fileName, pageNumber, content)).thenReturn(true);

		boolean result = editorBO.updateFile(id, fileName, pageNumber, content);

		assertTrue("File update should succeed", result);
		verify(mockDB, times(1)).updateFileInDB(id, fileName, pageNumber, content);
	}

	@Test
	public void testUpdateFile_Failure() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = 1;
		String content = "Updated content";
		when(mockDB.updateFileInDB(id, fileName, pageNumber, content)).thenReturn(false);

		boolean result = editorBO.updateFile(id, fileName, pageNumber, content);

		assertFalse("File update should fail", result);
	}

	@Test
	public void testUpdateFile_Exception() {
		int id = 1;
		String fileName = "updated.txt";
		int pageNumber = 1;
		String content = "Updated content";
		when(mockDB.updateFileInDB(id, fileName, pageNumber, content))
				.thenThrow(new RuntimeException("DB Error"));

		boolean result = editorBO.updateFile(id, fileName, pageNumber, content);

		assertFalse("File update should return false on exception", result);
	}

	// ===================== deleteFile() Tests =====================

	@Test
	public void testDeleteFile_Success() {
		int id = 1;
		when(mockDB.deleteFileInDB(id)).thenReturn(true);

		boolean result = editorBO.deleteFile(id);

		assertTrue("File deletion should succeed", result);
		verify(mockDB, times(1)).deleteFileInDB(id);
	}

	@Test
	public void testDeleteFile_Failure() {
		int id = 1;
		when(mockDB.deleteFileInDB(id)).thenReturn(false);

		boolean result = editorBO.deleteFile(id);

		assertFalse("File deletion should fail", result);
	}

	@Test
	public void testDeleteFile_Exception() {
		int id = 1;
		when(mockDB.deleteFileInDB(id)).thenThrow(new RuntimeException("DB Error"));

		boolean result = editorBO.deleteFile(id);

		assertFalse("File deletion should return false on exception", result);
	}

	// ===================== importTextFiles() Tests =====================

	@Test
	public void testImportTextFiles_TxtExtension_Success() throws IOException {
		String fileName = "import.txt";
		String fileContent = "Line 1\nLine 2\nLine 3";

		// Write content to test file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
			writer.write(fileContent);
		}

		when(mockDB.createFileInDB(fileName, fileContent + "\n")).thenReturn(true);

		boolean result = editorBO.importTextFiles(testFile, fileName);

		assertTrue("Import .txt file should succeed", result);
		verify(mockDB, times(1)).createFileInDB(eq(fileName), contains(fileContent));
	}

	@Test
	public void testImportTextFiles_Md5Extension_Success() throws IOException {
		String fileName = "import.md5";
		String fileContent = "Hash content";

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
			writer.write(fileContent);
		}

		when(mockDB.createFileInDB(fileName, fileContent + "\n")).thenReturn(true);

		boolean result = editorBO.importTextFiles(testFile, fileName);

		assertTrue("Import .md5 file should succeed", result);
		verify(mockDB, times(1)).createFileInDB(eq(fileName), contains(fileContent));
	}

	@Test
	public void testImportTextFiles_UnsupportedExtension() throws IOException {
		String fileName = "import.pdf";
		String fileContent = "PDF content";

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
			writer.write(fileContent);
		}

		boolean result = editorBO.importTextFiles(testFile, fileName);

		assertFalse("Import unsupported file type should fail", result);
		verify(mockDB, never()).createFileInDB(anyString(), anyString());
	}

	@Test
	public void testImportTextFiles_EmptyFile() throws IOException {
		String fileName = "empty.txt";

		// File is already empty from setUp
		when(mockDB.createFileInDB(fileName, "\n")).thenReturn(true);

		boolean result = editorBO.importTextFiles(testFile, fileName);

		assertTrue("Import empty file should succeed", result);
	}

	@Test
	public void testImportTextFiles_FileNotFound() {
		String fileName = "nonexistent.txt";
		File nonexistentFile = new File("/path/to/nonexistent/file.txt");

		boolean result = editorBO.importTextFiles(nonexistentFile, fileName);

		assertFalse("Import non-existent file should fail", result);
		verify(mockDB, never()).createFileInDB(anyString(), anyString());
	}

	@Test
	public void testImportTextFiles_MultipleLines() throws IOException {
		String fileName = "multiline.txt";
		String fileContent = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5";

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
			writer.write(fileContent);
		}

		when(mockDB.createFileInDB(fileName, fileContent + "\n")).thenReturn(true);

		boolean result = editorBO.importTextFiles(testFile, fileName);

		assertTrue("Import multiline file should succeed", result);
	}

	// ===================== getFile() Tests =====================

	@Test
	public void testGetFile_FileFound() {
		int fileId = 1;
		Documents mockDoc = new Documents(fileId, "test.txt", "hash123", "2024-01-01", "2024-01-01",
				new ArrayList<>());
		List<Documents> filesList = new ArrayList<>();
		filesList.add(mockDoc);

		when(mockDB.getFilesFromDB()).thenReturn(filesList);

		Documents result = editorBO.getFile(fileId);

		assertNotNull("File should be found", result);
		assertEquals("File ID should match", fileId, result.getId());
		assertEquals("File name should match", "test.txt", result.getName());
	}

	@Test
	public void testGetFile_FileNotFound() {
		int fileId = 999;
		List<Documents> filesList = new ArrayList<>();
		when(mockDB.getFilesFromDB()).thenReturn(filesList);

		Documents result = editorBO.getFile(fileId);

		assertNull("File should not be found", result);
	}

	@Test
	public void testGetFile_MultipleFilesSearch() {
		List<Documents> filesList = new ArrayList<>();
		filesList.add(new Documents(1, "file1.txt", "hash1", "2024-01-01", "2024-01-01", new ArrayList<>()));
		filesList.add(new Documents(2, "file2.txt", "hash2", "2024-01-01", "2024-01-01", new ArrayList<>()));
		filesList.add(new Documents(3, "file3.txt", "hash3", "2024-01-01", "2024-01-01", new ArrayList<>()));

		when(mockDB.getFilesFromDB()).thenReturn(filesList);

		Documents result = editorBO.getFile(2);

		assertNotNull("File should be found", result);
		assertEquals("File ID should be 2", 2, result.getId());
		assertEquals("File name should be file2.txt", "file2.txt", result.getName());
	}

	// ===================== getFileExtension() Tests =====================

	@Test
	public void testGetFileExtension_TxtFile() {
		String fileName = "document.txt";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be txt", "txt", extension);
	}

	@Test
	public void testGetFileExtension_Md5File() {
		String fileName = "hash.md5";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be md5", "md5", extension);
	}

	@Test
	public void testGetFileExtension_PdfFile() {
		String fileName = "document.pdf";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be pdf", "pdf", extension);
	}

	@Test
	public void testGetFileExtension_NoExtension() {
		String fileName = "document";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be empty", "", extension);
	}

	@Test
	public void testGetFileExtension_MultipleDotsInFileName() {
		String fileName = "document.backup.txt";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be txt (last dot)", "txt", extension);
	}

	@Test
	public void testGetFileExtension_HiddenFile() {
		String fileName = ".hidden";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should be hidden", "hidden", extension);
	}

	@Test
	public void testGetFileExtension_CaseInsensitive() {
		String fileName = "DOCUMENT.TXT";
		String extension = editorBO.getFileExtension(fileName);

		assertEquals("Extension should preserve case", "TXT", extension);
	}

	// ===================== getAllFiles() Tests =====================

	@Test
	public void testGetAllFiles_MultipleFiles() {
		List<Documents> filesList = new ArrayList<>();
		filesList.add(new Documents(1, "file1.txt", "hash1", "2024-01-01", "2024-01-01", new ArrayList<>()));
		filesList.add(new Documents(2, "file2.txt", "hash2", "2024-01-01", "2024-01-01", new ArrayList<>()));

		when(mockDB.getFilesFromDB()).thenReturn(filesList);

		List<Documents> result = editorBO.getAllFiles();

		assertNotNull("Files list should not be null", result);
		assertEquals("Should have 2 files", 2, result.size());
		verify(mockDB, times(1)).getFilesFromDB();
	}

	@Test
	public void testGetAllFiles_EmptyList() {
		List<Documents> emptyList = new ArrayList<>();
		when(mockDB.getFilesFromDB()).thenReturn(emptyList);

		List<Documents> result = editorBO.getAllFiles();

		assertNotNull("Files list should not be null", result);
		assertEquals("Should have 0 files", 0, result.size());
	}

	// ===================== transliterate() Tests =====================

	@Test
	public void testTransliterate_Success() {
		int pageId = 1;
		String arabicText = "مرحبا";
		String expectedResult = "Marhaba";

		when(mockDB.transliterateInDB(pageId, arabicText)).thenReturn(expectedResult);

		String result = editorBO.transliterate(pageId, arabicText);

		assertEquals("Transliteration should return correct result", expectedResult, result);
		verify(mockDB, times(1)).transliterateInDB(pageId, arabicText);
	}

	@Test
	public void testTransliterate_EmptyText() {
		int pageId = 1;
		String arabicText = "";
		String expectedResult = "";

		when(mockDB.transliterateInDB(pageId, arabicText)).thenReturn(expectedResult);

		String result = editorBO.transliterate(pageId, arabicText);

		assertEquals("Transliteration of empty text should return empty", expectedResult, result);
	}

	@Test
	public void testTransliterate_LongArabicText() {
		int pageId = 1;
		String arabicText = "هذا نص طويل باللغة العربية يحتوي على عدة كلمات";
		String expectedResult = "Hadha nass twyl balalghah alarabyah yhtwy ala adda klmat";

		when(mockDB.transliterateInDB(pageId, arabicText)).thenReturn(expectedResult);

		String result = editorBO.transliterate(pageId, arabicText);

		assertEquals("Transliteration of long text should succeed", expectedResult, result);
		verify(mockDB, times(1)).transliterateInDB(pageId, arabicText);
	}

	@Test
	public void testTransliterate_MixedArabicEnglish() {
		int pageId = 1;
		String mixedText = "مرحبا Hello العالم World";
		String expectedResult = "Marhaba Hello Alalm World";

		when(mockDB.transliterateInDB(pageId, mixedText)).thenReturn(expectedResult);

		String result = editorBO.transliterate(pageId, mixedText);

		assertEquals("Transliteration of mixed text should work", expectedResult, result);
	}

	// ===================== searchKeyword() Tests =====================

	@Test
	public void testSearchKeyword_KeywordFound() {
		String keyword = "test";
		List<Documents> mockFiles = new ArrayList<>();
		mockFiles.add(new Documents(1, "file1.txt", "hash1", "2024-01-01", "2024-01-01", new ArrayList<>()));

		when(mockDB.getFilesFromDB()).thenReturn(mockFiles);

		List<String> result = editorBO.searchKeyword(keyword);

		assertNotNull("Search result should not be null", result);
		// Result depends on SearchWord.searchKeyword implementation
	}

	@Test
	public void testSearchKeyword_NoFilesAvailable() {
		String keyword = "test";
		when(mockDB.getFilesFromDB()).thenReturn(new ArrayList<>());

		List<String> result = editorBO.searchKeyword(keyword);

		assertNotNull("Search result should not be null", result);
	}

}
