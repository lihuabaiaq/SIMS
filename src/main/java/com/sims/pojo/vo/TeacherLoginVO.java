package com.sims.pojo.vo;

import com.sims.pojo.entity.Teacher;
import lombok.Data;

@Data
public class TeacherLoginVO {
    private String token;
    private Teacher teacher;
}
