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
import com.sims.util.JwtUtil;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    JwtConfiguration jwtConfiguration;
    @Resource
    StudentService studentService;

    /**
     * 学生登录接口
     * 接收前端传来的账号密码，验证后生成 JWT 返回给前端
     */
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

    /**
     * 修改学生信息接口
     * 接收前端传来的修改后的学生信息并更新到数据库
     */
    @PatchMapping("/changeInfo")
    private Result<Void> changeInfo(@RequestBody StudentChangeDTO studentChangeDTO){
        studentService.stuchangeInfo(studentChangeDTO);
        return Result.ok();
    }

    /**
     * 获取学生成绩接口
     * 根据学生ID获取该学生的全部成绩信息
     */
    @GetMapping("/grade")
    private Result<List<StudentGradeVO>> getGrade(@RequestParam Long studentId){
        List<StudentGradeVO> grade = studentService.getGrade(studentId);
        return Result.ok(grade);
    }

    /**
     * 按条件查询学生成绩接口
     * 根据课程名称、学分等筛选条件查询学生的成绩
     */
    @PostMapping("/grade/select")
    private Result<List<StudentGradeVO>> select(@RequestBody StudentGradeDTO studentGradeDTO){
        List<StudentGradeVO> grade = studentService.select(studentGradeDTO);
        return Result.ok(grade);
    }

    /**
     * 获取可用学期列表接口
     * 查询该学生所有可分析的学期名称
     */
    @GetMapping("/studentanalyze/getavailable")
    private Result<List<String>> getAvailableSemester(@RequestParam Long studentId){
        return Result.ok(studentService.getAvailableSemester(studentId));
    }

    /**
     * 学习分析对比接口
     * 对比两个学期的学习成绩情况，返回分析结果
     * 前端要确保两个学期的相对顺序，格式为2022-2023-1
     */
    @GetMapping("/studentanalyze/getcomparsion")
    public Result<ScoreAVGVO> studyanalyze(@RequestParam Long studentId,String startsemester,String endsemester){
        ScoreAVGVO scoreAVGVO = studentService.studyanalyze(studentId,startsemester,endsemester);
        return Result.ok(scoreAVGVO);
    }
}
