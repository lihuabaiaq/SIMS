package com.sims.pojo.vo;

import com.sims.pojo.entity.Competition;
import lombok.Data;

@Data
public class CompetitionVO  {
   private Long competitionId;
   private Double comWeight;
   private String reason;
   private String recommendStatus;
}
