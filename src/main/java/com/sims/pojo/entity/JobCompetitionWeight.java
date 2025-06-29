package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName job_competition_weight
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCompetitionWeight implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    
    private Long id;
    /**
    * 
    */
    
    private Long jobId;
    /**
    * 竞赛类别
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String competitionCategory;
    /**
    * 权重值
    */
    @NotNull(message="[权重值]不能为空")
    private Double weight;

}
