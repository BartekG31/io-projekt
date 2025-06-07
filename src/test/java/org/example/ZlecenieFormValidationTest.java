package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ZlecenieFormValidationTest {

    @Test
    void validateZlecenieData_ShouldReturnTrue_ForValidData() {
        // Given
        ZlecenieData validData = new ZlecenieData(
                "Jan Kowalski",
                "ul. Główna 123",
                "Warszawa",
                "00-001",
                "Dokumenty",
                2.5,
                "2024-01-15"
        );

        // When
        boolean result = validateZlecenieData(validData);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidZlecenieDataProvider")
    void validateZlecenieData_ShouldReturnFalse_ForInvalidData(ZlecenieData data, String description) {
        // When
        boolean result = validateZlecenieData(data);

        // Then
        assertThat(result).as(description).isFalse();
    }

    private static Stream<Arguments> invalidZlecenieDataProvider() {
        return Stream.of(
                Arguments.of(
                        new ZlecenieData("", "ul. Główna 123", "Warszawa", "00-001", "Dokumenty", 2.5, "2024-01-15"),
                        "Empty recipient name"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "", "Warszawa", "00-001", "Dokumenty", 2.5, "2024-01-15"),
                        "Empty address"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "", "00-001", "Dokumenty", 2.5, "2024-01-15"),
                        "Empty city"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "Warszawa", "invalid", "Dokumenty", 2.5, "2024-01-15"),
                        "Invalid postal code"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "Warszawa", "00-001", "", 2.5, "2024-01-15"),
                        "Empty description"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "Warszawa", "00-001", "Dokumenty", 0, "2024-01-15"),
                        "Zero weight"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "Warszawa", "00-001", "Dokumenty", -1, "2024-01-15"),
                        "Negative weight"
                ),
                Arguments.of(
                        new ZlecenieData("Jan Kowalski", "ul. Główna 123", "Warszawa", "00-001", "Dokumenty", 2.5, "invalid-date"),
                        "Invalid date format"
                )
        );
    }

    // Helper classes and methods
    private boolean validateZlecenieData(ZlecenieData data) {
        return hasValue(data.odbiorca) &&
                hasValue(data.adres) &&
                hasValue(data.miasto) &&
                isValidPostalCode(data.kodPocztowy) &&
                hasValue(data.opis) &&
                data.waga > 0 &&
                isValidDate(data.dataNadania);
    }

    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private boolean isValidPostalCode(String postalCode) {
        return postalCode != null && postalCode.matches("\\d{2}-\\d{3}");
    }

    private boolean isValidDate(String date) {
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static class ZlecenieData {
        final String odbiorca;
        final String adres;
        final String miasto;
        final String kodPocztowy;
        final String opis;
        final double waga;
        final String dataNadania;

        ZlecenieData(String odbiorca, String adres, String miasto, String kodPocztowy,
                     String opis, double waga, String dataNadania) {
            this.odbiorca = odbiorca;
            this.adres = adres;
            this.miasto = miasto;
            this.kodPocztowy = kodPocztowy;
            this.opis = opis;
            this.waga = waga;
            this.dataNadania = dataNadania;
        }
    }
}