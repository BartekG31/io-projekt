package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilsTest {

    @Test
    void parseDate_ShouldReturnCorrectDate_ForValidString() {
        // Given
        String dateString = "2024-01-15";

        // When
        Date result = parseDate(dateString);

        // Then
        assertThat(result).isEqualTo(Date.valueOf(LocalDate.of(2024, 1, 15)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-date", "2024/01/15", "15-01-2024", "2024-13-01", "2024-01-32"})
    void parseDate_ShouldThrowException_ForInvalidString(String invalidDate) {
        // When & Then
        assertThatThrownBy(() -> parseDate(invalidDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatDate_ShouldReturnCorrectString_ForValidDate() {
        // Given
        Date date = Date.valueOf(LocalDate.of(2024, 1, 15));

        // When
        String result = formatDate(date);

        // Then
        assertThat(result).isEqualTo("2024-01-15");
    }

    @Test
    void isDateInFuture_ShouldReturnTrue_ForFutureDate() {
        // Given
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(1));

        // When
        boolean result = isDateInFuture(futureDate);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isDateInFuture_ShouldReturnFalse_ForPastDate() {
        // Given
        Date pastDate = Date.valueOf(LocalDate.now().minusDays(1));

        // When
        boolean result = isDateInFuture(pastDate);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isDateInFuture_ShouldReturnFalse_ForToday() {
        // Given
        Date today = Date.valueOf(LocalDate.now());

        // When
        boolean result = isDateInFuture(today);

        // Then
        assertThat(result).isFalse();
    }

    // Helper methods
    private Date parseDate(String dateString) {
        try {
            return Date.valueOf(LocalDate.parse(dateString));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString, e);
        }
    }

    private String formatDate(Date date) {
        return date.toLocalDate().toString();
    }

    private boolean isDateInFuture(Date date) {
        return date.toLocalDate().isAfter(LocalDate.now());
    }
}
