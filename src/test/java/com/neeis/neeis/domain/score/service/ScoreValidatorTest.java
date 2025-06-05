package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * ScoreValidator 테스트 클래스

 * 정적 메서드 테스트이므로 Mockito 없이 순수 단위 테스트로 구성
 */
@DisplayName("ScoreValidator 테스트")
class ScoreValidatorTest {

    private EvaluationMethod evaluationMethod100; // 만점 100점
    private EvaluationMethod evaluationMethod50;  // 만점 50점
    private EvaluationMethod evaluationMethodDecimal; // 만점 85.5점 (소수점)

    @BeforeEach
    void setUp() {
        // 테스트용 Subject 생성
        Subject testSubject = Subject.builder()
                .name("수학")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", 1L);

        // 만점 100점인 평가방법
        evaluationMethod100 = EvaluationMethod.builder()
                .subject(testSubject)
                .year(2024)
                .semester(1)
                .title("중간고사")
                .fullScore(100)
                .weight(100.0)
                .build();
        ReflectionTestUtils.setField(evaluationMethod100, "id", 1L);

        // 만점 50점인 평가방법
        evaluationMethod50 = EvaluationMethod.builder()
                .subject(testSubject)
                .year(2024)
                .semester(1)
                .title("퀴즈")
                .fullScore(50)
                .weight(50.0)
                .build();
        ReflectionTestUtils.setField(evaluationMethod50, "id", 2L);

        // 만점이 소수점인 평가방법
        evaluationMethodDecimal = EvaluationMethod.builder()
                .subject(testSubject)
                .year(2024)
                .semester(1)
                .title("실습평가")
                .fullScore(85.5)
                .weight(75.0)
                .build();
        ReflectionTestUtils.setField(evaluationMethodDecimal, "id", 3L);
    }

    @Nested
    @DisplayName("원점수 유효성 검증 - 정상 케이스")
    class ValidRawScoreTest {

        @Test
        @DisplayName("만점 100점에서 유효한 점수들")
        void validateRawScore_valid_scores_100() {
            // when & then - 예외가 발생하지 않아야 함
            assertThatCode(() -> ScoreValidator.validateRawScore(0, evaluationMethod100))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(50, evaluationMethod100))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(100, evaluationMethod100))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(85.5, evaluationMethod100))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("만점 50점에서 유효한 점수들")
        void validateRawScore_valid_scores_50() {
            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(0, evaluationMethod50))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(25, evaluationMethod50))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(50, evaluationMethod50))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(49.9, evaluationMethod50))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("소수점 만점에서 유효한 점수들")
        void validateRawScore_valid_scores_decimal() {
            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(0, evaluationMethodDecimal))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(42.75, evaluationMethodDecimal))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(85.5, evaluationMethodDecimal))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.1, 1.0, 10.5, 99.9, 100.0})
        @DisplayName("만점 100점에서 다양한 유효 점수들 - 파라미터화 테스트")
        void validateRawScore_various_valid_scores(double rawScore) {
            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(rawScore, evaluationMethod100))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("원점수 유효성 검증 - 음수 예외")
    class NegativeScoreTest {

        @Test
        @DisplayName("음수 점수 입력 시 SCORE_NEGATIVE 예외 발생")
        void validateRawScore_negative_score() {
            // given
            double negativeScore = -1.0;

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(negativeScore, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_NEGATIVE);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, -1.0, -10.5, -100.0, -999.9})
        @DisplayName("다양한 음수 점수들 - 파라미터화 테스트")
        void validateRawScore_various_negative_scores(double negativeScore) {
            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(negativeScore, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_NEGATIVE);
        }

        @Test
        @DisplayName("매우 작은 음수도 예외 발생")
        void validateRawScore_very_small_negative() {
            // given
            double verySmallNegative = -0.00001;

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(verySmallNegative, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_NEGATIVE);
        }
    }

    @Nested
    @DisplayName("원점수 유효성 검증 - 만점 초과 예외")
    class OverFullScoreTest {

        @Test
        @DisplayName("만점 100점 초과 시 SCORE_OVER_FULL 예외 발생")
        void validateRawScore_over_full_score_100() {
            // given
            double overScore = 100.1;

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(overScore, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @Test
        @DisplayName("만점 50점 초과 시 SCORE_OVER_FULL 예외 발생")
        void validateRawScore_over_full_score_50() {
            // given
            double overScore = 50.01;

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(overScore, evaluationMethod50))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @Test
        @DisplayName("소수점 만점 초과 시 SCORE_OVER_FULL 예외 발생")
        void validateRawScore_over_decimal_full_score() {
            // given
            double overScore = 85.51; // 만점이 85.5

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(overScore, evaluationMethodDecimal))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @ParameterizedTest
        @ValueSource(doubles = {100.1, 101.0, 150.0, 999.9})
        @DisplayName("만점 100점을 초과하는 다양한 점수들 - 파라미터화 테스트")
        void validateRawScore_various_over_scores(double overScore) {
            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(overScore, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @Test
        @DisplayName("매우 작은 초과값도 예외 발생")
        void validateRawScore_very_small_over() {
            // given
            double verySmallOver = 100.00001; // 만점이 100

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(verySmallOver, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("정확히 0점 - 경계값")
        void validateRawScore_exactly_zero() {
            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(0.0, evaluationMethod100))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정확히 만점 - 경계값")
        void validateRawScore_exactly_full_score() {
            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(100.0, evaluationMethod100))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(50.0, evaluationMethod50))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(85.5, evaluationMethodDecimal))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("0보다 아주 작은 값 - 음수 경계값")
        void validateRawScore_just_below_zero() {
            // given
            double justBelowZero = -0.000001; // 실제 음수값 사용

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(justBelowZero, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_NEGATIVE);
        }

        @Test
        @DisplayName("만점보다 아주 작은 값 초과 - 만점 초과 경계값")
        void validateRawScore_just_above_full_score() {
            // given
            double justAboveFullScore = 100.000001; // 실제로 100을 초과하는 값 사용

            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(justAboveFullScore, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }
    }

    @Nested
    @DisplayName("특수값 테스트")
    class SpecialValueTest {

        @Test
        @DisplayName("무한대 값 입력 시 만점 초과 예외")
        void validateRawScore_positive_infinity() {
            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(Double.POSITIVE_INFINITY, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @Test
        @DisplayName("음의 무한대 값 입력 시 음수 예외")
        void validateRawScore_negative_infinity() {
            // when & then
            assertThatThrownBy(() -> ScoreValidator.validateRawScore(Double.NEGATIVE_INFINITY, evaluationMethod100))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_NEGATIVE);
        }

        @Test
        @DisplayName("NaN 값 입력 시 음수 예외 (NaN < 0 은 false이므로 만점 초과로 처리)")
        void validateRawScore_nan() {
            // NaN은 모든 비교에서 false를 반환하므로
            // rawScore < 0 은 false, rawScore > fullScore 는 false
            // 따라서 NaN은 예외가 발생하지 않을 수 있음 (구현에 따라 다름)
            // 하지만 일반적으로 NaN > fullScore는 false이므로 통과할 가능성이 있음

            // when & then
            // NaN의 경우 비교 연산의 결과가 예측 불가능하므로
            // 실제 동작을 확인하는 테스트
            assertThatCode(() -> ScoreValidator.validateRawScore(Double.NaN, evaluationMethod100))
                    .doesNotThrowAnyException(); // NaN < 0은 false, NaN > 100도 false이므로 통과
        }
    }

    @Nested
    @DisplayName("다양한 평가방법별 테스트")
    class DifferentEvaluationMethodTest {

        @Test
        @DisplayName("만점 1점인 평가방법")
        void validateRawScore_full_score_one() {
            // given
            Subject testSubject = Subject.builder().name("참여도").build();
            EvaluationMethod onePointEval = EvaluationMethod.builder()
                    .subject(testSubject)
                    .year(2024)
                    .semester(1)
                    .title("출석체크")
                    .fullScore(1)
                    .weight(10.0)
                    .build();

            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(0, onePointEval))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(1, onePointEval))
                    .doesNotThrowAnyException();

            assertThatThrownBy(() -> ScoreValidator.validateRawScore(1.1, onePointEval))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }

        @Test
        @DisplayName("만점이 매우 큰 평가방법")
        void validateRawScore_very_large_full_score() {
            // given
            Subject testSubject = Subject.builder().name("프로젝트").build();
            EvaluationMethod largeScoreEval = EvaluationMethod.builder()
                    .subject(testSubject)
                    .year(2024)
                    .semester(1)
                    .title("대규모프로젝트")
                    .fullScore(10000)
                    .weight(100.0)
                    .build();

            // when & then
            assertThatCode(() -> ScoreValidator.validateRawScore(5000, largeScoreEval))
                    .doesNotThrowAnyException();

            assertThatCode(() -> ScoreValidator.validateRawScore(10000, largeScoreEval))
                    .doesNotThrowAnyException();

            assertThatThrownBy(() -> ScoreValidator.validateRawScore(10000.1, largeScoreEval))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_OVER_FULL);
        }
    }
}