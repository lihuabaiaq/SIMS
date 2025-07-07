package com.sims.controller;

import com.sims.config.JwtConfiguration;
import com.sims.pojo.dto.ScoreDTO;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Grade;
import com.sims.pojo.entity.Teacher;
import com.sims.pojo.vo.TeacherLoginVO;
import com.sims.service.TeacherService;
import com.sims.util.JwtUtil;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Resource
    TeacherService teacherService;
    @Resource
    JwtConfiguration jwtConfiguration;

    @PostMapping("/login")
    public Result<TeacherLoginVO> teacherLogin(@RequestBody TeacherDTO teacherDTO){
        Teacher teacher = teacherService.teacherLogin(teacherDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", teacher.getTeacherId());
        String token = JwtUtil.createJWT(jwtConfiguration.getSecretKey(), jwtConfiguration.getTtlMillis(), claims);
        TeacherLoginVO teacherLoginVO = new TeacherLoginVO();
        teacherLoginVO.setToken(token);
        teacherLoginVO.setTeacher(teacher);
        return Result.ok(teacherLoginVO);
    }

    @PatchMapping("/changeInfo")
    public Result<Void> changeInfo(@RequestBody TeacherChangeDTO teacherChangeDTO){
        teacherService.teachangeInfo(teacherChangeDTO);
        return Result.ok();
    }

    @PostMapping("/addcourse")
    public Result<Void> addCourse(@RequestBody Course course){
        teacherService.saveCourse(course);
        return Result.ok();
    }

    @GetMapping("/getcourse")
    public Result<List<Course>> getCourse(@RequestParam Long teacherId){
       return Result.ok(teacherService.getCourse(teacherId));
    }

    @GetMapping("/getgrade")
    public Result<List<Grade>> getGrade(@RequestParam Long courseId){
        return Result.ok(teacherService.getGrade(courseId));
    }

    @PutMapping("/scores")
    public Result<Void> updateScores(@RequestBody List<ScoreDTO> scoreList){
        teacherService.updateScores(scoreList);
        return Result.ok();
    }

    @PostMapping("/endCourse")
    public Result<Void> endCourse(@RequestParam Long courseId){
        teacherService.endCourse(courseId);
        return Result.ok();
    }
}
