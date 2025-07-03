package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {


    void registerCourse(Long courseId);

    List<Course> getCourseList(Long studentId);

    void deleteCourse(Long courseId);
}
