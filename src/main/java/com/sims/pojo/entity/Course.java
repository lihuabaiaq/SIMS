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
* @TableName course
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    private Long course_id;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String course_name;
    /**
    * 
    */
    
    private Long teacher_id;
    /**
    * 学分
    */
    @NotNull(message="[学分]不能为空")
    private BigDecimal credit;
    /**
    * 学时
    */
    private Integer hours;
    /**
    * 开设年级要求
    */
    @Size(max= 10,message="编码长度不能超过10")
    @Length(max= 10,message="编码长度不能超过10")
    private String grade_requirement;
    /**
    * 课程类别:数学,电子信息,计算机,自然基础学科,思想道德,体育等
    */
    @NotBlank(message="[课程类别:数学,电子信息,计算机,自然基础学科,思想道德,体育等]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String category;
    /**
    * 
    */
    @Size(max= -1,message="编码长度不能超过-1")
    
    @Length(max= -1,message="编码长度不能超过-1")
    private String description;
    /**
    * 最大选课人数
    */
    private Integer max_students;
    /**
    * 当前选课人数
    */
    private Integer current_students;
    /**
    * 学期，如"2023-2024-1"
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String semester;

}
