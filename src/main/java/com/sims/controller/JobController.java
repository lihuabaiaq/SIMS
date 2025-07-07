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

    /**
     * 获取所有岗位信息
     * @return 返回数据库中所有的Job对象列表
     */
    @GetMapping("/all")
    public Result<List<Job>> getAllJob() {
        return Result.ok(jobService.list());
    }

    /**
     * 根据学生ID推荐岗位
     * @param studentId 学生的唯一标识符
     * @return 返回推荐给指定学生的JobVO对象列表，包含推荐分数、理由和状态
     */
    @GetMapping("/commend")
    private Result<List<JobVO>> JobRecommend(@RequestParam Long studentId) {
        return Result.ok(jobService.JobRecommend(studentId));
    }

    /**
     * 刷新岗位数据
     * 用于重新加载或更新系统中的岗位信息
     * @return 操作结果，表示刷新操作是否成功
     */
    @DeleteMapping("/refresh")
    public Result<Void> refresh() {
        jobService.refresh();
        return Result.ok();
    }
}
