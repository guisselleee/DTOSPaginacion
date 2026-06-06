package com.example.weill.norms;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WeillNormTable {

    private static final int[] PERCENTILES = {90, 80, 75, 70, 60, 50, 40, 30, 25, 20, 10, 5};

    private static final int[][] RAW_SCORE_THRESHOLDS = {
        {35, 41, 42, 46, 46, 46, 47, 47, 48, 50, 30, 47, 57},
        {31, 36, 39, 42, 43, 43, 43, 43, 43, 46, 23, 43, 56},
        {30, 34, 36, 40, 42, 42, 42, 42, 42, 44, 22, 42, 55},
        {29, 33, 37, 39, 41, 41, 41, 41, 41, 42, 21, 40, 54},
        {27, 31, 35, 37, 38, 38, 38, 39, 39, 41, 19, 37, 53},
        {25, 28, 33, 33, 33, 34, 34, 35, 36, 36, 17, 35, 51},
        {23, 26, 31, 32, 32, 33, 33, 34, 35, 35, 16, 33, 49},
        {21, 23, 30, 31, 31, 32, 32, 33, 34, 34, 13, 30, 46},
        {29, 22, 28, 29, 30, 31, 31, 32, 32, 32, 12, 28, 44},
        {17, 20, 26, 28, 29, 30, 30, 31, 31, 31, 8, 26, 42},
        {11, 15, 21, 25, 25, 27, 26, 27, 26, 25, 7, 21, 40},
        {8, 12, 16, 19, 21, 22, 22, 22, 23, 23, 5, 16, 35}
    };

    private final Map<NormGroup, Map<Integer, Integer>> thresholdsByGroup;

    public WeillNormTable() {
        thresholdsByGroup = new EnumMap<>(NormGroup.class);
        NormGroup[] groups = NormGroup.values();
        for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
            Map<Integer, Integer> thresholds = new LinkedHashMap<>();
            for (int percentileIndex = 0; percentileIndex < PERCENTILES.length; percentileIndex++) {
                thresholds.put(PERCENTILES[percentileIndex], RAW_SCORE_THRESHOLDS[percentileIndex][groupIndex]);
            }
            thresholdsByGroup.put(groups[groupIndex], Map.copyOf(thresholds));
        }
    }

    public int percentileFor(NormGroup group, int correctAnswers) {
        if (correctAnswers < 0 || correctAnswers > 60) {
            throw new IllegalArgumentException("Correct answers must be between 0 and 60.");
        }
        Map<Integer, Integer> thresholds = thresholdsByGroup.get(group);
        if (thresholds == null) {
            throw new IllegalArgumentException("Unsupported norm group: " + group);
        }
        for (int percentile : PERCENTILES) {
            if (correctAnswers >= thresholds.get(percentile)) {
                return percentile;
            }
        }
        return 5;
    }

    public int thresholdFor(NormGroup group, int percentile) {
        Integer threshold = thresholdsByGroup.getOrDefault(group, Map.of()).get(percentile);
        if (threshold == null) {
            throw new IllegalArgumentException("Unsupported percentile: " + percentile);
        }
        return threshold;
    }
}
