package com.sims.pojo.dto;

import lombok.Data;

@Data
public class StudentChangeDTO {
    private String phone;
    private String email;
    private Long studentId;
    private String originPassword;
    private String changePassword;
}
