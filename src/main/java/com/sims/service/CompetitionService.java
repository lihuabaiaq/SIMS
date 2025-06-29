package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.dto.CompetitionDTO;
import com.sims.pojo.entity.Competition;
import com.sims.pojo.vo.CompetitionVO;

import java.util.List;

public interface CompetitionService extends IService<Competition> {
    List<Competition> recentCompetition();

    List<CompetitionVO> competitionRecommend(CompetitionDTO competitionDTO);
}
