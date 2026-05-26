package com.example.weill.attempt;

public record ResponseSubmission(
    int pageNumber,
    int rowNumber,
    int itemNumber,
    int selectedOption
) {
}
