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
* @TableName job
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    
    private Long jobId;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String title;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String company;
    /**
    * 行业
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String industry;
    /**
    * 岗位类型:技术,管理,销售,研发等
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String jobType;
    /**
    * 
    */
    @Size(max= -1,message="编码长度不能超过-1")
    
    @Length(max= -1,message="编码长度不能超过-1")
    private String description;
    /**
    * 岗位要求
    */
    @Size(max= -1,message="编码长度不能超过-1")
    @Length(max= -1,message="编码长度不能超过-1")
    private String requirements;
    /**
    * 
    */
    @Size(max= 50,message="编码长度不能超过50")
    
    @Length(max= 50,message="编码长度不能超过50")
    private String salaryRange;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String location;
    /**
    * 
    */
    @Size(max= 50,message="编码长度不能超过50")
    
    @Length(max= 50,message="编码长度不能超过50")
    private String experienceRequirement;
    /**
    * 
    */
    @Size(max= 50,message="编码长度不能超过50")
    
    @Length(max= 50,message="编码长度不能超过50")
    private String educationRequirement;


}
