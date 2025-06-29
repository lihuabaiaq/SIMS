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

    @GetMapping()
    public Result<List<Competition>> getRecentCompetition(){
        List<Competition> competitions=competitionService.recentCompetition();
        return Result.ok(competitions);
    }

    @PostMapping("/recommendations")
    public Result<List<CompetitionVO>> competitionRecommend(@RequestBody CompetitionDTO competitionDTO){
        return Result.ok(competitionService.competitionRecommend(competitionDTO));
    }

    @GetMapping("/all")
    public Result<List<Competition>> getAllcompetition(){
        return Result.ok(competitionService.list());
    }

}
