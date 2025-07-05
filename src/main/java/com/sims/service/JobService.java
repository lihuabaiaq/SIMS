package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.entity.Job;
import com.sims.pojo.vo.JobVO;

import java.util.List;

public interface JobService extends IService<Job> {
    List<JobVO> JobRecommend(Long studentId);

    void refresh();
}
