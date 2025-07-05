package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.constants.RecommendConstants;
import com.sims.handle.Exception.CompetitionExceotion;
import com.sims.handle.Exception.CourseException;
import com.sims.mapper.CompetitionMapper;
import com.sims.mapper.CourseMapper;
import com.sims.pojo.dto.CompetitionDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Competition;
import com.sims.pojo.entity.CompetitionCourseWeight;
import com.sims.pojo.vo.CompetitionVO;
import com.sims.service.CompetitionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        LocalDate exceptDate = LocalDate.now().plusMonths(6L);
        List<Competition> competitions = this.query()
                .lt("registration_deadline", exceptDate)
                .gt("registration_deadline", LocalDate.now())
                .list();
        if(competitions.isEmpty())
            throw new CompetitionExceotion("没有近期的竞赛");
        return competitions;
    }

    @Override
    @Transactional
    public List<CompetitionVO> competitionRecommend(CompetitionDTO competitionDTO) {
        Long studentId = competitionDTO.getStudentId();
        List<AVGScore> avgScores =studentService.getStudentScore(studentId);
        if(avgScores.isEmpty())
            throw new CourseException("学号错误，没有成绩");
        Map<String, Double> avgScoreMap = avgScores.stream()
                .collect(Collectors.toMap(AVGScore::getCourseCategory, AVGScore::getAvgScore));
        List<Competition> competitions = this.query()
                .lt(competitionDTO.getMaxDate() != null, "registration_deadline", competitionDTO.getMaxDate())
                .gt(competitionDTO.getMinDate() != null, "registration_deadline", competitionDTO.getMinDate())
                .gt("registration_deadline", LocalDate.now())
                .eq(competitionDTO.getCategory() != null, "category", competitionDTO.getCategory())
                .eq(competitionDTO.getLevel() != null, "level", competitionDTO.getLevel())
                .list();
        List<CompetitionVO> competitionVOS = competitions.stream(). map(competition -> {
            CompetitionVO competitionVO = new CompetitionVO();
            BeanUtils.copyProperties(competition, competitionVO);
            List<CompetitionCourseWeight> courseWeights = competitionMapper.getCourseWeight(competition.getName());
            double comWeight = 0.0;
            StringBuilder stringBuilder = new StringBuilder();
            for (CompetitionCourseWeight courseWeight : courseWeights) {
                if(avgScoreMap.containsKey(courseWeight.getCourseCategory()))
                {
                    Double avgScore = avgScoreMap.get(courseWeight.getCourseCategory());
                    comWeight += avgScore* courseWeight.getWeight().doubleValue();
                    stringBuilder.append(courseWeight.getCourseCategory()).append("获得了").append(avgScore).append("分\n");
                    }
                }
            String recommendStatus;
            if (comWeight > 80) {
                recommendStatus = RecommendConstants.RECOMMEND;
            } else if (comWeight > 60) {
                recommendStatus = RecommendConstants.RECOMMEND_MID;
            } else
                recommendStatus = RecommendConstants.RECOMMEND_LOW;
            String reason = stringBuilder.toString();
            competitionVO.setRecommendStatus(recommendStatus);
            competitionVO.setReason(reason);
            competitionVO.setComWeight(comWeight);
            return competitionVO;
        }).collect(Collectors.toList());
        return competitionVOS;
    }

    @Override
    public List<Competition> getALl() {
        List<Competition> allcompetitions = this.query()
                .gt("registration_deadline", LocalDate.now())
                .list();
        return allcompetitions;
    }
}
