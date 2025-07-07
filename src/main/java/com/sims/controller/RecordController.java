package com.sims.controller;

import com.sims.pojo.vo.ActivityRecordVO;
import com.sims.pojo.vo.CompetitionRecordVO;
import com.sims.pojo.vo.StudentGradeVO;
import com.sims.service.RecordService;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/record")
public class RecordController {
    @Resource
    private RecordService recordService;

    /**
     * 获取学生的课程成绩记录
     *
     * @param studentId 学生ID
     * @param semester  学期
     * @return Result<List<StudentGradeVO>> 包含学生课程成绩信息的结果对象
     */
    @GetMapping("/course/{id}")
    public Result<List<StudentGradeVO>> getCourseRecord(@PathVariable(value = "id") Long studentId,
                                                        @RequestParam String semester) {
        return Result.ok(recordService.getCourseRecord(studentId, semester));
    }

    /**
     * 获取学生的竞赛记录
     *
     * @param studentId 学生ID
     * @param semester  学期
     * @return Result<List<CompetitionRecordVO>> 包含学生竞赛信息的结果对象
     */
    @GetMapping("/competition/{id}")
    public Result<List<CompetitionRecordVO>> getCompetitionRecord(@PathVariable(value = "id") Long studentId,
                                                                  @RequestParam String semester) {
        return Result.ok(recordService.getCompetitionRecord(studentId, semester));
    }

    /**
     * 获取学生的活动记录
     *
     * @param studentId 学生ID
     * @param semester  学期
     * @return Result<List<ActivityRecordVO>> 包含学生活动信息的结果对象
     */
    @GetMapping("/activity/{id}")
    public Result<List<ActivityRecordVO>> getActivityRecord(@PathVariable(value = "id") Long studentId,
                                                            @RequestParam String semester){
        return Result.ok(recordService.getActivityRecord(studentId, semester));
    }
}
