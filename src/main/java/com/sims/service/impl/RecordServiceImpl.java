package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.handle.Exception.LoginException;
import com.sims.mapper.RecordMapper;
import com.sims.pojo.vo.ActivityRecordVO;
import com.sims.pojo.vo.CompetitionRecordVO;
import com.sims.pojo.vo.StudentGradeVO;
import com.sims.service.RecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {
    @Resource
    private RecordMapper recordMapper;

    /**
     * 获取学生的课程成绩记录
     * @param studentId 学生ID
     * @param semester 学期
     * @return 学生的课程成绩记录列表
     */
    @Override
    public List<StudentGradeVO> getCourseRecord(Long studentId, String semester) {
        if (studentId==null)
            throw new RuntimeException("登录状态错误");
        return recordMapper.getCourseRecord(studentId, semester);
    }

    /**
     * 获取学生的竞赛记录
     * @param studentId 学生ID
     * @param semester 学期
     * @return 学生的竞赛记录列表
     */
    @Override
    public List<CompetitionRecordVO> getCompetitionRecord(Long studentId, String semester) {
        if (studentId==null)
            throw new LoginException("登录状态错误");
        LocalDate startDate =null;
        LocalDate endDate = null;
        if(semester!=null){
            String[] split=semester.split("-");
            if(split[2].equals("1")) {
                startDate = LocalDate.parse(split[0] + "-09-01");
                endDate=LocalDate.parse(split[1] + "-03-01");
            }else {
                startDate = LocalDate.parse(split[1] + "-03-01");
                endDate = LocalDate.parse(split[1] + "-09-01");
            }
        }
        return recordMapper.getCompetitionRecord(studentId, startDate, endDate);
    }

    /**
     * 获取学生的活动记录
     * @param studentId 学生ID
     * @param semester 学期
     * @return 学生的活动记录列表
     */
    @Override
    public List<ActivityRecordVO> getActivityRecord(Long studentId, String semester) {
        if (studentId==null)
            throw new LoginException("登录状态错误");
        LocalDateTime startDate =null;
        LocalDateTime endDate = null;
        if(semester!=null){
            String[] split=semester.split("-");
            if(split[2].equals("1")) {
                startDate = LocalDate.parse(split[0] + "-09-01").atStartOfDay();
                endDate=LocalDate.parse(split[1] + "-03-01").atStartOfDay();
            }else {
                startDate = LocalDate.parse(split[1] + "-03-01").atStartOfDay();
                endDate = LocalDate.parse(split[1] + "-09-01").atStartOfDay();
            }
        }
        return recordMapper.getActivityRecord(studentId, startDate, endDate);
    }
}
