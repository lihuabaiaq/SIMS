package com.sims.controller;

import com.sims.pojo.vo.CourseVO;
import com.sims.service.CourseService;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/course")

public class CourseController {
    @Resource
    private CourseService courseService;

    /**
     * 获取开放选课且该学生没有选的课程
     * @param studentId 学生ID
     * @return 课程信息列表
     */
    @GetMapping("/list/having")
    public Result<List<CourseVO>> getHavingList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHavingList(studentId));
    }

    /**
     * 获取开放选课且该学生已经选择的课程
     * @param studentId 学生ID
     * @return 课程信息列表
     */
    @GetMapping("/list/had")
    public Result<List<CourseVO>> getHadList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHadList(studentId));
    }

    /**
     * 获取未开放的所有课程
     * @param studentId 学生ID
     * @return 课程信息列表
     */
    @GetMapping("/list/behaving")
    public Result<List<CourseVO>> getBehavingList(@RequestParam Long studentId) {
        return Result.ok(courseService.getBeHavingList(studentId));
    }

    /**
     * 注册（选课）操作
     * @param courseId 课程ID
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<Void> registerCourse(@RequestParam Long courseId) {
        courseService.registerCourse(courseId);
        return Result.ok();
    }

    /**
     * 删除（退课）操作
     * @param courseId 课程ID
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteCourse(@RequestParam Long courseId) {
        courseService.deleteCourse(courseId);
        return Result.ok();
    }
}
