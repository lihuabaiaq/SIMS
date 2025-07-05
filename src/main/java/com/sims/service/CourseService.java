package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.entity.Course;
import com.sims.pojo.vo.CourseVO;

import java.util.List;

public interface CourseService extends IService<Course> {


    void registerCourse(Long courseId);

    void deleteCourse(Long courseId);

    List<CourseVO> getHavingList(Long studentId);

    List<CourseVO> getHadList(Long studentId);

    List<CourseVO> getBeHavingList(Long studentId);
}
