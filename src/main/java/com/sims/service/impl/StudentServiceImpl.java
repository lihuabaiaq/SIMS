package com.sims.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.StudentMapper;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.entity.Student;
import com.sims.service.StudentService;
import com.sims.util.MD5Util;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {


    @Override
    public Student studentLogin(StudentDTO studentDTO) {
        String password = studentDTO.getPassword();
        Long studentId = studentDTO.getStudentId();
        Student student = this.query().eq("student_id", studentId).one();
        if (student != null && student.getPassword().equals(password)) {
            return student;
        }
        throw new RuntimeException("用户名或密码错误");
    }


}
