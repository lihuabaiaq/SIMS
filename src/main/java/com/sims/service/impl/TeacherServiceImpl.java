package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.TeacherMapper;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Teacher;
import com.sims.service.TeacherService;
import com.sims.util.MD5Util;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>  implements TeacherService {

    @Override
    public Teacher teacherLogin(TeacherDTO teacherDTO) {
        String password = teacherDTO.getPassword();
        Long teacherId = teacherDTO.getTeacherId();
        Teacher teacher = this.query().eq("teacher_id", teacherId).one();
        if (teacher != null && teacher.getPassword().equals(MD5Util.encrypt(password))) {
            return teacher;
        }
        throw new RuntimeException("用户名或密码错误");
    }
}
