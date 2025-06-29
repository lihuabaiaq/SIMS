package com.sims.pojo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompetitionDTO {
    private Long studentId;
    private LocalDate minDate;
    private LocalDate maxDate;
    private String category;
    private String level;
}
