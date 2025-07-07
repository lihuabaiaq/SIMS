package com.sims.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.constants.RedisConstants;
import com.sims.handle.Exception.DataBaseException;
import com.sims.mapper.*;
import com.sims.pojo.entity.*;
import com.sims.pojo.vo.JobVO;
import com.sims.service.JobService;
import com.sims.service.StudentService;
import com.sims.util.AwardScoreUtil;
import com.sims.util.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService{

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    ActivityMapper activityMapper;
    @Resource
    private StudentService studentService;


    @Override
    @Transactional
    public List<JobVO> JobRecommend(Long studentId) {
        String jobKey = RedisConstants.JOB_COMMEND_KEY + studentId;
        Set<String> redisJobVOS = stringRedisTemplate.opsForZSet().reverseRange(jobKey, 0, 5);
        if (redisJobVOS != null && !redisJobVOS.isEmpty()) {
            // 如果 Redis 中存在缓存的推荐岗位，直接返回这些岗位
            return redisJobVOS.stream()
                    .map(jobVO -> JSONObject.parseObject(jobVO, JobVO.class))
                    .collect(Collectors.toList());
        }
        // 获取学生的课程平均成绩并构建映射关系（课程类别 -> 平均分）
        Map<String, Double> scoreMap = studentService.getStudentScore(studentId)
                .stream().collect(Collectors.toMap(AVGScore::getCourseCategory, AVGScore::getAvgScore));
        // 获取学生参与的竞赛奖项并按竞赛类别分组
        Map<String, List<CompetitionAward>> competitionMap = competitionMapper.getCompetitionAward(studentId)
                .stream().collect(Collectors.groupingBy(CompetitionAward::getCategory));
        // 获取学生参与的活动列表
        List<String> activities = activityMapper.getActivities(studentId);
        // 获取所有岗位信息，并转换为 JobVO 列表
        List<Job> jobs = this.list();
        List<JobVO> jobVOS = jobs.stream().map(job -> {
            JobVO jobVO = new JobVO();
            BeanUtils.copyProperties(job, jobVO);
            return jobVO;
        }).toList();
        // 遍历每个岗位，计算匹配分数和推荐理由
        jobVOS.forEach(jobVO -> {
            Long jobId = jobVO.getJobId();
            double score = 0D;
            StringBuilder stringBuilder = new StringBuilder();
            // 获取岗位对应的课程权重并计算课程得分
            List<JobCourseWeight> jobCourseWeights = jobMapper.getCourseWeight(jobId);
            for (JobCourseWeight jobCourseWeight : jobCourseWeights) {
                String courseCategory = jobCourseWeight.getCourseCategory();
                if (scoreMap.containsKey(courseCategory)) {
                    score += scoreMap.get(courseCategory) * jobCourseWeight.getWeight();
                    stringBuilder.append(courseCategory + "获得了" + scoreMap.get(courseCategory) + "分\n");
                }
            }
            // 获取岗位对应的竞赛权重并计算竞赛奖项得分
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
            // 获取岗位对应的活动权重并计算活动参与得分
            List<JobActivityWeight> jobActivityWeights = jobMapper.getActivityWeight(jobId);
            for (JobActivityWeight jobActivityWeight : jobActivityWeights) {
                if (activities.contains(jobActivityWeight.getActivityCategory())) {
                    score += 5D * jobActivityWeight.getWeight();
                    stringBuilder.append("积极参加" + jobActivityWeight.getActivityCategory() + "类活动\n");
                }
            }
            // 设置最终得分与推荐理由
            jobVO.setReason(stringBuilder.toString());
            jobVO.setScore(score);
            // 将当前岗位推荐结果写入 Redis 缓存
            stringRedisTemplate.opsForZSet().add(jobKey, JSONObject.toJSONString(jobVO), score);
        });
        try {
            // 从 Redis 中读取前五条推荐岗位数据
            Set<String> JobVOS = stringRedisTemplate.opsForZSet().reverseRange(jobKey, 0, 5);
            return JobVOS.stream()
                    .map(jobVO -> JSONObject.parseObject(jobVO, JobVO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("数据库异常");
            throw new DataBaseException("数据库错误");
        }
    }

    @Override
    public void refresh() {
        Long studentId = UserHolder.getId();
        stringRedisTemplate.delete(RedisConstants.JOB_COMMEND_KEY + studentId);
    }

}
