package com.sims.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.*;
import com.sims.pojo.entity.*;
import com.sims.pojo.vo.JobVO;
import com.sims.service.JobService;
import com.sims.util.AwardScoreUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService{

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CompetitionMapper competitionMapper;

    @Resource
    ActivityMapper activityMapper;

    @Override
    @Transactional
    public List<JobVO> JobRecommend(Long studentId) {
        String jobKey = "job:commend:" + studentId;
        Set<String> redisJobVOS = stringRedisTemplate.opsForZSet().reverseRange(jobKey, 0, 5);
        if (!redisJobVOS.isEmpty()) {
            return redisJobVOS.stream()
                    .map(jobVO -> JSONObject.parseObject(jobVO, JobVO.class))
                    .collect(Collectors.toList());
        }
        Map<String, Double> scoreMap = courseMapper.getStudentScore(studentId,null)
                .stream().collect(Collectors.toMap(AVGScore::getCourseCategory, AVGScore::getAvgScore));
        Map<String, List<CompetitionAward>> competitionMap = competitionMapper.getCompetitionAward(studentId)
                .stream().collect(Collectors.groupingBy(CompetitionAward::getCategory));
        List<String> activities = activityMapper.getActivities(studentId);
        List<Job> jobs = this.list();
        List<JobVO> jobVOS = jobs.stream().map(job -> {
            JobVO jobVO = new JobVO();
            BeanUtils.copyProperties(job, jobVO);
            return jobVO;
        }).collect(Collectors.toList());
        jobVOS.forEach(jobVO -> {
            Long jobId = jobVO.getJobId();
            Double score = 0D;
            StringBuilder stringBuilder = new StringBuilder();
            List<JobCourseWeight> jobCourseWeights = jobMapper.getCourseWeight(jobId);
            for (JobCourseWeight jobCourseWeight : jobCourseWeights) {
                String courseCategory = jobCourseWeight.getCourseCategory();
                if (scoreMap.containsKey(courseCategory)) {
                    score += scoreMap.get(courseCategory) * jobCourseWeight.getWeight();
                    stringBuilder.append(courseCategory + "获得了" + scoreMap.get(courseCategory) + "分\n");
                }
            }
            List<JobCompetitionWeight> jobCompetitionWeights = jobMapper.getCompetitionWeight(jobId);
            for (JobCompetitionWeight jobCompetitionWeight : jobCompetitionWeights) {
                String competitionCategory = jobCompetitionWeight.getCompetitionCategory();
                List<CompetitionAward> awards = competitionMap.get(competitionCategory);
                if (awards != null) {
                    for (CompetitionAward award : awards) {
                        score += AwardScoreUtil.getAwardScore(award.getAward(), award.getLevel()) * jobCompetitionWeight.getWeight();
                        stringBuilder.append("在" + award.getName() + "中获得" + award.getAward() + "\n");
                    }
                }
            }
            List<JobActivityWeight> jobActivityWeights = jobMapper.getActivityWeight(jobId);
            for (JobActivityWeight jobActivityWeight : jobActivityWeights) {
                if (activities.contains(jobActivityWeight.getActivityCategory())) {
                    score += 5D * jobActivityWeight.getWeight();
                    stringBuilder.append("积极参加" + jobActivityWeight.getActivityCategory() + "类活动\n");
                }
            }
            jobVO.setReason(stringBuilder.toString());
            jobVO.setScore(score);
            stringRedisTemplate.opsForZSet().add(jobKey, JSONObject.toJSONString(jobVO), score);
            System.out.println("存储在redis里面");
        });
        try {
            Set<String> JobVOS = stringRedisTemplate.opsForZSet().reverseRange(jobKey, 0, 5);
            return JobVOS.stream()
                    .map(jobVO -> JSONObject.parseObject(jobVO, JobVO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("数据库错误");
        }
    }

}
