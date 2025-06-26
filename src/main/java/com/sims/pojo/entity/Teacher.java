package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName teacher
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("teacher")
public class Teacher implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    private Long teacherId;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String name;
    /**
    * 
    */
    
    private String gender;
    /**
    * 
    */
    
    private Date birth_date;
    /**
    * 职称
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String title;
    /**
    * 院系
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String department;
    /**
    * 
    */
    @Size(max= 15,message="编码长度不能超过15")
    
    @Length(max= 15,message="编码长度不能超过15")
    private String phone;
    /**
    * 
    */
    @Size(max= 50,message="编码长度不能超过50")
    
    @Length(max= 50,message="编码长度不能超过50")
    private String email;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    
    @Length(max= 100,message="编码长度不能超过100")
    private String password;
    /**
    * 
    */
    
    private Date hire_date;
    /**
    * 研究方向
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Length(max= 100,message="编码长度不能超过100")
    private String research_field;

}
