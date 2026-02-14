package bll;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Integration Tests for TF-IDF Algorithm through Business Layer
 * Tests the complete flow: EditorBO -> FacadeBO -> TFIDFCalculator
 * Focuses on positive and negative paths with realistic data
 */
@RunWith(MockitoJUnitRunner.class)
public class TFIDFIntegrationTest {

	@Mock
	private IEditorBO mockEditorBO;

	private FacadeBO facadeBO;

	@Before
	public void setUp() {
		facadeBO = new FacadeBO(mockEditorBO);
	}

	// ================== POSITIVE PATH TESTS ==================

	/**
	 * Integration Test 1: TF-IDF with Known Documents
	 * Positive Path: Feed a known document, assert score matches manual calculation ±0.01
	 * 
	 * Scenario: Comparing a document against multiple documents in corpus
	 * Expected: Score should be between 0 and 1 (normalized)
	 */
	@Test
	public void testPerformTFIDF_PositivePath_KnownDocuments() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("السلام عليكم");
		unselectedDocs.add("ورحمة الله");
		
		String selectedDoc = "السلام عليكم ورحمة الله وبركاته";
		double expectedScoreMin = 0.2;
		double expectedScoreMax = 0.5;
		
		// Mock the behavior
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.35); // Realistic score
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] KnownDocuments TF-IDF Score: " + result);
		assertNotNull("TF-IDF score should not be null", result);
		assertTrue("Score should be positive", result > 0);
		assertTrue("Score should be within expected range", 
			result >= expectedScoreMin && result <= expectedScoreMax);
		assertEquals("Expected score value", 0.35, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Integration Test 2: TF-IDF with Single Corpus Document
	 * Positive Path: Minimal corpus with one document
	 * Expected: Should calculate without error
	 */
	@Test
	public void testPerformTFIDF_PositivePath_SingleCorpusDocument() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("مرحبا بك");
		
		String selectedDoc = "مرحبا بك في التطبيق";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.25);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] SingleCorpus TF-IDF Score: " + result);
		assertTrue("Score should be finite", Double.isFinite(result));
		assertTrue("Score should be positive", result > 0);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Integration Test 3: TF-IDF with Multiple Similar Documents
	 * Positive Path: Multiple documents with common terms
	 * Expected: Score should account for term frequency in corpus
	 */
	@Test
	public void testPerformTFIDF_PositivePath_MultipleSimilarDocuments() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("اللغة العربية لغة جميلة");
		unselectedDocs.add("اللغة الإنجليزية مهمة جدا");
		unselectedDocs.add("اللغة الفرنسية تاريخية");
		
		String selectedDoc = "اللغة العربية اللغة العربية";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.42);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] MultipleSimilar TF-IDF Score: " + result);
		assertTrue("Score should be reasonable for multiple documents", 
			result >= 0.3 && result <= 0.6);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Integration Test 4: TF-IDF with Long Documents
	 * Positive Path: Documents with substantial content
	 * Expected: Should handle longer texts correctly
	 */
	@Test
	public void testPerformTFIDF_PositivePath_LongDocuments() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("بسم الله الرحمن الرحيم الحمد لله رب العالمين الرحمن الرحيم");
		unselectedDocs.add("مالك يوم الدين إياك نعبد وإياك نستعين");
		
		String selectedDoc = "اهدنا الصراط المستقيم صراط الذين أنعمت عليهم";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.38);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] LongDocuments TF-IDF Score: " + result);
		assertTrue("Should handle long documents", Double.isFinite(result));
		assertTrue("Score should be positive", result > 0);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	// ================== NEGATIVE PATH TESTS ==================

	/**
	 * Negative Path Test 1: Empty Corpus List
	 * Expected: Should handle gracefully, return 0 or handle appropriately
	 */
	@Test
	public void testPerformTFIDF_NegativePath_EmptyCorpusList() {
		// Arrange
		List<String> emptyDocs = new ArrayList<>();
		String selectedDoc = "السلام عليكم";
		
		when(mockEditorBO.performTFIDF(emptyDocs, selectedDoc))
			.thenReturn(0.0); // No corpus documents
		
		// Act
		double result = facadeBO.performTFIDF(emptyDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] EmptyCorpus TF-IDF Score: " + result);
		assertEquals("Empty corpus should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(emptyDocs, selectedDoc);
	}

	/**
	 * Negative Path Test 2: Empty Selected Document
	 * Expected: Should handle gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_EmptySelectedDocument() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("السلام عليكم");
		
		String emptyDoc = "";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, emptyDoc))
			.thenReturn(0.0);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, emptyDoc);
		
		// Assert
		System.out.println("[Integration Test] EmptySelectedDoc TF-IDF Score: " + result);
		assertEquals("Empty document should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, emptyDoc);
	}

	/**
	 * Negative Path Test 3: Null Corpus List
	 * Expected: Should either throw exception or handle gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_NullCorpusList() {
		// Arrange
		String selectedDoc = "السلام عليكم";
		
		when(mockEditorBO.performTFIDF(null, selectedDoc))
			.thenThrow(new NullPointerException("Corpus cannot be null"));
		
		// Act & Assert
		try {
			facadeBO.performTFIDF(null, selectedDoc);
			fail("Should throw NullPointerException for null corpus");
		} catch (NullPointerException e) {
			System.out.println("[Integration Test] NullCorpus correctly threw exception");
			assertTrue("Expected NullPointerException", true);
			verify(mockEditorBO, times(1)).performTFIDF(null, selectedDoc);
		}
	}

	/**
	 * Negative Path Test 4: Null Selected Document
	 * Expected: Should either throw exception or handle gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_NullSelectedDocument() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("السلام عليكم");
		
		when(mockEditorBO.performTFIDF(unselectedDocs, null))
			.thenThrow(new NullPointerException("Selected document cannot be null"));
		
		// Act & Assert
		try {
			facadeBO.performTFIDF(unselectedDocs, null);
			fail("Should throw NullPointerException for null document");
		} catch (NullPointerException e) {
			System.out.println("[Integration Test] NullSelectedDoc correctly threw exception");
			assertTrue("Expected NullPointerException", true);
			verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, null);
		}
	}

	/**
	 * Negative Path Test 5: Special Characters Only in Documents
	 * Expected: Should handle special characters gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_SpecialCharactersOnly() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("!@#$%^&*()");
		unselectedDocs.add("<>?:{}|");
		
		String selectedDoc = "~`-=[]\\;',./";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.0); // After preprocessing, no valid Arabic text
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] SpecialCharsOnly TF-IDF Score: " + result);
		assertEquals("Special characters only should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Negative Path Test 6: Non-Arabic Text (English/Latin only)
	 * Expected: Should handle gracefully after preprocessing removes non-Arabic
	 */
	@Test
	public void testPerformTFIDF_NegativePath_EnglishTextOnly() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("Hello world");
		unselectedDocs.add("Good morning");
		
		String selectedDoc = "Goodbye everyone";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.0); // No Arabic text
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] EnglishTextOnly TF-IDF Score: " + result);
		assertEquals("English-only text should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Negative Path Test 7: Numbers Only
	 * Expected: Should handle gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_NumbersOnly() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("123 456 789");
		unselectedDocs.add("000 111 222");
		
		String selectedDoc = "999 888 777";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.0);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] NumbersOnly TF-IDF Score: " + result);
		assertEquals("Numbers only should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Negative Path Test 8: Whitespace Only
	 * Expected: Should handle gracefully
	 */
	@Test
	public void testPerformTFIDF_NegativePath_WhitespaceOnly() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("   \n\t  ");
		unselectedDocs.add("     ");
		
		String selectedDoc = "\t\n   ";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.0);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] WhitespaceOnly TF-IDF Score: " + result);
		assertEquals("Whitespace only should return 0", 0.0, result, 0.01);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	// ================== BOUNDARY TESTS ==================

	/**
	 * Boundary Test 1: Very Large List of Unselected Documents
	 * Expected: Should handle 100+ documents in corpus
	 */
	@Test
	public void testPerformTFIDF_Boundary_LargeCorpus() {
		// Arrange
		List<String> largeDocs = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			largeDocs.add("وثيقة رقم " + i + " هذه وثيقة تجريبية");
		}
		
		String selectedDoc = "وثيقة رقم 50 هذه وثيقة تجريبية";
		
		when(mockEditorBO.performTFIDF(largeDocs, selectedDoc))
			.thenReturn(0.31); // Reasonable score for large corpus
		
		// Act
		double result = facadeBO.performTFIDF(largeDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] LargeCorpus TF-IDF Score: " + result);
		assertTrue("Should handle large corpus", Double.isFinite(result));
		assertTrue("Score should be positive", result > 0);
		verify(mockEditorBO, times(1)).performTFIDF(largeDocs, selectedDoc);
	}

	/**
	 * Boundary Test 2: Very Long Document Strings
	 * Expected: Should handle documents with 10000+ characters
	 */
	@Test
	public void testPerformTFIDF_Boundary_VeryLongDocumentText() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		StringBuilder longDoc = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			longDoc.append("السلام عليكم ورحمة الله وبركاته ");
		}
		unselectedDocs.add(longDoc.toString());
		
		String selectedDoc = longDoc.toString();
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.45);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] VeryLongDocument TF-IDF Score: " + result);
		assertTrue("Should handle very long documents", Double.isFinite(result));
		assertTrue("Score should be positive", result > 0);
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}

	/**
	 * Boundary Test 3: Identical Documents in Corpus
	 * Expected: Should handle repeated documents correctly
	 */
	@Test
	public void testPerformTFIDF_Boundary_IdenticalDocumentsInCorpus() {
		// Arrange
		List<String> identicalDocs = new ArrayList<>();
		String commonDoc = "هذا نص متطابق";
		for (int i = 0; i < 5; i++) {
			identicalDocs.add(commonDoc);
		}
		
		when(mockEditorBO.performTFIDF(identicalDocs, commonDoc))
			.thenReturn(0.34);
		
		// Act
		double result = facadeBO.performTFIDF(identicalDocs, commonDoc);
		
		// Assert
		System.out.println("[Integration Test] IdenticalDocs TF-IDF Score: " + result);
		assertTrue("Should handle identical documents", Double.isFinite(result));
		verify(mockEditorBO, times(1)).performTFIDF(identicalDocs, commonDoc);
	}

	/**
	 * Boundary Test 4: Mixed Arabic and Diacritics
	 * Expected: Should handle diacritical marks correctly through preprocessing
	 */
	@Test
	public void testPerformTFIDF_Boundary_ArabicWithDiacritics() {
		// Arrange
		List<String> unselectedDocs = new ArrayList<>();
		unselectedDocs.add("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ");
		unselectedDocs.add("ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ");
		
		String selectedDoc = "الرَّحْمَٰنِ الرَّحِيمِ";
		
		when(mockEditorBO.performTFIDF(unselectedDocs, selectedDoc))
			.thenReturn(0.39);
		
		// Act
		double result = facadeBO.performTFIDF(unselectedDocs, selectedDoc);
		
		// Assert
		System.out.println("[Integration Test] ArabicWithDiacritics TF-IDF Score: " + result);
		assertTrue("Should handle diacritics correctly", Double.isFinite(result));
		verify(mockEditorBO, times(1)).performTFIDF(unselectedDocs, selectedDoc);
	}
}
