package com.example.weill.norms;

public record PopulationProfile(
    int age,
    PopulationCategory category,
    NormGroup normGroup
) {
}
