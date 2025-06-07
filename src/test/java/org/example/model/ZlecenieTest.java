package org.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ZlecenieTest {

    @Test
    void constructor_ShouldCreateZlecenieWithAllFields() {
        // Given
        int id = 1;
        int nadawcaId = 2;
        int odbiorcaId = 3;
        int pojazdId = 4;
        String status = "Nowe";
        Date dataUtworzenia = Date.valueOf(LocalDate.now());

        // When
        Zlecenie zlecenie = new Zlecenie(id, nadawcaId, odbiorcaId, pojazdId, status, dataUtworzenia);

        // Then
        assertThat(zlecenie.getId()).isEqualTo(id);
        assertThat(zlecenie.getNadawcaId()).isEqualTo(nadawcaId);
        assertThat(zlecenie.getOdbiorcaId()).isEqualTo(odbiorcaId);
        assertThat(zlecenie.getPojazdId()).isEqualTo(pojazdId);
        assertThat(zlecenie.getStatus()).isEqualTo(status);
        assertThat(zlecenie.getDataUtworzenia()).isEqualTo(dataUtworzenia);
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyZlecenie() {
        // When
        Zlecenie zlecenie = new Zlecenie();

        // Then
        assertThat(zlecenie.getId()).isEqualTo(0);
        assertThat(zlecenie.getNadawcaId()).isEqualTo(0);
        assertThat(zlecenie.getOdbiorcaId()).isEqualTo(0);
        assertThat(zlecenie.getPojazdId()).isEqualTo(0);
        assertThat(zlecenie.getStatus()).isNull();
        assertThat(zlecenie.getDataUtworzenia()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Nowe", "Przyjęte", "W drodze", "Zrealizowane", "Odrzucone"})
    void setStatus_ShouldSetCorrectStatus(String status) {
        // Given
        Zlecenie zlecenie = new Zlecenie();

        // When
        zlecenie.setStatus(status);

        // Then
        assertThat(zlecenie.getStatus()).isEqualTo(status);
    }

    @Test
    void setId_ShouldSetCorrectId() {
        // Given
        Zlecenie zlecenie = new Zlecenie();
        int expectedId = 123;

        // When
        zlecenie.setId(expectedId);

        // Then
        assertThat(zlecenie.getId()).isEqualTo(expectedId);
    }

    @Test
    void setDataUtworzenia_ShouldSetCorrectDate() {
        // Given
        Zlecenie zlecenie = new Zlecenie();
        Date expectedDate = Date.valueOf(LocalDate.of(2024, 1, 15));

        // When
        zlecenie.setDataUtworzenia(expectedDate);

        // Then
        assertThat(zlecenie.getDataUtworzenia()).isEqualTo(expectedDate);
    }
}