package com.sims.controller;

import com.sims.config.JwtConfiguration;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Student;
import com.sims.pojo.entity.Teacher;
import com.sims.pojo.vo.StudentLoginVO;
import com.sims.pojo.vo.TeacherLoginVO;
import com.sims.service.StudentService;
import com.sims.service.TeacherService;
import com.sims.util.JwtUtil;
import com.sims.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@Tag(name = "登录接口", description = "学生/教师登录接口")
public class LoginController {

    @Resource
    JwtConfiguration jwtConfiguration;
    @Resource
    StudentService studentService;
    @Resource
    TeacherService teacherService;

    @PostMapping("/student")
    public Result<StudentLoginVO> studentLogin(@RequestBody StudentDTO studentDTO){
        Student student = studentService.studentLogin(studentDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", student.getStudentId());
        String token = JwtUtil.createJWT(jwtConfiguration.getSecretKey(), jwtConfiguration.getTtlMillis(), claims);
        StudentLoginVO studentLoginVO = new StudentLoginVO();
        studentLoginVO.setToken(token);
        studentLoginVO.setStudent(student);
        return Result.ok(studentLoginVO);
    }

    @PostMapping("/teacher")
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

}
