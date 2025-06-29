package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.entity.Competition;
import com.sims.pojo.entity.CompetitionAward;
import com.sims.pojo.entity.CompetitionCourseWeight;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    List<CompetitionCourseWeight> getCourseWeight(String competitionName);


    @MapKey("category")
    Map<String,List<CompetitionAward>> getCompetitionAward(Long studentId);
}
