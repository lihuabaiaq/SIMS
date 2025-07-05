package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.dto.ScoreDTO;
import com.sims.pojo.entity.Grade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {
    void updateScores(List<ScoreDTO> scoreList);
}
