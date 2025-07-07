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

    /**
     * 教师登录接口
     * 接收教师登录信息，验证后生成JWT令牌并返回教师信息
     */
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

    /**
     * 修改教师信息接口
     * 接收包含修改信息的TeacherChangeDTO对象，更新教师资料
     */
    @PatchMapping("/changeInfo")
    public Result<Void> changeInfo(@RequestBody TeacherChangeDTO teacherChangeDTO){
        teacherService.teachangeInfo(teacherChangeDTO);
        return Result.ok();
    }

    /**
     * 添加课程接口
     * 接收课程信息并保存到数据库
     */
    @PostMapping("/addcourse")
    public Result<Void> addCourse(@RequestBody Course course){
        teacherService.saveCourse(course);
        return Result.ok();
    }

    /**
     * 获取教师所授课程接口
     * 根据教师ID查询所有教授的课程
     */
    @GetMapping("/getcourse")
    public Result<List<Course>> getCourse(@RequestParam Long teacherId){
       return Result.ok(teacherService.getCourse(teacherId));
    }

    /**
     * 获取课程成绩接口
     * 根据课程ID查询所有学生的成绩记录
     */
    @GetMapping("/getgrade")
    public Result<List<Grade>> getGrade(@RequestParam Long courseId){
        return Result.ok(teacherService.getGrade(courseId));
    }

    /**
     * 更新成绩接口
     * 接收成绩数据列表批量更新学生考试成绩等信息
     */
    @PutMapping("/scores")
    public Result<Void> updateScores(@RequestBody List<ScoreDTO> scoreList){
        teacherService.updateScores(scoreList);
        return Result.ok();
    }

    /**
     * 结束课程接口
     * 将课程状态标记为结课
     */
    @PostMapping("/endCourse")
    public Result<Void> endCourse(@RequestParam Long courseId){
        teacherService.endCourse(courseId);
        return Result.ok();
    }
}
