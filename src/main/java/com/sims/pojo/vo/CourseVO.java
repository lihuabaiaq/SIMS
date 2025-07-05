package com.sims.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {
    private Long courseId;

    private String courseName;

    private String teacherName;

    private BigDecimal credit;

    private Integer hours;

    private String gradeRequirement;

    private String category;

    private String description;

    private Integer maxStudents;

    private Integer currentStudents;

    private String semester;

    private LocalDateTime registerStart;

    private LocalDateTime registerEnd;

}
