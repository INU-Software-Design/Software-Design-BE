package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.Counsel;
import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.CustomUserDetails;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class CounselServiceTest {

    @Resource
    private CounselService counselService;

    @Resource
    private CounselRepository counselRepository;

    @Resource
    private StudentRepository studentRepository;

    @Resource
    private TeacherRepository teacherRepository;

    @Resource
    private TeacherService teacherService;

    @Resource
    private StudentService studentService;

    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        teacher = teacherRepository.findById(1L).orElseThrow(() -> new RuntimeException("테스트용 교사 없음"));
        student = studentRepository.findById(1L).orElseThrow(() -> new RuntimeException("테스트용 학생 없음"));

        CustomUserDetails userDetails = new CustomUserDetails(teacher.getUser());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("선생님 권한이 아닌 경우 - 접근 거부 예외 발생")
    void authenticate_notTeacher() {
        // given
        String notTeacherUsername = "student01";

        // when & then
        assertThatThrownBy(() -> teacherService.authenticate(notTeacherUsername))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("상담 생성 성공")
    void createCounsel_success() {
        // given
        CounselRequestDto requestDto = CounselRequestDto.builder()
                .category("ACADEMIC")
                .content("수학 과목 상담")
                .dateTime(LocalDate.now())
                .nextPlan("문제 풀이 집중")
                .isPublic(true)
                .build();

        // when
        CounselResponseDto responseDto = counselService.createCounsel(teacher.getUser().getUsername(), student.getId(), requestDto);

        // then
        assertThat(responseDto).isNotNull();
    }

    @Test
    @DisplayName("상담 단건 조회 성공")
    void getCounsel_success() {
        // given
        Counsel counsel = counselRepository.save(Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(CounselCategory.ACADEMIC)
                .content("단건 조회 상담")
                .dateTime(LocalDate.now())
                .nextPlan("추가 지도 예정")
                .isPublic(true)
                .build());

        // when
        CounselDetailDto detailDto = counselService.getCounsel(teacher.getUser().getUsername(), counsel.getId());

        // then
        assertThat(detailDto).isNotNull();
        assertThat(detailDto.getContent()).isEqualTo("단건 조회 상담");
    }

    @Test
    @DisplayName("상담 목록 조회 성공")
    void getCounsels_success() {
        // given
        counselRepository.save(Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(CounselCategory.CAREER)
                .content("목록 조회 상담")
                .dateTime(LocalDate.now())
                .nextPlan("진로 체험 예정")
                .isPublic(true)
                .build());

        counselRepository.save(Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(CounselCategory.CAREER)
                .content("목록 조회 상담")
                .dateTime(LocalDate.now())
                .nextPlan("진로 체험 예정")
                .isPublic(true)
                .build());

        // when
        List<CounselDetailDto> counsels = counselService.getCounsels(teacher.getUser().getUsername(), student.getId());

        // then
        assertThat(counsels).isNotEmpty();
    }

    @Test
    @DisplayName("상담 수정 성공")
    void updateCounsel_success() {
        // given
        Counsel counsel = counselRepository.save(Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(CounselCategory.PERSONAL)
                .content("기존 상담")
                .dateTime(LocalDate.now())
                .nextPlan("없음")
                .isPublic(true)
                .build());

        CounselRequestDto updateDto = CounselRequestDto.builder()
                .category("FAMILY")
                .content("수정된 상담")
                .dateTime(LocalDate.now())
                .nextPlan("부모 상담 연계")
                .isPublic(true)
                .build();

        // when
        CounselDetailDto updated = counselService.updateCounsel(teacher.getUser().getUsername(), counsel.getId(), updateDto);

        // then
        assertThat(updated.getContent()).isEqualTo("수정된 상담");
        assertThat(updated.getCategory()).isEqualTo(CounselCategory.FAMILY);
        assertThat(updated.getIsPublic()).isEqualTo(true);
    }

    @Test
    @DisplayName("존재하지 않는 학생으로 상담 생성 시 예외 발생")
    void createCounsel_nonexistentStudent() {
        // given
        CounselRequestDto requestDto = CounselRequestDto.builder()
                .category("ACADEMIC")
                .content("없는 학생 상담 요청")
                .dateTime(LocalDate.now())
                .nextPlan("추가 지도 예정")
                .isPublic(true)
                .build();

        Long nonexistentStudentId = 9999L; // 없는 학생 ID (테스트용)

        // when & then
        assertThatThrownBy(() -> counselService.createCounsel(teacher.getUser().getUsername(), nonexistentStudentId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("잘못된 카테고리 입력시 예외 발생")
    void findCategory_invalidCategory() {
        // given
        CounselRequestDto wrongDto = CounselRequestDto.builder()
                .category("INVALID_CATEGORY")
                .content("잘못된 카테고리 테스트")
                .dateTime(LocalDate.now())
                .nextPlan("없음")
                .isPublic(true)
                .build();

        // when & then
        assertThatThrownBy(() -> counselService.createCounsel(teacher.getUser().getUsername(), student.getId(), wrongDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_CATEGORY_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 상담 조회 시 예외 발생")
    void getCounsel_nonexistentCounsel() {
        // given
        Long nonexistentCounselId = 9999L; // 없는 상담 ID (테스트용)

        // when & then
        assertThatThrownBy(() -> counselService.getCounsel(teacher.getUser().getUsername(), nonexistentCounselId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_NOT_FOUND.getMessage());
    }
}