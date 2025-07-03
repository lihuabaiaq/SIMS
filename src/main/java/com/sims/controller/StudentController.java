package com.sims.controller;


import com.sims.config.JwtConfiguration;
import com.sims.pojo.dto.StudentChangeDTO;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.Student;
import com.sims.pojo.vo.ScoreAVGVO;
import com.sims.pojo.vo.StudentGradeVO;
import com.sims.pojo.vo.StudentLoginVO;
import com.sims.service.StudentService;
import com.sims.service.TeacherService;
import com.sims.util.JwtUtil;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    JwtConfiguration jwtConfiguration;
    @Resource
    StudentService studentService;

    @PostMapping("/login")
    public Result<StudentLoginVO> studentLogin(@RequestBody StudentDTO studentDTO){
        Student student = studentService.studentLogin(studentDTO);
        System.out.println("接收到前端传来的账号密码");
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", student.getStudentId());
        String token = JwtUtil.createJWT(jwtConfiguration.getSecretKey(), jwtConfiguration.getTtlMillis(), claims);
        StudentLoginVO studentLoginVO = new StudentLoginVO();
        studentLoginVO.setToken(token);
        studentLoginVO.setStudent(student);
        return Result.ok(studentLoginVO);
    }

    @PatchMapping("/changeinfo")
    private Result<Void> changeInfo(@RequestBody StudentChangeDTO studentChangeDTO){
        studentService.changeInfo(studentChangeDTO);
        return Result.ok();
    }

    @GetMapping("/grade")
    private Result<List<StudentGradeVO>> getGrade(@RequestParam Long studentId){
        List<StudentGradeVO> grade = studentService.getGrade(studentId);
        return Result.ok(grade);
    }

    @PostMapping("/grade/select")
    private Result<List<StudentGradeVO>> select(@RequestBody StudentGradeDTO studentGradeDTO){
        List<StudentGradeVO> grade = studentService.select(studentGradeDTO);
        return Result.ok(grade);
    }

    @GetMapping("/studentanalyze/getavailable")
    private Result<List> getAvailableSemester(@RequestParam Long studentId){
        return Result.ok(studentService.getAvailableSemester(studentId));
    }

    @GetMapping("/studentanalyze/getcomparsion")//前端要确保两个学期的相对顺序，格式为2022-2023-1
    public Result<ScoreAVGVO> studyanalyze(@RequestParam Long studentId,String startsemester,String endsemester){
        ScoreAVGVO scoreAVGVO = studentService.studyanalyze(studentId,startsemester,endsemester);
        return Result.ok(scoreAVGVO);
    }
}
