package com.sims.pojo.dto;

import lombok.Data;

@Data
public class ScoreDTO {
    private Long studentId;
    private Long courseId;
    private Double regularGrade;
    private Double  examGrade;
    private Double finalGrade;
    private Double gradePoint;
    private String comments;
}
