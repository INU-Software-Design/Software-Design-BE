package com.neeis.neeis.domain.scoreSummary.service;

import java.util.Collection;
import java.util.List;

public class ScoreStatUtil {

    // 평균 계산
    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;

        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return round(sum / values.size(), 2);
    }

    // 표준편차 계산
    public static double standardDeviation(Collection<Double> values) {
        if (values == null || values.size() < 2) return 0.0;

        double mean = values.stream().mapToDouble(d -> d).average().orElse(0.0);
        double sumSquared = values.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .sum();

        return Math.round(Math.sqrt(sumSquared / values.size()) * 10.0) / 10.0; // 소수 첫째 자리 반올림
    }

    // 소수점 반올림
    private static double round(double value, int digit) {
        double factor = Math.pow(10, digit);
        return Math.round(value * factor) / factor;
    }
}