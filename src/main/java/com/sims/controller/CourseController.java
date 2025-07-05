package com.sims.controller;

import com.sims.pojo.entity.Course;
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
    //获取开放选课但是该学生未选的课程
    public Result<List<Course>> getHavingList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHavingList(studentId));
    }

    @GetMapping("/list/had")
    //获取开放选课同时该学生已经选的课程
    public Result<List<Course>> getHadList(@RequestParam Long studentId) {
        return Result.ok(courseService.getHadList(studentId));
    }

    @GetMapping("/list/behaving")
    //获取即将开放选课的课程
    public Result<List<Course>> getBehavingList(@RequestParam Long studentId) {
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
