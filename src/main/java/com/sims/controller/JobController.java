package com.sims.controller;

import com.sims.pojo.entity.Job;
import com.sims.pojo.vo.JobVO;
import com.sims.service.JobService;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job")
public class JobController {
    @Resource
    JobService jobService;

    @GetMapping("/all")
    public Result<List<Job>> getAllJob() {
        return Result.ok(jobService.list());
    }

    @GetMapping("/commend")
    private Result<List<JobVO>> JobRecommend(@RequestParam Long studentId) {
        return Result.ok(jobService.JobRecommend(studentId));
    }
    //TODO：增加一个刷新接口
}
