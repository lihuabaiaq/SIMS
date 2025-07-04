package com.sims.controller;

import com.sims.pojo.entity.Course;
import com.sims.service.CourseService;
import com.sims.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/course")

public class CourseController {
    @Resource
    private CourseService courseService;

    @GetMapping("/list")
    @Operation(summary = "获取课程列表")
    public Result<List<Course>> getCourseList(@RequestParam Long studentId) {
        return Result.ok(courseService.getCourseList(studentId));
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
