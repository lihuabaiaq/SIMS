package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.CompetitionMapper;
import com.sims.mapper.CourseMapper;
import com.sims.pojo.dto.CompetitionDTO;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Competition;
import com.sims.pojo.entity.CompetitionCourseWeight;
import com.sims.pojo.vo.CompetitionVO;
import com.sims.service.CompetitionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {

    @Resource
    CompetitionMapper competitionMapper;
    @Resource
    StudentServiceImpl studentService;
    @Resource
    CourseMapper courseMapper;
    @Override
    public List<Competition> recentCompetition() {
        LocalDate localDate = LocalDate.now().plusMonths(6L);
        List<Competition> competitions = this.query()
                .lt("registration_deadline", localDate)
                .list();
        if(competitions.isEmpty())
            throw new RuntimeException("没有近期的竞赛");
        return competitions;
    }

    @Override
    public List<CompetitionVO> competitionRecommend(CompetitionDTO competitionDTO) {
        Long studentId = competitionDTO.getStudentId();
        List<AVGScore> avgScores = courseMapper.getAVGScore(studentId);
        List<Competition> competitions = this.query()
                .lt(competitionDTO.getMaxDate() != null, "registration_deadline", competitionDTO.getMaxDate())
                .gt(competitionDTO.getMinDate() != null, "registration_deadline", competitionDTO.getMinDate())
                .eq(competitionDTO.getCategory() != null, "category", competitionDTO.getCategory())
                .eq(competitionDTO.getLevel() != null, "level", competitionDTO.getLevel())
                .list();
        List<CompetitionVO> competitionVOS = new ArrayList<>();
        competitions.forEach(competition -> {
            CompetitionVO competitionVO = new CompetitionVO();
            BeanUtils.copyProperties(competition, competitionVO);
            List<CompetitionCourseWeight> courseWeights = competitionMapper.getCourseWeight(competition.getName());
            double comWeight = 0.0;
            StringBuilder stringBuilder = new StringBuilder();
            for (CompetitionCourseWeight courseWeight : courseWeights) {
                for (AVGScore avgScore : avgScores) {
                    if (avgScore.getCourseCategory().equals(courseWeight.getCourseCategory())) {
                        comWeight += avgScore.getAvgScore() * courseWeight.getWeight().doubleValue();
                        stringBuilder.append(courseWeight.getCourseCategory()).append("获得了").append(avgScore.getAvgScore()).append("分\n");
                        break;
                    }
                }
            }
            String recommendStatus;
            if (comWeight > 80) {
                recommendStatus = "推荐";
            } else if (comWeight > 60) {
                recommendStatus = "可以考虑";
            } else
                recommendStatus = "不推荐";
            String reason = stringBuilder.toString();
            competitionVO.setRecommendStatus(recommendStatus);
            competitionVO.setReason(reason);
        });
        return competitionVOS;
    }
}
