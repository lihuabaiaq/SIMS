package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Teacher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeacherService extends IService<Teacher> {
    Teacher teacherLogin(TeacherDTO teacherDTO);

    void changeInfo(TeacherChangeDTO teacherChangeDTO);

    void saveCourse(Course course);

    List<Course> getCourse(Long teacherId);
}
