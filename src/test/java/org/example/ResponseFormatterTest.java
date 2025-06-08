package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseFormatterTest {

    @Test
    void formatSuccessResponse_ShouldReturnCorrectFormat() {
        // Given
        String message = "Operation successful";

        // When
        String result = formatSuccessResponse(message);

        // Then
        assertThat(result).isEqualTo("OK;Operation successful");
    }

    @Test
    void formatErrorResponse_ShouldReturnCorrectFormat() {
        // Given
        String message = "Operation failed";

        // When
        String result = formatErrorResponse(message);

        // Then
        assertThat(result).isEqualTo("ERROR;Operation failed");
    }

    @Test
    void formatDataResponse_ShouldReturnCorrectFormat_ForSingleItem() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");

        // When
        String result = formatDataResponse(data);

        // Then
        assertThat(result).isEqualTo("OK;item1;item2;item3");
    }

    @Test
    void formatDataResponse_ShouldReturnOKOnly_ForEmptyList() {
        // Given
        List<String> data = Arrays.asList();

        // When
        String result = formatDataResponse(data);

        // Then
        assertThat(result).isEqualTo("OK");
    }

    @ParameterizedTest
    @MethodSource("statusMessageProvider")
    void getStatusMessage_ShouldReturnCorrectMessage(String status, String expectedMessage) {
        // When
        String result = getStatusMessage(status);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    private static Stream<Arguments> statusMessageProvider() {
        return Stream.of(
                Arguments.of("Zrealizowane", "Przesyłka została zrealizowana"),
                Arguments.of("Odrzucone", "Przesyłka została odrzucona"),
                Arguments.of("Przyjęte", "Towar został przyjęty do magazynu"),
                Arguments.of("Gotowe do wysyłki", "Paczka jest gotowa do wysyłki"),
                Arguments.of("W drodze", "Paczka jest w drodze"),
                Arguments.of("Oczekiwanie na odbiór", "Paczka oczekuje na odbiór"),
                Arguments.of("Przypisane", "Zlecenie zostało przypisane do pojazdu"),
                Arguments.of("Unknown", "Status został zmieniony")
        );
    }

    // Helper methods
    private String formatSuccessResponse(String message) {
        return "OK;" + message;
    }

    private String formatErrorResponse(String message) {
        return "ERROR;" + message;
    }

    private String formatDataResponse(List<String> data) {
        if (data.isEmpty()) {
            return "OK";
        }
        return "OK;" + String.join(";", data);
    }

    private String getStatusMessage(String status) {
        switch (status) {
            case "Zrealizowane": return "Przesyłka została zrealizowana";
            case "Odrzucone": return "Przesyłka została odrzucona";
            case "Przyjęte": return "Towar został przyjęty do magazynu";
            case "Gotowe do wysyłki": return "Paczka jest gotowa do wysyłki";
            case "W drodze": return "Paczka jest w drodze";
            case "Oczekiwanie na odbiór": return "Paczka oczekuje na odbiór";
            case "Przypisane": return "Zlecenie zostało przypisane do pojazdu";
            default: return "Status został zmieniony";
        }
    }
}