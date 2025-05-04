package com.neeis.neeis.domain.evaluationMethod;

public enum ExamType {
    // 자필
    WRITTEN,

    // 수행
    PRACTICAL;

    public static boolean exists(String value){
        for(ExamType examType : ExamType.values()){
            if(examType.name().equals(value)){
                return true;
            }
        }
        return false;
    }
}