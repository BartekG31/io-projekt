package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTransitionTest {

    @ParameterizedTest
    @CsvSource({
            "Nowe, Przyjęte, true",
            "Przyjęte, Gotowe do wysyłki, true",
            "Gotowe do wysyłki, W drodze, true",
            "W drodze, Oczekiwanie na odbiór, true",
            "Oczekiwanie na odbiór, Zrealizowane, true",
            "Oczekiwanie na odbiór, Odrzucone, true",
            "Zrealizowane, W drodze, false",
            "Odrzucone, Przyjęte, false",
            "Nowe, Zrealizowane, false"
    })
    void isValidStatusTransition_ShouldReturnCorrectResult(String fromStatus, String toStatus, boolean expected) {
        // When
        boolean result = isValidStatusTransition(fromStatus, toStatus);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private boolean isValidStatusTransition(String fromStatus, String toStatus) {

        return switch (fromStatus) {
            case "Nowe" -> "Przyjęte".equals(toStatus) || "Odrzucone".equals(toStatus);
            case "Przyjęte" -> "Gotowe do wysyłki".equals(toStatus);
            case "Gotowe do wysyłki" -> "W drodze".equals(toStatus);
            case "W drodze" -> "Oczekiwanie na odbiór".equals(toStatus);
            case "Oczekiwanie na odbiór" -> "Zrealizowane".equals(toStatus) || "Odrzucone".equals(toStatus);
            case "Zrealizowane", "Odrzucone" -> false;
            default -> false;
        };
    }
}