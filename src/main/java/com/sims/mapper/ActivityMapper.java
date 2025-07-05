package com.sims.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityMapper {

    @Select("select activity.category,activity.name from activity " +
            "join sism.studentactivity s on activity.activity_id = s.activity_id " +
            "where student_id=#{studentId}")
    List<String> getActivities(Long studentId);
}
