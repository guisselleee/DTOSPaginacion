package com.example.weill.norms;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PercentileIqTable {

    private static final Map<Integer, Integer> IQ_BY_PERCENTILE = Map.ofEntries(
        Map.entry(99, 135),
        Map.entry(97, 129),
        Map.entry(95, 125),
        Map.entry(90, 119),
        Map.entry(80, 113),
        Map.entry(75, 110),
        Map.entry(70, 108),
        Map.entry(60, 104),
        Map.entry(50, 100),
        Map.entry(40, 96),
        Map.entry(30, 92),
        Map.entry(25, 90),
        Map.entry(20, 87),
        Map.entry(10, 81),
        Map.entry(5, 75),
        Map.entry(3, 72),
        Map.entry(1, 65)
    );

    public int iqFor(int percentile) {
        Integer iq = IQ_BY_PERCENTILE.get(percentile);
        if (iq == null) {
            throw new IllegalArgumentException("No IQ equivalence is defined for percentile " + percentile + ".");
        }
        return iq;
    }
}
