package com.sims.util;

public class AwardScoreUtil {
    public static Double getAwardScore(String award,String level) {
        if(award.equals("一等奖")&&level.equals("国家级"))
            return 20.0;
        else if((award.equals("二等奖")&&level.equals("国家级")||award.equals("一等奖")&&level.equals("省级")))
            return 12.0;
        else if((award.equals("三等奖")&&level.equals("国家级")||award.equals("二等奖")&&level.equals("省级")))
            return 8.0;
        else
            return 0.0;
    }
}
