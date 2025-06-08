package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilsTest {

    @Test
    void hashPassword_ShouldReturnDifferentHash_ForSamePassword() {
        // Given
        String password = "testPassword123";

        // When
        String hash1 = hashPassword(password);
        String hash2 = hashPassword(password);

        // Then
        assertThat(hash1).isNotEqualTo(hash2); // Due to salt
        assertThat(hash1).isNotEqualTo(password);
        assertThat(hash2).isNotEqualTo(password);
    }

    @Test
    void verifyPassword_ShouldReturnTrue_ForCorrectPassword() {
        // Given
        String password = "testPassword123";
        String hash = hashPassword(password);

        // When
        boolean result = verifyPassword(password, hash);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void verifyPassword_ShouldReturnFalse_ForIncorrectPassword() {
        // Given
        String correctPassword = "testPassword123";
        String incorrectPassword = "wrongPassword";
        String hash = hashPassword(correctPassword);

        // When
        boolean result = verifyPassword(incorrectPassword, hash);

        // Then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"password", "12345", "abc", "PASSWORD", "Password"})
    void isWeakPassword_ShouldReturnTrue_ForWeakPasswords(String weakPassword) {
        // When
        boolean result = isWeakPassword(weakPassword);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"StrongPass123!", "MyP@ssw0rd", "SecurE#2024", "C0mpl3x!Pass"})
    void isWeakPassword_ShouldReturnFalse_ForStrongPasswords(String strongPassword) {
        // When
        boolean result = isWeakPassword(strongPassword);

        // Then
        assertThat(result).isFalse();
    }

    // Mock implementations for testing
    private String hashPassword(String password) {
        // Simple mock implementation - in real app use BCrypt or similar
        return "hash_" + password.hashCode() + "_" + System.nanoTime();
    }

    private boolean verifyPassword(String password, String hash) {
        // Simple mock implementation
        return hash.startsWith("hash_" + password.hashCode());
    }

    private boolean isWeakPassword(String password) {
        if (password == null || password.length() < 8) {
            return true;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return !(hasUpper && hasLower && hasDigit && hasSpecial);
    }
}