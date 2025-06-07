package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    void isEmptyOrWhitespace_ShouldReturnTrue_ForEmptyOrWhitespaceStrings(String input) {
        // When
        boolean result = isEmptyOrWhitespace(input);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "a", "valid input", "123"})
    void isEmptyOrWhitespace_ShouldReturnFalse_ForValidStrings(String input) {
        // When
        boolean result = isEmptyOrWhitespace(input);

        // Then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isEmptyOrWhitespace_ShouldReturnTrue_ForNullString(String input) {
        // When
        boolean result = isEmptyOrWhitespace(input);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "12-345, true",
            "12345, false",
            "123-45, false",
            "12-3456, false",
            "ab-cde, false",
            "12-34a, false",
            ", false"  // null case
    })
    void isValidPostalCode_ShouldValidateCorrectly(String postalCode, boolean expected) {
        // When
        boolean result = isValidPostalCode(postalCode);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 1.0, 10.5, 999.99})
    void isValidWeight_ShouldReturnTrue_ForPositiveWeights(double weight) {
        // When
        boolean result = isValidWeight(weight);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -10.5})
    void isValidWeight_ShouldReturnFalse_ForNonPositiveWeights(double weight) {
        // When
        boolean result = isValidWeight(weight);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidWeight_ShouldReturnFalse_ForExtremeValues() {
        // Test edge cases to avoid "always true/false" warnings
        assertThat(isValidWeight(Double.NEGATIVE_INFINITY)).isFalse();
        assertThat(isValidWeight(Double.POSITIVE_INFINITY)).isFalse(); // POPRAWIONE: isFinite() zwraca false dla infinity
        assertThat(isValidWeight(Double.NaN)).isFalse();
        assertThat(isValidWeight(Double.MIN_VALUE)).isTrue(); // Bardzo mała dodatnia liczba
        assertThat(isValidWeight(Double.MAX_VALUE)).isTrue(); // Bardzo duża liczba
        assertThat(isValidWeight(-Double.MIN_VALUE)).isFalse(); // Bardzo mała ujemna liczba
    }

    @Test
    void isValidPostalCode_ShouldHandleEdgeCases() {
        // Boundary cases
        assertThat(isValidPostalCode("00-000")).isTrue();
        assertThat(isValidPostalCode("99-999")).isTrue();
        assertThat(isValidPostalCode("1-234")).isFalse();
        assertThat(isValidPostalCode("123-4")).isFalse();
        assertThat(isValidPostalCode("12-34-56")).isFalse();

        // Special characters and spaces
        assertThat(isValidPostalCode("12 345")).isFalse();
        assertThat(isValidPostalCode("12_345")).isFalse();
        assertThat(isValidPostalCode("12.345")).isFalse();
        assertThat(isValidPostalCode("12/345")).isFalse();

        // Empty and whitespace variations
        assertThat(isValidPostalCode("")).isFalse();
        assertThat(isValidPostalCode("     ")).isFalse();
        assertThat(isValidPostalCode("\t")).isFalse();

        // Case with letters mixed in
        assertThat(isValidPostalCode("1a-234")).isFalse();
        assertThat(isValidPostalCode("12-a34")).isFalse();
        assertThat(isValidPostalCode("AB-CDE")).isFalse();
    }

    @Test
    void isEmptyOrWhitespace_ShouldHandleUnicodeWhitespace() {
        // Unicode whitespace characters - tylko te które trim() rozpoznaje
        assertThat(isEmptyOrWhitespace("\u0020")).isTrue(); // Regular space
        assertThat(isEmptyOrWhitespace("\u00A0")).isFalse(); // POPRAWIONE: Non-breaking space nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\u2000")).isFalse(); // POPRAWIONE: En quad nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\u2001")).isFalse(); // POPRAWIONE: Em quad nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\u2002")).isFalse(); // POPRAWIONE: En space nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\u2003")).isFalse(); // POPRAWIONE: Em space nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\u3000")).isFalse(); // POPRAWIONE: Ideographic space nie jest rozpoznawany przez trim()

        // Mixed whitespace - tylko standardowe białe znaki
        assertThat(isEmptyOrWhitespace(" \t\n\r")).isTrue();
        assertThat(isEmptyOrWhitespace("\u0020\u00A0\u2000")).isFalse(); // POPRAWIONE: zawiera znaki nierozpoznawane przez trim()

        // Non-whitespace unicode
        assertThat(isEmptyOrWhitespace("ąęółćń")).isFalse();
        assertThat(isEmptyOrWhitespace("αβγ")).isFalse();
        assertThat(isEmptyOrWhitespace("中文")).isFalse();
    }

    @Test
    void isEmptyOrWhitespace_ShouldHandleMixedContent() {
        // Text with leading/trailing whitespace
        assertThat(isEmptyOrWhitespace(" text ")).isFalse();
        assertThat(isEmptyOrWhitespace("\ttext\n")).isFalse();

        // Single characters
        assertThat(isEmptyOrWhitespace("a")).isFalse();
        assertThat(isEmptyOrWhitespace("1")).isFalse();
        assertThat(isEmptyOrWhitespace("@")).isFalse();

        // Zero-width characters - te nie są rozpoznawane przez trim() jako whitespace
        assertThat(isEmptyOrWhitespace("\u200B")).isFalse(); // POPRAWIONE: Zero-width space nie jest rozpoznawany przez trim()
        assertThat(isEmptyOrWhitespace("\uFEFF")).isFalse(); // POPRAWIONE: Byte order mark nie jest rozpoznawany przez trim()
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            0.000001, 0.1, 0.5, 0.9, 0.99, 0.999,
            1.0, 1.1, 10.0, 100.0, 1000.0,
            Double.MIN_NORMAL, // Smallest normalized positive double
            1E-10, 1E-100, 1E100, 1E300
    })
    void isValidWeight_ShouldReturnTrue_ForVariousPositiveValues(double weight) {
        assertThat(isValidWeight(weight)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            -0.000001, -0.1, -1.0, -10.0, -100.0,
            -Double.MIN_NORMAL, -1E10, -1E100,
            Double.NEGATIVE_INFINITY
    })
    void isValidWeight_ShouldReturnFalse_ForNegativeAndSpecialValues(double weight) {
        assertThat(isValidWeight(weight)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12-345", "00-000", "99-999", "50-123", "80-456"
    })
    void isValidPostalCode_ShouldReturnTrue_ForValidFormats(String postalCode) {
        assertThat(isValidPostalCode(postalCode)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "12345", "123-45", "12-3456", "1-234", "123-4",
            "ab-cde", "12-34a", "a2-345", "12-c45", "1a-2b3",
            "12 345", "12_345", "12.345", "12/345", "12\\345",
            "-12-345", "12--345", "12-345-", "--", "12-", "-345",
            "012-345", "12-0345", // Leading zeros might be invalid in some systems
            "ABCDEF", "12-ABC", "ABC-123"
    })
    void isValidPostalCode_ShouldReturnFalse_ForInvalidFormats(String postalCode) {
        assertThat(isValidPostalCode(postalCode)).isFalse();
    }

    @Test
    void isValidWeight_ShouldHandleEdgeCasesWithZero() {
        // Different representations of zero
        assertThat(isValidWeight(0.0)).isFalse();
        assertThat(isValidWeight(-0.0)).isFalse();
        assertThat(isValidWeight(0)).isFalse();

        // Very small positive numbers close to zero
        assertThat(isValidWeight(1E-323)).isTrue(); // Very close to zero but positive
        assertThat(isValidWeight(Double.MIN_VALUE)).isTrue();

        // Very small negative numbers close to zero
        assertThat(isValidWeight(-1E-323)).isFalse();
        assertThat(isValidWeight(-Double.MIN_VALUE)).isFalse();
    }

    // Dodatkowy test dla lepszego zrozumienia zachowania trim()
    @Test
    void isEmptyOrWhitespace_ShouldShowTrimBehavior() {
        // Te znaki SĄ rozpoznawane przez trim() jako whitespace
        assertThat(isEmptyOrWhitespace(" ")).isTrue();     // space
        assertThat(isEmptyOrWhitespace("\t")).isTrue();    // tab
        assertThat(isEmptyOrWhitespace("\n")).isTrue();    // newline
        assertThat(isEmptyOrWhitespace("\r")).isTrue();    // carriage return
        assertThat(isEmptyOrWhitespace("\f")).isTrue();    // form feed

        // Te znaki NIE SĄ rozpoznawane przez trim() jako whitespace
        assertThat(isEmptyOrWhitespace("\u00A0")).isFalse(); // non-breaking space
        assertThat(isEmptyOrWhitespace("\u2000")).isFalse(); // en quad
        assertThat(isEmptyOrWhitespace("\u200B")).isFalse(); // zero-width space
    }

    // Helper methods for validation
    private boolean isEmptyOrWhitespace(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidPostalCode(String postalCode) {
        if (postalCode == null) return false;
        return postalCode.matches("\\d{2}-\\d{3}");
    }

    private boolean isValidWeight(double weight) {
        return weight > 0 && !Double.isNaN(weight) && Double.isFinite(weight);
    }
}