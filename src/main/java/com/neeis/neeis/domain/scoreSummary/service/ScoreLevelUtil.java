package com.neeis.neeis.domain.scoreSummary.service;

import java.util.*;

public class ScoreLevelUtil {

    // 석차 산정 (평가 대상자별 점수 -> 석차 Map 반환)
    public static Map<Long, Integer> calculateRanks(Map<Long, Double> studentScoreMap) {
        List<Map.Entry<Long, Double>> sortedList = new ArrayList<>(studentScoreMap.entrySet());

        // 점수 내림차순 정렬
        sortedList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        Map<Long, Integer> rankMap = new HashMap<>();
        int rank = 1;
        int sameRankCount = 1;
        double prevScore = -1;

        for (int i = 0; i < sortedList.size(); i++) {
            long studentId = sortedList.get(i).getKey();
            double score = sortedList.get(i).getValue();

            if (score == prevScore) {
                rankMap.put(studentId, rank);
                sameRankCount++;
            } else {
                rank = i + 1;
                rankMap.put(studentId, rank);
                sameRankCount = 1;
            }

            prevScore = score;
        }

        return rankMap;
    }

    // 등급 산정 (석차 기준, 상위 10%씩 A~E 부여)
    public static int getGrade(int rank, int total) {
        if (total == 0) return 0;

        double ratio = (double) rank / total;

        if (ratio <= 0.1) return 1; // A
        if (ratio <= 0.3) return 2; // B
        if (ratio <= 0.6) return 3; // C
        if (ratio <= 0.8) return 4; // D
        return 5;                  // E
    }

    // 성취도(A~E) 문자열 변환
    public static String toAchievementLevel(int grade) {
        return switch (grade) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            default -> "-";
        };
    }
}