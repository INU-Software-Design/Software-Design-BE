@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "담임 학생 월별 출결 저장", description = "로그인한 교사의 월별 출결을 저장합니다. <br>" +
            "출결은 당월 저장만 가능합니다. <br>" +
            "조회하려는 학급의 년도, 학년, 반이 필수로 입력해야합니다. <br>" +
            "status : PRESENT(출석), ABSENT(결석), LATE(지각), EARLY(조퇴)  ")
    public ResponseEntity<CommonResponse<Object>> saveAttendance(@AuthenticationPrincipal UserDetails userDetails,
                                                           @Valid @RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto){
        attendanceService.saveAttendance(userDetails.getUsername(), attendanceBulkRequestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_ATTENDANCE.getMessage()));
    }
