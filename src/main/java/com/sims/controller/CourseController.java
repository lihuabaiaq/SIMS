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

    @GetMapping("/list/having")
    //获取开放选课且该学生没有选的课程
    public Result<List<CourseVO>> getHavingList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHavingList(studentId));
    }

    @GetMapping("/list/had")
    //获取开放选课且该学生已经选择的课程
    public Result<List<CourseVO>> getHadList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHadList(studentId));
    }

    @GetMapping("/list/behaving")
    //获取未开放的所有课程
    public Result<List<CourseVO>> getBehavingList(@RequestParam Long studentId) {
        return Result.ok(courseService.getBeHavingList(studentId));
    }

    @PostMapping("/register")
    public Result<Void> registerCourse(@RequestParam Long courseId) {
        courseService.registerCourse(courseId);
        return Result.ok();
    }

    @DeleteMapping("/delete")
    public Result<Void> deleteCourse(@RequestParam Long courseId) {
        courseService.deleteCourse(courseId);
        return Result.ok();
    }
}
