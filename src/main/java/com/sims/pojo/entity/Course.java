package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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

    @NotNull(message="[]不能为空")
    @TableId(type = IdType.AUTO)
    private Long courseId;

    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String courseName;
    
    private Long teacherId;
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
    private String gradeRequirement;
    /**
    * 课程类别:数学,电子信息,计算机,自然基础学科,思想道德,体育等
    */
    @NotBlank(message="[课程类别:数学,电子信息,计算机,自然基础学科,思想道德,体育等]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String category;

    @Size(max= -1,message="编码长度不能超过-1")
    @Length(max= -1,message="编码长度不能超过-1")
    private String description;
    /**
    * 最大选课人数
    */
    private Integer maxStudents;
    /**
    * 当前选课人数
    */
    private Integer currentStudents;
    /**
    * 学期，如"2023-2024-1"
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String semester;

    /**
     * 状态，0表示未开始，1表示选课中，2表示在修，3表示结课
     */
    private Integer status;

    /**
     * 选课开始时间
     */
    @NotNull(message="[选课开始时间]不能为空")
    private LocalDateTime registerStart;
    /**
     * 选课结束时间
     */
    @NotNull(message="[选课结束时间]不能为空")
    private LocalDateTime registerEnd;

}
