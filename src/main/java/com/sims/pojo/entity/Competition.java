package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName competition
*/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Competition implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    
    private Long competition_id;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String name;
    /**
    * 竞赛类别:数学,编程,电子设计,机器人,创新创业等
    */
    @NotBlank(message="[竞赛类别:数学,编程,电子设计,机器人,创新创业等]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String category;
    /**
    * 竞赛级别:国际级,国家级,省级,校级
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String level;
    /**
    * 主办方
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Length(max= 100,message="编码长度不能超过100")
    private String organizer;
    /**
    * 
    */
    
    private Date start_date;
    /**
    * 
    */
    
    private Date end_date;
    /**
    * 
    */
    @Size(max= -1,message="编码长度不能超过-1")
    
    @Length(max= -1,message="编码长度不能超过-1")
    private String description;
    /**
    * 
    */
    
    private Date registration_deadline;
    /**
    * 
    */
    
    private Integer max_participants;
    /**
    * 
    */
    
    private Integer current_participants;

}
