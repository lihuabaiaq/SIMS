package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Student;
import com.sims.pojo.entity.Teacher;
import org.springframework.stereotype.Service;

@Service
public interface StudentService extends IService<Student> {
    Student studentLogin(StudentDTO studentDTO);


}
