package com.example.weill.norms;

import com.example.weill.user.WeillUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeillPsychometricEngineTests {

    private final WeillPsychometricEngine engine = new WeillPsychometricEngine(
        new PopulationClassifier(),
        new WeillNormTable(),
        new PercentileIqTable()
    );

    @Test
    void calculatesPercentileIqAndInterpretationForAChild() {
        PsychometricResult result = engine.calculate(10, true, "Primaria", 42);

        assertThat(result.populationCategory()).isEqualTo(PopulationCategory.CHILD);
        assertThat(result.normGroup()).isEqualTo(NormGroup.AGE_10);
        assertThat(result.percentile()).isEqualTo(80);
        assertThat(result.intelligenceQuotient()).isEqualTo(113);
        assertThat(result.interpretation()).isEqualTo(IntelligenceInterpretation.AVERAGE);
    }

    @Test
    void calculatesPercentileIqAndInterpretationForAUniversityStudent() {
        PsychometricResult result = engine.calculate(19, true, "Universidad", 57);

        assertThat(result.populationCategory()).isEqualTo(PopulationCategory.UNIVERSITY_STUDENT);
        assertThat(result.normGroup()).isEqualTo(NormGroup.UNIVERSITY);
        assertThat(result.percentile()).isEqualTo(90);
        assertThat(result.intelligenceQuotient()).isEqualTo(119);
        assertThat(result.interpretation()).isEqualTo(IntelligenceInterpretation.GOOD_INTELLIGENCE);
    }

    @Test
    void calculatesAgeFromARegisteredUserAtTheReferenceDate() {
        WeillUser user = new WeillUser(
            "student@example.com",
            "Estudiante",
            "Secundaria",
            "Centro escolar",
            "Municipio",
            "Departamento",
            LocalDate.of(2012, 6, 7),
            true
        );

        PsychometricResult result = engine.calculate(user, 43, LocalDate.of(2026, 6, 6));

        assertThat(result.age()).isEqualTo(13);
        assertThat(result.normGroup()).isEqualTo(NormGroup.AGE_13);
        assertThat(result.percentile()).isEqualTo(80);
        assertThat(result.intelligenceQuotient()).isEqualTo(113);
    }

    @Test
    void mapsEveryDefinedPercentileToItsIq() {
        PercentileIqTable iqTable = new PercentileIqTable();

        assertThat(iqTable.iqFor(99)).isEqualTo(135);
        assertThat(iqTable.iqFor(90)).isEqualTo(119);
        assertThat(iqTable.iqFor(50)).isEqualTo(100);
        assertThat(iqTable.iqFor(25)).isEqualTo(90);
        assertThat(iqTable.iqFor(1)).isEqualTo(65);
    }
}
