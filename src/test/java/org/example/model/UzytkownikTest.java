package org.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

public class UzytkownikTest {

    @Test
    @DisplayName("Powinien utworzyć pustego użytkownika")
    void shouldCreateEmptyUser() {
        // When
        Uzytkownik uzytkownik = new Uzytkownik();

        // Then
        assertThat(uzytkownik.getId()).isEqualTo(0);
        assertThat(uzytkownik.getImie()).isNull();
        assertThat(uzytkownik.getNazwisko()).isNull();
        assertThat(uzytkownik.getLogin()).isNull();
        assertThat(uzytkownik.getHaslo()).isNull();
        assertThat(uzytkownik.getRola()).isNull();
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać ID")
    void shouldCorrectlySetAndGetId() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        int expectedId = 123;

        // When
        uzytkownik.setId(expectedId);

        // Then
        assertThat(uzytkownik.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać imię")
    void shouldCorrectlySetAndGetImie() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String expectedImie = "Jan";

        // When
        uzytkownik.setImie(expectedImie);

        // Then
        assertThat(uzytkownik.getImie()).isEqualTo(expectedImie);
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać nazwisko")
    void shouldCorrectlySetAndGetNazwisko() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String expectedNazwisko = "Kowalski";

        // When
        uzytkownik.setNazwisko(expectedNazwisko);

        // Then
        assertThat(uzytkownik.getNazwisko()).isEqualTo(expectedNazwisko);
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać login")
    void shouldCorrectlySetAndGetLogin() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String expectedLogin = "jkowalski";

        // When
        uzytkownik.setLogin(expectedLogin);

        // Then
        assertThat(uzytkownik.getLogin()).isEqualTo(expectedLogin);
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać hasło")
    void shouldCorrectlySetAndGetHaslo() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String expectedHaslo = "securePassword123";

        // When
        uzytkownik.setHaslo(expectedHaslo);

        // Then
        assertThat(uzytkownik.getHaslo()).isEqualTo(expectedHaslo);
    }

    @Test
    @DisplayName("Powinien prawidłowo ustawić i pobrać rolę")
    void shouldCorrectlySetAndGetRola() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String expectedRola = "KLIENT";

        // When
        uzytkownik.setRola(expectedRola);

        // Then
        assertThat(uzytkownik.getRola()).isEqualTo(expectedRola);
    }

    @ParameterizedTest
    @DisplayName("Powinien obsłużyć różne typy ról")
    @ValueSource(strings = {"KLIENT", "MAGAZYNIER", "KURIER", "LOGISTYK", "ADMIN"})
    void shouldHandleDifferentRoles(String rola) {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When
        uzytkownik.setRola(rola);

        // Then
        assertThat(uzytkownik.getRola()).isEqualTo(rola);
    }

    @ParameterizedTest
    @DisplayName("Powinien obsłużyć różne formaty imion i nazwisk")
    @CsvSource({
            "Jan, Kowalski",
            "Anna-Maria, Nowak-Kowalska",
            "Józef, O'Connor",
            "Marie, D'Artagnan",
            "李, 小明"
    })
    void shouldHandleDifferentNameFormats(String imie, String nazwisko) {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When
        uzytkownik.setImie(imie);
        uzytkownik.setNazwisko(nazwisko);

        // Then
        assertThat(uzytkownik.getImie()).isEqualTo(imie);
        assertThat(uzytkownik.getNazwisko()).isEqualTo(nazwisko);
    }

    @Test
    @DisplayName("Powinien obsłużyć wartości null")
    void shouldHandleNullValues() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When
        uzytkownik.setImie(null);
        uzytkownik.setNazwisko(null);
        uzytkownik.setLogin(null);
        uzytkownik.setHaslo(null);
        uzytkownik.setRola(null);

        // Then
        assertThat(uzytkownik.getImie()).isNull();
        assertThat(uzytkownik.getNazwisko()).isNull();
        assertThat(uzytkownik.getLogin()).isNull();
        assertThat(uzytkownik.getHaslo()).isNull();
        assertThat(uzytkownik.getRola()).isNull();
    }

    @Test
    @DisplayName("Powinien obsłużyć puste stringi")
    void shouldHandleEmptyStrings() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When
        uzytkownik.setImie("");
        uzytkownik.setNazwisko("");
        uzytkownik.setLogin("");
        uzytkownik.setHaslo("");
        uzytkownik.setRola("");

        // Then
        assertThat(uzytkownik.getImie()).isEmpty();
        assertThat(uzytkownik.getNazwisko()).isEmpty();
        assertThat(uzytkownik.getLogin()).isEmpty();
        assertThat(uzytkownik.getHaslo()).isEmpty();
        assertThat(uzytkownik.getRola()).isEmpty();
    }

    @Test
    @DisplayName("Powinien obsłużyć graniczne wartości ID")
    void shouldHandleBoundaryIdValues() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When & Then
        uzytkownik.setId(0);
        assertThat(uzytkownik.getId()).isEqualTo(0);

        uzytkownik.setId(1);
        assertThat(uzytkownik.getId()).isEqualTo(1);

        uzytkownik.setId(Integer.MAX_VALUE);
        assertThat(uzytkownik.getId()).isEqualTo(Integer.MAX_VALUE);

        uzytkownik.setId(-1);
        assertThat(uzytkownik.getId()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Powinien obsłużyć długie stringi")
    void shouldHandleLongStrings() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String longString = "a".repeat(1000);

        // When
        uzytkownik.setImie(longString);
        uzytkownik.setNazwisko(longString);
        uzytkownik.setLogin(longString);
        uzytkownik.setHaslo(longString);
        uzytkownik.setRola(longString);

        // Then
        assertThat(uzytkownik.getImie()).hasSize(1000);
        assertThat(uzytkownik.getNazwisko()).hasSize(1000);
        assertThat(uzytkownik.getLogin()).hasSize(1000);
        assertThat(uzytkownik.getHaslo()).hasSize(1000);
        assertThat(uzytkownik.getRola()).hasSize(1000);
    }

    @Test
    @DisplayName("Powinien utworzyć kompletnego użytkownika")
    void shouldCreateCompleteUser() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();

        // When
        uzytkownik.setId(1);
        uzytkownik.setImie("Jan");
        uzytkownik.setNazwisko("Kowalski");
        uzytkownik.setLogin("jkowalski");
        uzytkownik.setHaslo("haslo123");
        uzytkownik.setRola("KLIENT");

        // Then
        assertThat(uzytkownik.getId()).isEqualTo(1);
        assertThat(uzytkownik.getImie()).isEqualTo("Jan");
        assertThat(uzytkownik.getNazwisko()).isEqualTo("Kowalski");
        assertThat(uzytkownik.getLogin()).isEqualTo("jkowalski");
        assertThat(uzytkownik.getHaslo()).isEqualTo("haslo123");
        assertThat(uzytkownik.getRola()).isEqualTo("KLIENT");
    }

    @Test
    @DisplayName("Powinien obsłużyć znaki specjalne w polach tekstowych")
    void shouldHandleSpecialCharactersInTextFields() {
        // Given
        Uzytkownik uzytkownik = new Uzytkownik();
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        uzytkownik.setImie("Jan" + specialChars);
        uzytkownik.setLogin("user@domain.com");
        uzytkownik.setHaslo("P@ssw0rd!");

        // Then
        assertThat(uzytkownik.getImie()).contains(specialChars);
        assertThat(uzytkownik.getLogin()).isEqualTo("user@domain.com");
        assertThat(uzytkownik.getHaslo()).isEqualTo("P@ssw0rd!");
    }
}