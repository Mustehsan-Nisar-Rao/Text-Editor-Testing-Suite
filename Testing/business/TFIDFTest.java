package business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import bll.EditorBO;
import dal.FacadeDAO;
import dal.EditorDBDAO;

/**
 * TF-IDF Algorithm Test Suite - WHITE BOX TESTING
 * 
 * This test suite IDENTIFIES BUGS in the TF-IDF implementation.
 * Many tests will FAIL initially, indicating bugs that need to be fixed.
 * 
 * Expected Behavior:
 * - Initially: ~17 tests FAIL (showing bugs)
 * - After fixes: All tests PASS
 * 
 * @author [Your Team Name]
 * @date February 15, 2026
 */
@DisplayName("TF-IDF Algorithm Test Suite")
public class TFIDFTest {
    
    private EditorBO businessObj;
    private static final double DELTA = 0.01;
    
    @BeforeEach
    void setUp() {
        EditorDBDAO editorDBDAO = new EditorDBDAO();
        FacadeDAO facadeDAO = new FacadeDAO(editorDBDAO);
        businessObj = new EditorBO(facadeDAO);
    }
    
    // ================== POSITIVE PATH TESTS ==================
    
    @Test
    @DisplayName("Positive: Arabic text processing")
    void testTFIDF_Positive_ArabicText() {
        List<String> corpus = Arrays.asList(
            "السلام عليكم ورحمة الله",
            "مرحبا بك في التطبيق",
            "هذا نص عربي للتجربة"
        );
        
        String document = "السلام عليكم ورحمة الله وبركاته";
        
        double result = businessObj.performTFIDF(corpus, document);
        double expected = 0.120;
        
        System.out.println("Arabic Text ACTUAL: " + result);
        
        assertFalse(Double.isNaN(result), "TF-IDF should not return NaN for Arabic text");
        assertEquals(expected, result, DELTA, "Arabic text TF-IDF score should be ~0.120");
        assertTrue(result > 0, "Arabic text should have positive TF-IDF score");
    }
    
    @Test
    @DisplayName("Positive: Single word document")
    void testTFIDF_Positive_SingleWord() {
        List<String> corpus = Arrays.asList(
            "hello world",
            "world peace",
            "peace love"
        );
        
        String document = "world";
        
        double result = businessObj.performTFIDF(corpus, document);
        double expected = 1.386;
        
        System.out.println("Single Word ACTUAL: " + result);
        
        assertFalse(Double.isNaN(result), "TF-IDF should not return NaN for single word");
        assertEquals(expected, result, DELTA, "Single word TF-IDF should be ~1.386");
        assertTrue(result > 0, "Single word should have positive score");
    }
    
    @Test
    @DisplayName("Positive: Known document from corpus - WILL FAIL (BUG)")
    void testTFIDF_Positive_KnownDocument() {
        List<String> corpus = Arrays.asList(
            "the cat sat on the mat",
            "the dog sat on the log",
            "cats and dogs are pets"
        );
        
        String document = "the cat sat on the mat";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Known Document ACTUAL: " + result);
        
        // BUG: Currently returns NaN instead of valid score
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: TF-IDF returns NaN for valid document with common terms");
        assertTrue(result >= 0, "TF-IDF should return non-negative value");
    }
    
    @Test
    @DisplayName("Positive: Document with repeated term - WILL FAIL (BUG)")
    void testTFIDF_Positive_SpecificTerm() {
        List<String> corpus = Arrays.asList(
            "term appears in document one",
            "document two has different words",
            "term appears again in document three"
        );
        
        String document = "this document has the term term term";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Specific Term ACTUAL: " + result);
        
        // BUG: Should calculate TF-IDF for repeated terms
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: TF-IDF returns NaN for repeated terms");
        assertTrue(result >= 0, "Repeated terms should produce valid score");
    }
    
    @Test
    @DisplayName("Positive: Unique term not in corpus - WILL FAIL (BUG)")
    void testTFIDF_Positive_UniqueTerm() {
        List<String> corpus = Arrays.asList(
            "document one content",
            "document two content",
            "document three content"
        );
        
        String document = "unique term appears only here";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Unique Term ACTUAL: " + result);
        
        // BUG: Unique terms should have high TF-IDF scores
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: TF-IDF returns NaN for unique terms");
        assertTrue(result > 0, "Unique terms should have high positive score");
    }
    
    @Test
    @DisplayName("Positive: Common terms across corpus - WILL FAIL (BUG)")
    void testTFIDF_Positive_MultipleSimilar() {
        List<String> corpus = Arrays.asList(
            "language is beautiful language",
            "language is important language",
            "language has history language"
        );
        
        String document = "language language language";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Multiple Similar ACTUAL: " + result);
        
        // BUG: Common terms should have low (but valid) scores
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: TF-IDF returns NaN for common terms (IDF division by zero)");
        assertTrue(Double.isFinite(result), "Should return finite value");
    }
    
    @Test
    @DisplayName("Positive: Mixed case sensitivity - WILL FAIL (BUG)")
    void testTFIDF_Positive_MixedCase() {
        List<String> corpus = Arrays.asList(
            "Test Document One",
            "test document two",
            "TEST DOCUMENT THREE"
        );
        
        String document = "TeSt DoCuMeNt";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Mixed Case ACTUAL: " + result);
        
        // BUG: Case should be normalized
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Mixed case returns NaN (tokenization issue)");
        assertTrue(Double.isFinite(result), "Should handle case variations");
    }
    
    @Test
    @DisplayName("Positive: Document with punctuation - WILL FAIL (BUG)")
    void testTFIDF_Positive_WithPunctuation() {
        List<String> corpus = Arrays.asList(
            "Hello, world!",
            "Hello? Yes.",
            "Hello!"
        );
        
        String document = "Hello, how are you?";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("With Punctuation ACTUAL: " + result);
        
        // BUG: Punctuation should be stripped during tokenization
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Punctuation causes NaN (tokenization failure)");
        assertTrue(result >= 0, "Should extract words from punctuated text");
    }
    
    @Test
    @DisplayName("Positive: Mixed Arabic and English")
    void testTFIDF_Positive_MixedLanguages() {
        List<String> corpus = Arrays.asList(
            "Hello مرحبا",
            "World العالم",
            "Test اختبار"
        );
        
        String document = "Hello مرحبا World";
        
        double result = businessObj.performTFIDF(corpus, document);
        double expected = 0.029;
        
        System.out.println("Mixed Languages ACTUAL: " + result);
        
        assertFalse(Double.isNaN(result), "Mixed languages should work");
        assertEquals(expected, result, DELTA, "Mixed languages score should be ~0.029");
    }
    
    // ================== NEGATIVE PATH TESTS ==================
    
    @Test
    @DisplayName("Negative: Empty document string")
    void testTFIDF_Negative_EmptyDocument() {
        List<String> corpus = Arrays.asList("doc1", "doc2", "doc3");
        String emptyDoc = "";
        
        double result = businessObj.performTFIDF(corpus, emptyDoc);
        double expected = -0.288;
        
        System.out.println("Empty Document ACTUAL: " + result);
        
        assertEquals(expected, result, DELTA, "Empty document should return -0.288");
        assertTrue(result < 0, "Empty document should have negative score");
    }
    
    @Test
    @DisplayName("Negative: Empty corpus - WILL FAIL (BUG)")
    void testTFIDF_Negative_EmptyCorpus() {
        List<String> emptyCorpus = new ArrayList<>();
        String document = "test document";
        
        // BUG: Should throw IllegalArgumentException, not return NaN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            businessObj.performTFIDF(emptyCorpus, document);
        }, "BUG FOUND: Empty corpus should throw IllegalArgumentException, not return NaN");
        
        System.out.println("Empty Corpus threw: " + exception.getMessage());
    }
    
    @Test
    @DisplayName("Negative: Special characters only")
    void testTFIDF_Negative_SpecialCharactersOnly() {
        List<String> corpus = Arrays.asList("normal", "documents");
        String specialDoc = "!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~`";
        
        double result = businessObj.performTFIDF(corpus, specialDoc);
        double expected = -0.405;
        
        System.out.println("Special Characters ACTUAL: " + result);
        
        assertEquals(expected, result, DELTA, "Special chars should return -0.405");
        assertTrue(result < 0, "Invalid content should have negative score");
    }
    
    @Test
    @DisplayName("Negative: Numbers only - WILL FAIL (BUG)")
    void testTFIDF_Negative_NumbersOnly() {
        List<String> corpus = Arrays.asList("text", "documents");
        String numbersDoc = "123 456 789 0";
        
        double result = businessObj.performTFIDF(corpus, numbersDoc);
        
        System.out.println("Numbers Only ACTUAL: " + result);
        
        // BUG: Should handle numbers gracefully (treat as empty or return negative)
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Numbers should be handled, not return NaN");
        assertTrue(Double.isFinite(result), "Should return finite value for numbers");
    }
    
    @Test
    @DisplayName("Negative: Whitespace only - WILL FAIL (BUG)")
    void testTFIDF_Negative_WhitespaceOnly() {
        List<String> corpus = Arrays.asList("content", "files");
        String whitespaceDoc = "   \n   \t   \r\n   ";
        
        double result = businessObj.performTFIDF(corpus, whitespaceDoc);
        
        System.out.println("Whitespace Only ACTUAL: " + result);
        
        // BUG: Should treat as empty document, return negative value
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Whitespace should be treated as empty, not return NaN");
        assertTrue(result < 0, "Whitespace-only should return negative value like empty doc");
    }
    
    @Test
    @DisplayName("Negative: Mixed special chars - WILL FAIL (BUG)")
    void testTFIDF_Negative_MixedSpecialChars() {
        List<String> corpus = Arrays.asList(
            "normal document with text",
            "another normal document"
        );
        String mixedDoc = "Hello! @World# $This% ^Is& *Test+";
        
        double result = businessObj.performTFIDF(corpus, mixedDoc);
        
        System.out.println("Mixed Special Chars ACTUAL: " + result);
        
        // BUG: Should extract valid words (Hello, World, This, Is, Test)
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Should extract words from mixed content, not return NaN");
        assertTrue(result >= 0, "Valid words should produce non-negative score");
    }
    
    @Test
    @DisplayName("Negative: Very large document - WILL FAIL (BUG)")
    void testTFIDF_Negative_VeryLargeDocument() {
        List<String> corpus = Arrays.asList("small", "corpus");
        
        StringBuilder largeDoc = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeDoc.append("word ");
        }
        
        double result = businessObj.performTFIDF(corpus, largeDoc.toString());
        
        System.out.println("Large Document ACTUAL: " + result);
        
        // BUG: Should handle large documents
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Large documents should be processable, not return NaN");
        assertTrue(Double.isFinite(result), "Should return finite value");
    }
    
    @Test
    @DisplayName("Negative: Single character")
    void testTFIDF_Negative_SingleCharacter() {
        List<String> corpus = Arrays.asList("documents", "with", "words");
        String singleCharDoc = "a";
        
        double result = businessObj.performTFIDF(corpus, singleCharDoc);
        double expected = -0.288;
        
        System.out.println("Single Character ACTUAL: " + result);
        
        assertEquals(expected, result, DELTA, "Single char should return -0.288");
    }
    
    @Test
    @DisplayName("Negative: Null document throws exception")
    void testTFIDF_Negative_NullDocument() {
        List<String> corpus = Arrays.asList("doc1", "doc2");
        
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            businessObj.performTFIDF(corpus, null);
        });
        
        System.out.println("Null document threw: " + exception.getMessage());
        
        assertNotNull(exception.getMessage(), "Exception should have message");
    }
    
    @Test
    @DisplayName("Negative: Null corpus throws exception")
    void testTFIDF_Negative_NullCorpus() {
        String document = "test document";
        
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            businessObj.performTFIDF(null, document);
        });
        
        System.out.println("Null corpus threw: " + exception.getMessage());
        
        assertNotNull(exception.getMessage(), "Exception should have message");
    }
    
    // ================== BOUNDARY TESTS ==================
    
    @Test
    @DisplayName("Boundary: Single document corpus - WILL FAIL (BUG)")
    void testTFIDF_Boundary_SingleDocCorpus() {
        List<String> corpus = Arrays.asList("only one document");
        String document = "testing single corpus";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Single Doc Corpus ACTUAL: " + result);
        
        // BUG: Should work with single document corpus
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Single document corpus should work, not return NaN");
        assertTrue(Double.isFinite(result), "Should return finite value");
    }
    
    @Test
    @DisplayName("Boundary: Identical to corpus - WILL FAIL (BUG)")
    void testTFIDF_Boundary_IdenticalToCorpus() {
        String text = "exact same text";
        List<String> corpus = Arrays.asList(text, text, text);
        String document = text;
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Identical to Corpus ACTUAL: " + result);
        
        // BUG: Identical docs cause IDF division by zero
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Identical document causes division by zero in IDF -> NaN");
        assertTrue(Double.isFinite(result), "Should handle identical documents");
    }
    
    @Test
    @DisplayName("Boundary: Corpus with empty strings - WILL FAIL (BUG)")
    void testTFIDF_Boundary_CorpusWithEmptyStrings() {
        List<String> corpus = Arrays.asList("", "document", "");
        String document = "test document";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Corpus with Empty Strings ACTUAL: " + result);
        
        // BUG: Should filter empty entries from corpus
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Should filter empty corpus entries, not return NaN");
        assertTrue(Double.isFinite(result), "Should handle corpus with empty strings");
    }
    
    @Test
    @DisplayName("Boundary: Minimal corpus (2 docs) - WILL FAIL (BUG)")
    void testTFIDF_Boundary_MinimalCorpus() {
        List<String> corpus = Arrays.asList("first doc", "second doc");
        String document = "test document with words";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Minimal Corpus ACTUAL: " + result);
        
        // BUG: Small corpus should work
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Minimal corpus should be processable, not return NaN");
        assertTrue(Double.isFinite(result), "Should work with 2-document corpus");
    }
    
    @Test
    @DisplayName("Boundary: Unicode characters - WILL FAIL (BUG)")
    void testTFIDF_Boundary_UnicodeCharacters() {
        List<String> corpus = Arrays.asList(
            "emoji test 😀",
            "special chars ñ ü",
            "symbols © ® ™"
        );
        
        String document = "testing unicode 🎉 ñ ©";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Unicode Characters ACTUAL: " + result);
        
        // BUG: Should handle Unicode
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Unicode characters should be handled, not return NaN");
        assertTrue(Double.isFinite(result), "Should process Unicode text");
    }
    
    @Test
    @DisplayName("Boundary: Irregular whitespace - WILL FAIL (BUG)")
    void testTFIDF_Boundary_IrregularWhitespace() {
        List<String> corpus = Arrays.asList(
            "normal spacing",
            "regular  spacing",
            "standard spacing"
        );
        
        String document = "word1\t\tword2    word3\nword4";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Irregular Whitespace ACTUAL: " + result);
        
        // BUG: Should normalize whitespace
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Irregular whitespace should be normalized, not return NaN");
        assertTrue(result >= 0, "Should extract words from irregular whitespace");
    }
    
    @Test
    @DisplayName("Boundary: Very long word")
    void testTFIDF_Boundary_VeryLongWord() {
        List<String> corpus = Arrays.asList("short", "words", "here");
        String document = "supercalifragilisticexpialidocious";
        
        double result = businessObj.performTFIDF(corpus, document);
        double expected = -0.288;
        
        System.out.println("Very Long Word ACTUAL: " + result);
        
        assertEquals(expected, result, DELTA, "Very long word should return -0.288");
    }
    
    // ================== EDGE CASES ==================
    
    @Test
    @DisplayName("Edge: Only stop words - WILL FAIL (BUG)")
    void testTFIDF_Edge_OnlyStopWords() {
        List<String> corpus = Arrays.asList(
            "the and or but",
            "is was are were",
            "a an the"
        );
        
        String document = "the and the or the";
        
        double result = businessObj.performTFIDF(corpus, document);
        
        System.out.println("Only Stop Words ACTUAL: " + result);
        
        // BUG: Stop words should be processable
        assertFalse(Double.isNaN(result), 
                   "BUG FOUND: Stop words should be handled, not return NaN");
        assertTrue(Double.isFinite(result), "Should return finite value for stop words");
    }
}
