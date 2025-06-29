package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.entity.Job;
import com.sims.pojo.entity.JobActivityWeight;
import com.sims.pojo.entity.JobCompetitionWeight;
import com.sims.pojo.entity.JobCourseWeight;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
    List<JobCourseWeight> getCourseWeight(Long jobId);

    List<JobCompetitionWeight> getCompetitionWeight(Long jobId);

    List<JobActivityWeight> getActivityWeight(Long jobId);
}
