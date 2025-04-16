package com.neeis.neeis.domain.counsel;

public enum CounselCategory {
    // 대학
    UNIVERSITY,

    // 취업
    CAREER,

    // 가정
    FAMILY,

    // 학업
    ACADEMIC,

    // 개인
    PERSONAL,

    // 기타
    OTHER;

    public static boolean exists(String value) {
        for(CounselCategory c : CounselCategory.values()) {
            if(c.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
