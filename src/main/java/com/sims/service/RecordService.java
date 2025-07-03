package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.vo.ActivityRecordVO;
import com.sims.pojo.vo.CompetitionRecordVO;
import com.sims.pojo.vo.StudentGradeVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RecordService {
    List<StudentGradeVO> getCourseRecord(Long studentId, String semester);

    List<CompetitionRecordVO> getCompetitionRecord(Long studentId, String semester);

    List<ActivityRecordVO> getActivityRecord(Long studentId, String semester);
}
