package com.sims.controller;

import com.sims.pojo.dto.CompetitionDTO;
import com.sims.pojo.entity.Competition;
import com.sims.pojo.vo.CompetitionVO;
import com.sims.service.CompetitionService;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competition")
public class CompetitionController {
    @Resource
    CompetitionService competitionService;

    /**
     * 获取近期竞赛信息
     * @return 操作结果，包含近期的竞赛列表
     */
    @GetMapping("/recent")
    public Result<List<Competition>> getRecentCompetition(){
        List<Competition> competitions=competitionService.recentCompetition();
        return Result.ok(competitions);
    }

    /**
     * 根据学生信息获取竞赛推荐
     * @param competitionDTO 学生信息及筛选条件
     * @return 操作结果，包含推荐的竞赛VO列表
     */
    @PostMapping("/recommendations")
    public Result<List<CompetitionVO>> competitionRecommend(@RequestBody CompetitionDTO competitionDTO){
        return Result.ok(competitionService.competitionRecommend(competitionDTO));
    }

    /**
     * 获取所有竞赛信息
     * @return 操作结果，包含所有竞赛列表
     */
    @GetMapping("/all")
    public Result<List<Competition>> getAllcompetition(){
        return Result.ok(competitionService.getALl());
    }
}
