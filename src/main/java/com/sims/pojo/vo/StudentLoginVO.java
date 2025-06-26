package com.sims.pojo.vo;

import com.sims.pojo.entity.Student;
import lombok.Data;

@Data
public class StudentLoginVO {
    private String token;
    private Student student;
}
