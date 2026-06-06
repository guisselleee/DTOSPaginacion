package com.example.weill.norms;

public record PsychometricResult(
    int age,
    PopulationCategory populationCategory,
    NormGroup normGroup,
    int correctAnswers,
    int percentile,
    int intelligenceQuotient,
    IntelligenceInterpretation interpretation
) {
}
