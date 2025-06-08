package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandParserTest {

    @ParameterizedTest
    @CsvSource({
            "LOGIN;testuser;password, LOGIN",
            "DODAJ_ZLECENIE;1;Jan;ul. Główna;Warszawa, DODAJ_ZLECENIE",
            "POBIERZ_DO_ODBIORU;Jan Kowalski, POBIERZ_DO_ODBIORU",
            "ZMIEN_STATUS;1;Przyjęte, ZMIEN_STATUS"
    })
    void parseCommand_ShouldExtractCorrectCommandType(String command, String expectedCommand) {
        // When
        String result = parseCommandType(command);

        // Then
        assertThat(result).isEqualTo(expectedCommand);
    }

    @Test
    void parseCommand_ShouldThrowException_ForInvalidCommand() {
        // Given
        String invalidCommand = "";

        // When & Then
        assertThatThrownBy(() -> parseCommandType(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseParameters_ShouldSplitCorrectly() {
        // Given
        String command = "LOGIN;testuser;password";

        // When
        String[] parameters = parseParameters(command);

        // Then
        assertThat(parameters).hasSize(3);
        assertThat(parameters[0]).isEqualTo("LOGIN");
        assertThat(parameters[1]).isEqualTo("testuser");
        assertThat(parameters[2]).isEqualTo("password");
    }

    @Test
    void validateParameterCount_ShouldReturnTrue_ForCorrectCount() {
        // Given
        String[] parameters = {"LOGIN", "user", "pass"};
        int expectedCount = 3;

        // When
        boolean result = validateParameterCount(parameters, expectedCount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateParameterCount_ShouldReturnFalse_ForIncorrectCount() {
        // Given
        String[] parameters = {"LOGIN", "user"};
        int expectedCount = 3;

        // When
        boolean result = validateParameterCount(parameters, expectedCount);

        // Then
        assertThat(result).isFalse();
    }

    // Helper methods
    private String parseCommandType(String command) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }
        return command.split(";")[0];
    }

    private String[] parseParameters(String command) {
        return command.split(";");
    }

    private boolean validateParameterCount(String[] parameters, int expectedCount) {
        return parameters.length == expectedCount;
    }
}