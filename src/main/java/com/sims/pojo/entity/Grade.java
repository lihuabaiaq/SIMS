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
* @TableName grade
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade implements Serializable {

    /**
     *
     */
    @NotNull(message = "[]不能为空")
    private Long id;
    /**
     *
     */

    private Long student_id;
    /**
     *
     */

    private Long course_id;
    /**
     * 平时成绩
     */
    private BigDecimal regular_grade;
    /**
     * 考试成绩
     */
    private BigDecimal exam_grade;
    /**
     * 最终成绩
     */
    private BigDecimal final_grade;
    /**
     * 绩点
     */
    private BigDecimal grade_point;
    /**
     * 学期
     */
    @Size(max = 20, message = "编码长度不能超过20")
    @Length(max = 20, message = "编码长度不能超过20")
    private String semester;
    /**
     * 评语
     */
    @Size(max = -1, message = "编码长度不能超过-1")
    @Length(max = -1, message = "编码长度不能超过-1")
    private String comments;
}
