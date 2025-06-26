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
* @TableName courseselection
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Courseselection implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    
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
    * 
    */
    
    private Date selection_date;
    /**
    * 1:已选, 2:已退, 3:已完成
    */

    private Integer status;
    /**
    * 学期
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String semester;

}
