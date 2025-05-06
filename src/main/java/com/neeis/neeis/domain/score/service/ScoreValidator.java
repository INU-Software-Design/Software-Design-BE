package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;

public class ScoreValidator {

    public static void validateRawScore(double rawScore, EvaluationMethod eval) {
        if (rawScore < 0) {
            throw new CustomException(ErrorCode.SCORE_NEGATIVE);
        }

        if (rawScore > eval.getFullScore()) {
            throw new CustomException(ErrorCode.SCORE_OVER_FULL);
        }
    }
}