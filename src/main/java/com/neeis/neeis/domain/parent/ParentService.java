package com.neeis.neeis.domain.parent;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.neeis.neeis.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentService {
    private final ParentRepository parentRepository;

    public Parent getParentByUser(User user) {
        return parentRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public List<Parent> getParents(Student student) {
        return parentRepository.findByStudent(student);
    }
}
