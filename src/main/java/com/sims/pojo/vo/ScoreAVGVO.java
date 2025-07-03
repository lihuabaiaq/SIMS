package com.sims.pojo.vo;

import com.sims.pojo.entity.AVGScore;
import lombok.Data;

import java.util.List;

@Data
public class ScoreAVGVO {
    private List<AVGScore> studentScoreList;
    private List<AVGScore> averageScoreList;

}
