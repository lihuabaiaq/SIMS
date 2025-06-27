package com.sims.pojo.dto;

import lombok.Data;

@Data
public class StudentGradeDTO {
    private Long studentId;
    private String courseName;
    private Integer credit;
    private Integer courseCategory;
    private String courseYear;
}
