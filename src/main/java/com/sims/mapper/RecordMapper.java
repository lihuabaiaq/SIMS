package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.vo.ActivityRecordVO;
import com.sims.pojo.vo.CompetitionRecordVO;
import com.sims.pojo.vo.StudentGradeVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecordMapper  {
    List<StudentGradeVO> getCourseRecord(Long studentId, String semester);

    List<CompetitionRecordVO> getCompetitionRecord(Long studentId, LocalDate startDate, LocalDate endDate);

    List<ActivityRecordVO> getActivityRecord(Long studentId, LocalDateTime startDate, LocalDateTime endDate);
}
