package com.sims.pojo.dto;

import lombok.Data;

@Data
public class TeacherChangeDTO {
    private String phone;
    private String email;
    private Long teacherId;
    private String originalPassword;
    private String changePassword;
}
