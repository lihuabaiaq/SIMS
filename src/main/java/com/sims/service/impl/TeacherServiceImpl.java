package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.TeacherMapper;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Teacher;
import com.sims.service.TeacherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>  implements TeacherService {

    @Resource
    TeacherMapper teacherMapper;

    @Override
    public Teacher teacherLogin(TeacherDTO teacherDTO) {
        String password = teacherDTO.getPassword();
        Long teacherId = teacherDTO.getTeacherId();
        Teacher teacher = this.query().eq("teacher_id", teacherId).one();
        if (teacher != null && teacher.getPassword().equals(password)) {
            return teacher;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public void changeInfo(TeacherChangeDTO teacherChangeDTO) {
        Teacher teacher=query()
                .eq("teacher_id",teacherChangeDTO.getTeacherId())
                .one();
        if(teacher==null || !teacher.getPassword().equals(teacherChangeDTO.getOriginalPassword()))
            throw new RuntimeException("原密码错误,无法修改");
        teacherMapper.updateInfo(teacherChangeDTO);
    }
}
