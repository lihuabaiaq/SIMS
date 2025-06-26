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
* @TableName growthrecord
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Growthrecord implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    
    private Long record_id;
    /**
    * 
    */
    
    private Long student_id;
    /**
    * 记录类型:学业,竞赛,活动,实习等
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String record_type;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String title;
    /**
    * 
    */
    @Size(max= -1,message="编码长度不能超过-1")
    
    @Length(max= -1,message="编码长度不能超过-1")
    private String content;
    /**
    * 
    */
    
    private Date record_date;
    /**
    * 附件路径
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Length(max= 255,message="编码长度不能超过255")
    private String attachment;
    /**
    * 
    */
    
    private Date created_at;
    /**
    * 
    */
    
    private Date updated_at;

}
