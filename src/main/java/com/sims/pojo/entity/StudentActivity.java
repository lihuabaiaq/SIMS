package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName student_activity
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentActivity implements Serializable {

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
    
    private Long activity_id;
    /**
    * 角色:组织者,参与者等
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String role;
    /**
    * 参与时长
    */
    private BigDecimal hours;
    /**
    * 证书编号
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String certificate_no;
    /**
    * 
    */
    
    private Date created_at;

}
