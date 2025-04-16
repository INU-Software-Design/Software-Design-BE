package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class StudentDetailResDto {
    private final Long id;
    private final int grade;
    private final int classroom;
    private final int number;
    private final String gender;
   // private final String name;
    private final String ssn;
    private final String address;
    private final String phone;
    private final LocalDate admissionDate;
    private final String fatherName;
    private final String fatherNum;
    private final String motherName;
    private final String motherNum;

    @Builder
    private StudentDetailResDto(Long id, int grade, int classroom, int number, String gender, String name, String ssn, String address, String phone, LocalDate admissionDate,  String fatherName, String fatherNum, String motherName, String motherNum) {
        this.id = id;
        this.grade = grade;
        this.classroom = classroom;
        this.number = number;
        this.gender = gender;
      //  this.name = name;
        this.ssn = ssn;
        this.address = address;
        this.phone = phone;
        this.admissionDate = admissionDate;
        this.fatherName = fatherName;
        this.fatherNum = fatherNum;
        this.motherName = motherName;
        this.motherNum = motherNum;
    }

    public static StudentDetailResDto of(Student student, Parent father, Parent mother, Classroom classroom, ClassroomStudent classroomStudent) {
        return StudentDetailResDto.builder()
                .id(student.getId())
                .grade(classroom.getGrade())
                .classroom(classroom.getClassNum())
                .number(classroomStudent.getNumber())
                .gender(student.getGender())
                .name(student.getName())
                .ssn(student.getSsn())
                .address(student.getAddress())
                .phone(student.getPhone())
                .admissionDate(student.getAdmissionDate())
                .fatherName(father.getName())
                .fatherNum(father.getPhone())
                .motherName(mother.getName())
                .motherNum(mother.getPhone())
                .build();
    }

}
