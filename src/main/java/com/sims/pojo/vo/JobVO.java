package com.sims.pojo.vo;

import com.sims.pojo.entity.Job;
import lombok.Data;

@Data
public class JobVO extends Job {
    private Double score;
    private String reason;
    private String recommendStatus;
}
