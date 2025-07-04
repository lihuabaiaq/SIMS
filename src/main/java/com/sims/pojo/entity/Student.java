package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName student
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("student")
public class Student implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    @TableId
    private Long studentId;
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
    * 年级，如"2022级"
    */
    @NotBlank(message="[年级，如'2022级']不能为空")
    @Size(max= 10,message="编码长度不能超过10")
    @Length(max= 10,message="编码长度不能超过10")
    private String grade;
    /**
    * 专业
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String major;
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
    
    private Date admission_date;
    /**
    * 
    */
    @Size(max= 200,message="编码长度不能超过200")
    
    @Length(max= 200,message="编码长度不能超过200")
    private String address;
    /**
    * 1:在校, 0:离校
    */
    private Integer status;

}
