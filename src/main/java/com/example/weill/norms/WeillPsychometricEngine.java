package com.example.weill.norms;

import com.example.weill.user.WeillUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Service
public class WeillPsychometricEngine {

    private final PopulationClassifier populationClassifier;
    private final WeillNormTable normTable;
    private final PercentileIqTable iqTable;

    public WeillPsychometricEngine(
        PopulationClassifier populationClassifier,
        WeillNormTable normTable,
        PercentileIqTable iqTable
    ) {
        this.populationClassifier = populationClassifier;
        this.normTable = normTable;
        this.iqTable = iqTable;
    }

    public PsychometricResult calculate(
        int age,
        boolean currentlyStudying,
        String educationLevel,
        int correctAnswers
    ) {
        PopulationProfile profile = populationClassifier.classify(age, currentlyStudying, educationLevel);
        int percentile = normTable.percentileFor(profile.normGroup(), correctAnswers);
        int iq = iqTable.iqFor(percentile);
        return new PsychometricResult(
            age,
            profile.category(),
            profile.normGroup(),
            correctAnswers,
            percentile,
            iq,
            IntelligenceInterpretation.fromIq(iq)
        );
    }

    public PsychometricResult calculate(WeillUser user, int correctAnswers, LocalDate referenceDate) {
        Objects.requireNonNull(user, "User is required.");
        Objects.requireNonNull(referenceDate, "Reference date is required.");
        if (user.getBirthDate() == null || user.getCurrentlyStudying() == null) {
            throw new IllegalArgumentException("The user does not have complete psychometric profile data.");
        }
        int age = Period.between(user.getBirthDate(), referenceDate).getYears();
        return calculate(age, user.getCurrentlyStudying(), user.getEducationLevel(), correctAnswers);
    }
}
