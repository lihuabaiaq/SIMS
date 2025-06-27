package com.sims.controller;

import com.sims.config.JwtConfiguration;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Teacher;
import com.sims.pojo.vo.ScoreAVGVO;
import com.sims.pojo.vo.TeacherLoginVO;
import com.sims.service.TeacherService;
import com.sims.util.JwtUtil;
import com.sims.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Resource
    TeacherService teacherService;
    @Resource
    JwtConfiguration jwtConfiguration;

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

    @PostMapping("/changeInfo")
    public Result<Void> changeInfo(@RequestBody TeacherChangeDTO teacherChangeDTO){
        teacherService.changeInfo(teacherChangeDTO);
        return Result.ok();
    }


}
