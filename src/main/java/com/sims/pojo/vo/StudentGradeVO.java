package com.sims.pojo.vo;

import lombok.Data;

@Data
public class StudentGradeVO {
    private String courseName;
    private String courseCategory;
    private String courseYear;
    private String credit;
    private Integer regularGrade;
    private Integer examGrade;
    private Integer finalGrade;
    private String gradePoint;
    private String comments;
    private String semester;
}
