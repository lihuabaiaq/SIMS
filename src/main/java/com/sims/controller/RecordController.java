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
//先调用studentController里面的getAvailableSemester创建下拉列表
    @GetMapping("/course/{id}")
    public Result<List<StudentGradeVO>> getCourseRecord(@PathVariable(value = "id") Long studentId,
                                                        @RequestParam String semester) {
        return Result.ok(recordService.getCourseRecord(studentId, semester));
    }

    @GetMapping("/competition/{id}")
    public Result<List<CompetitionRecordVO>> getCompetitionRecord(@PathVariable(value = "id") Long studentId,
                                                                  @RequestParam String semester) {
        return Result.ok(recordService.getCompetitionRecord(studentId, semester));
    }
    @GetMapping("/activity/{id}")
    public Result<List<ActivityRecordVO>> getActivityRecord(@PathVariable(value = "id") Long studentId,
                                                            @RequestParam String semester){
        return Result.ok(recordService.getActivityRecord(studentId, semester));
    }
}
