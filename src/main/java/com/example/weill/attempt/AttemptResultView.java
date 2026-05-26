package com.example.weill.attempt;

import java.util.List;

public record AttemptResultView(
    WeillTestAttempt attempt,
    List<ItemResponse> responses,
    long answeredItems
) {
}
