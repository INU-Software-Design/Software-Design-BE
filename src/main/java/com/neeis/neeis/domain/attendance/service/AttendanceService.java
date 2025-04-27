@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final SemesterRepository semesterRepository;
    private final AttendanceFeedbackRepository feedbackRepository;

    @Transactional
    public void saveAttendance(String username, AttendanceBulkRequestDto requestDto) {

        Teacher teacher = teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(requestDto.getYear(), requestDto.getGrade(), requestDto.getClassNumber(), teacher.getId());

        if(classroom.getTeacher() != teacher) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 담당 학생들 확인
        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);
        Map<Long, Student> studentMap = classroomStudentList.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getStudent().getId(),
                        ClassroomStudent::getStudent
                ));

        // 학생과 DB 확인
        for (StudentAttendanceDto dto : requestDto.getStudents()) {
            if (!studentMap.containsKey(dto.getStudentId())) {
                throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
            }
        }

        // 출결 저장
        // 입력받은 연도 + 월 -> 마지막 일 구하기
        YearMonth yearMonth = YearMonth.of(requestDto.getYear(), requestDto.getMonth());
        int lastDay = yearMonth.lengthOfMonth(); // 해당 월의 마지막 일

        for (StudentAttendanceDto studentDto : requestDto.getStudents()) {
            Student student = studentMap.get(studentDto.getStudentId());

            // 날짜별 출결사항 매핑
            Map<LocalDate, AttendanceStatus> statusMap = new HashMap<>();
            for (DailyAttendanceDto dailyDto : studentDto.getAttendances()) {

                statusMap.put(dailyDto.getDate(), dailyDto.getStatus());
            }

            // 한달치 모든 날짜에 대해 저장
            for (int day = 1; day <= lastDay; day++) {
                LocalDate date = LocalDate.of(requestDto.getYear(), requestDto.getMonth(), day);
                AttendanceStatus status = statusMap.getOrDefault(date, AttendanceStatus.PRESENT); // 없으면 출석으로

                // 특이사항만 저장
                if (status != AttendanceStatus.PRESENT) {
                    Attendance attendance = attendanceRepository.findByStudentAndDate(student, date)
                            .orElse(Attendance.builder()
                                    .student(student)
                                    .date(date)
                                    .status(status)
                                    .build());

                    // 상태 업데이트
                    attendance = Attendance.builder()
                            .student(student)
                            .date(date)
                            .status(status)
                            .build();

                    attendanceRepository.save(attendance);
                }
            }
        }
    }
