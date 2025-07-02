package com.sims.pojo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompetitionRecordVO {
    private String name;
    private String category;
    private String level;
    private String description;
    private String role;
    private String award;
    private LocalDateTime StartDate;
    private LocalDateTime EndDate;
}
