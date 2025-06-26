package com.sims.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;
import java.math.BigDecimal;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName activity
*/
public class Activity implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    private Long activityId;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Length(max= 100,message="编码长度不能超过100")
    private String name;
    /**
    * 活动类别:志愿服务,文艺表演,体育比赛,学术讲座等
    */
    @NotBlank(message="[活动类别:志愿服务,文艺表演,体育比赛,学术讲座等]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String category;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Length(max= 100,message="编码长度不能超过100")
    private String organizer;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Length(max= 100,message="编码长度不能超过100")
    private String location;
    /**
    * 
    */
    private Date startTime;
    /**
    * 
    */
    private Date endTime;
    /**
    * 
    */
    @Size(max= -1,message="编码长度不能超过-1")
    @Length(max= -1,message="编码长度不能超过-1")
    private String description;
    /**
    *
    */
    private Integer maxParticipants;
    /**
    * 
    */

    private Integer currentParticipants;
    /**
    * 可获得的学时/学分
    */

    private BigDecimal creditHours;

}
