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
* @TableName student_competition
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCompetition implements Serializable {

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
    
    private Long competition_id;
    /**
    * 角色:队长,队员等
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String role;
    /**
    * 获奖情况
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String award;
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
