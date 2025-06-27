package com.sims.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScoreAVGVO {
    private List<Double> studentScoreList;
    private List<Double> averageScoreList;
}
