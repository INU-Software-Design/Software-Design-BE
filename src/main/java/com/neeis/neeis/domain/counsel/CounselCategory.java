package com.neeis.neeis.domain.counsel;

import lombok.Getter;

@Getter
public enum CounselCategory {
    UNIVERSITY("대학"),
    CAREER("취업"),
    FAMILY("가정"),
    ACADEMIC("학업"),
    PERSONAL("개인"),
    OTHER("기타");

    private final String displayName;

    CounselCategory(String displayName) {
        this.displayName = displayName;
    }

    public static boolean exists(String value) {
        for(CounselCategory c : CounselCategory.values()) {
            if(c.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
